package de.eindaniel.playerShops.listener;

import de.eindaniel.playerShops.Main;
import de.eindaniel.playerShops.gui.ShopGui;
import de.eindaniel.playerShops.gui.ShopStashGui;
import de.eindaniel.playerShops.notifications.NotificationManager;
import de.eindaniel.playerShops.shop.PlayerShop;
import de.eindaniel.playerShops.util.ChatInputHandler;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Interaction;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class InteractionListener implements Listener {

    private final Main plugin;

    public InteractionListener(Main plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInteract(PlayerInteractEntityEvent e) {
        if (!(e.getRightClicked() instanceof Interaction it)) return;

        Optional<String> keyOpt = plugin.entities().readKey(it.getPersistentDataContainer());
        if (keyOpt.isEmpty()) return;

        var shopOpt = plugin.shops().getByKey(keyOpt.get());
        if (shopOpt.isEmpty()) return;
        PlayerShop shop = shopOpt.get();

        e.setCancelled(true);

        Player p = e.getPlayer();
        if (shop.getOwner().equals(p.getUniqueId())) {
            ShopStashGui gui = new ShopStashGui(plugin, shop);
            gui.openFor(p);
        } else {
            Inventory inv = new ShopGui(shop).build();
            p.openInventory(inv);
        }
    }

    @EventHandler
    public void onClose(InventoryCloseEvent e) {
        if (!ShopStashGui.isStash(e.getView().title())) return;

        Player p = (Player) e.getPlayer();

        UUID pid = p.getUniqueId();
        String key = ShopStashGui.OPEN.remove(pid);
        if (key == null) return;

        var shopOpt = plugin.shops().getByKey(key);
        if (shopOpt.isEmpty()) return;
        PlayerShop shop = shopOpt.get();

        ShopStashGui.saveBack(shop, e.getInventory(), p);

        plugin.entities().updateLabel(shop);
        try {
            plugin.storage().saveAll();
        } catch (Exception ignored) {}
    }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player p)) return;

        // --- Shop-GUI (Kauf/Verkauf) ---
        if (ShopGui.isShop(e.getView().title())) {
            e.setCancelled(true);

            PlayerShop shop = nearestShopToViewer(p);
            if (shop == null) {
                p.closeInventory();
                return;
            }

            if (shop.getOwner().equals(p.getUniqueId())) {
                p.sendMessage(Main.prefix().append(MiniMessage.miniMessage().deserialize("<#ff1717>Du kannst deinen eigenen Shop nicht benutzen.")));
                return;
            }

            int slot = e.getRawSlot();
            if (slot == 11 && shop.isBuyEnabled()) {
                int need = shop.getAmountPerTrade();
                if (shop.countStashMaterial() < need) {
                    p.sendMessage(Main.prefix().append(MiniMessage.miniMessage().deserialize("<#ff1717>Das Lager von diesem Shop ist leer!")));
                    return;
                }
                double price = shop.getBuyPrice();
                if (!plugin.vault().has(p, price)) {
                    p.sendMessage(Main.prefix().append(MiniMessage.miniMessage().deserialize("<#ff1717>Du hast nicht genug Geld!")));
                    return;
                }
                if (!plugin.vault().withdraw(p, price)) {
                    p.sendMessage(Main.prefix().append(MiniMessage.miniMessage().deserialize("<#ff1717>Die Zahlung ist fehlgeschlagen, bitte melde das einem Entwickler.")));
                    return;
                }

                OfflinePlayer owner = plugin.getServer().getOfflinePlayer(shop.getOwner());
                plugin.vault().deposit(owner, price);

                int taken = shop.takeFromStash(need);

                ItemStack give = new ItemStack(shop.getMaterial(), taken);
                var leftover = p.getInventory().addItem(give);
                if (!leftover.isEmpty()) {
                    leftover.values().forEach(item -> p.getWorld().dropItemNaturally(p.getLocation(), item));
                }

                Component bought1 = MiniMessage.miniMessage().deserialize("<#1fff17>Transaktion erfolgreich!");
                Component bought2 = MiniMessage.miniMessage().deserialize("<#1fff17>+ <gray>" + taken + "x <white><lang:" + shop.getMaterial().translationKey() + ">");
                Component bought3 = MiniMessage.miniMessage().deserialize("<#ff1717>- <#a3ff2b>" + String.format("%.2f€", price));
                p.sendMessage(Main.prefix().append(bought1));
                p.sendMessage(Main.prefix().append(bought2));
                p.sendMessage(Main.prefix().append(bought3));
                Component msg = MiniMessage.miniMessage().deserialize("<#1fff17>" + p.getName() + " hat für " + String.format("%.2f", price) + "€ in einer deiner Shops eingekauft. (Kauf)");
                plugin.notifications().notifyShopOwner(shop.getOwner(), msg);
                plugin.entities().updateLabel(shop);
                try {
                    plugin.storage().saveAll();
                } catch (Exception ignored) {
                }
            } else if (slot == 15 && shop.isSellEnabled()) {
                int need = shop.getAmountPerTrade();
                if (!hasItems(p, shop.getMaterial(), need)) {
                    p.sendMessage(Main.prefix().append(MiniMessage.miniMessage().deserialize("<#ff1717>Du hast nicht genug <lang:" + shop.getMaterial().translationKey() + ">.")));
                    return;
                }
                double price = shop.getSellPrice();
                OfflinePlayer owner = plugin.getServer().getOfflinePlayer(shop.getOwner());
                if (!plugin.vault().has(owner, price)) {
                    p.sendMessage(Main.prefix().append(MiniMessage.miniMessage().deserialize("<#ff1717>Der Shop-Besitzer hat nicht genug Geld!")));
                    return;
                }
                if (!plugin.vault().withdraw(owner, price)) {
                    p.sendMessage(Main.prefix().append(MiniMessage.miniMessage().deserialize("<#ff1717>Die Auszahlung ist fehlgeschlagen, bitte melde dies einem Entwickler.")));
                    return;
                }
                if (!plugin.vault().deposit(p, price)) {
                    plugin.vault().deposit(owner, price);
                    p.sendMessage(Main.prefix().append(MiniMessage.miniMessage().deserialize("<#ff1717>Die Auszahlung ist fehlgeschlagen, bitte melde dies einem Entwickler.")));
                    return;
                }
                Component msg = MiniMessage.miniMessage().deserialize("<#1fff17>" + p.getName() + " hat in einer deiner Shops " + String.format("%.2f", price) + "€ erhalten. (Verkauf)");
                plugin.notifications().notifyShopOwner(shop.getOwner(), msg);
                takeItems(p, shop.getMaterial(), need);
                try {
                    shop.addToStash(need);
                } catch (IllegalStateException ex) {
                    p.sendMessage(Main.prefix().append(MiniMessage.miniMessage().deserialize("<#ff1717>Zu viele Items im Shop-Lager!")));
                }

                Component selled1 = MiniMessage.miniMessage().deserialize("<#1fff17>Transaktion erfolgreich!");
                Component selled2 = MiniMessage.miniMessage().deserialize("<#ff1717>- <gray>" + need + "x <white><lang:" + shop.getMaterial().translationKey() + ">");
                Component selled3 = MiniMessage.miniMessage().deserialize("<#a3ff2b>+ " + String.format("%.2f€", price));
                p.sendMessage(Main.prefix().append(selled1));
                p.sendMessage(Main.prefix().append(selled2));
                p.sendMessage(Main.prefix().append(selled3));
                plugin.entities().updateLabel(shop);
                try {
                    plugin.storage().saveAll();
                } catch (Exception ignored) {
                }
            }
            return;
        }

        // --- Stash-GUI (Owner) ---
        if (ShopStashGui.isStash(e.getView().title())) {
            int slot = e.getRawSlot();

            // --- Toggle verkaufen/kaufen ---
            if (slot == 43 || slot == 44 || slot == 40 || slot == 39 || slot == 41 || slot == 42) {
                e.setCancelled(true);

                String key = ShopStashGui.OPEN.get(p.getUniqueId());
                Optional<PlayerShop> shopOpt = Optional.empty();
                if (key != null) shopOpt = plugin.shops().getByKey(key);
                if (shopOpt.isEmpty()) {
                    PlayerShop fallback = nearestShopToViewer(p);
                    if (fallback == null) return;
                    shopOpt = Optional.of(fallback);
                }
                PlayerShop shop = shopOpt.get();

                if (slot == 43) {
                    shop.setSellEnabled(!shop.isSellEnabled());

                    ItemStack toggleSell = new ItemStack(shop.isSellEnabled() ? Material.EMERALD_BLOCK : Material.REDSTONE_BLOCK);
                    ItemMeta sm = toggleSell.getItemMeta();
                    if (sm != null) {
                        sm.displayName(MiniMessage.miniMessage().deserialize("<#ffc900>Verkaufsstatus ändern").decoration(TextDecoration.ITALIC,false));
                        sm.lore(List.of(
                                MiniMessage.miniMessage().deserialize((shop.isSellEnabled() ? "<#1fff17>Aktiviert" : "<#ff1717>Deaktiviert")).decoration(TextDecoration.ITALIC,false)
                        ));
                    }
                    toggleSell.setItemMeta(sm);
                    e.getInventory().setItem(43, toggleSell);

                    plugin.entities().updateLabel(shop);
                    try {
                        plugin.storage().saveAll();
                    } catch (Exception ignored) {
                    }
                } else if (slot == 44) {
                    shop.setBuyEnabled(!shop.isBuyEnabled());

                    ItemStack toggleBuy = new ItemStack(shop.isBuyEnabled() ? Material.EMERALD_BLOCK : Material.REDSTONE_BLOCK);
                    ItemMeta bm = toggleBuy.getItemMeta();
                    if (bm != null) {
                        bm.displayName(MiniMessage.miniMessage().deserialize("<#ffc900>Kaufsstatus ändern").decoration(TextDecoration.ITALIC,false));
                        bm.lore(List.of(
                                MiniMessage.miniMessage().deserialize(shop.isBuyEnabled() ? "<#1fff17>Aktiviert" : "<#ff1717>Deaktiviert").decoration(TextDecoration.ITALIC,false))
                        );
                    }
                    toggleBuy.setItemMeta(bm);
                    e.getInventory().setItem(44, toggleBuy);

                    plugin.entities().updateLabel(shop);
                    try {
                        plugin.storage().saveAll();
                    } catch (Exception ignored) {
                    }
                }
                return;
            }

            if (slot == 36 || slot == 37 || slot == 38) {
                e.setCancelled(true);

                String key = ShopStashGui.OPEN.get(p.getUniqueId());
                if (key == null) return;
                var shopOpt = plugin.shops().getByKey(key);
                if (shopOpt.isEmpty()) return;
                PlayerShop shop = shopOpt.get();

                if (slot == 36) {
                    p.closeInventory();
                    p.sendMessage(Main.prefix().append(MiniMessage.miniMessage().deserialize("<gray>Bitte gib einen neuen <#a3ff2b>Verkaufspreis<gray> im Chat ein.")));
                    new ChatInputHandler(p, input -> {
                        try {
                            double price = Double.parseDouble(input);
                            shop.setSellPrice(price);
                            p.sendMessage(Main.prefix().append(MiniMessage.miniMessage().deserialize("<#a3ff2b>Verkaufspreis geändert zu <dark_gray>→ <#a3ff2b>" + price)));
                            plugin.entities().updateLabel(shop);
                            try {
                                plugin.storage().saveAll();
                            } catch (Exception ignored) {
                            }
                        } catch (NumberFormatException ex) {
                            p.sendMessage(Main.prefix().append(MiniMessage.miniMessage().deserialize("<#ff1717>Ungültige Zahl!")));
                        }
                        Bukkit.getScheduler().runTask(plugin, () -> new ShopStashGui(plugin, shop).openFor(p));
                    });
                }

                if (slot == 37) {
                    p.closeInventory();
                    p.sendMessage(Main.prefix().append(MiniMessage.miniMessage().deserialize(
                            "<gray>Bitte gib einen neuen <#a3ff2b>Ankaufspreis<gray> im Chat ein.")));

                    new ChatInputHandler(p, input -> {
                        Bukkit.getScheduler().runTask(plugin, () -> {
                            if (input.equalsIgnoreCase("abbrechen")) {
                                p.sendMessage(Main.prefix().append(MiniMessage.miniMessage().deserialize("<#ff1717>Eingabe abgebrochen.")));
                                new ShopStashGui(plugin, shop).openFor(p);
                                return;
                            }

                            try {
                                double price = Double.parseDouble(input);
                                shop.setBuyPrice(price);
                                p.sendMessage(Main.prefix().append(MiniMessage.miniMessage().deserialize(
                                        "<#a3ff2b>Ankaufspreis geändert zu <dark_gray>→ <#a3ff2b>" + price)));

                                plugin.entities().updateLabel(shop);
                                try {
                                    plugin.storage().saveAll();
                                } catch (Exception ignored) {}

                            } catch (NumberFormatException ex) {
                                p.sendMessage(Main.prefix().append(MiniMessage.miniMessage().deserialize("<#ff1717>Ungültige Zahl!")));
                            }
                            new ShopStashGui(plugin, shop).openFor(p);
                        });
                    });
                }
                if (slot == 38) {
                    p.closeInventory();
                    p.sendMessage(Main.prefix().append(MiniMessage.miniMessage().deserialize(
                            "<gray>Bitte gib eine neue <#a3ff2b>Verkaufsmenge<gray> im Chat ein.")));

                    new ChatInputHandler(p, input -> {
                        Bukkit.getScheduler().runTask(plugin, () -> {
                            if (input.equalsIgnoreCase("abbrechen")) {
                                p.sendMessage(Main.prefix().append(MiniMessage.miniMessage().deserialize("<#ff1717>Eingabe abgebrochen.")));
                                new ShopStashGui(plugin, shop).openFor(p);
                                return;
                            }

                            try {
                                int amount = Integer.parseInt(input);
                                shop.setAmountPerTrade(amount);
                                p.sendMessage(Main.prefix().append(MiniMessage.miniMessage().deserialize(
                                        "<#a3ff2b>Verkaufsmenge geändert zu <dark_gray>→ <#fbecab>" + amount + "x")));

                                plugin.entities().updateLabel(shop);
                                try {
                                    plugin.storage().saveAll();
                                } catch (Exception ignored) {}

                            } catch (NumberFormatException ex) {
                                p.sendMessage(Main.prefix().append(MiniMessage.miniMessage().deserialize("<#ff1717>Ungültige Zahl!")));
                            }
                            new ShopStashGui(plugin, shop).openFor(p);
                        });
                    });
                }
                return;
            }

            ItemStack clicked = e.getCurrentItem();
            if (clicked != null && (clicked.getType() == Material.EMERALD_BLOCK || clicked.getType() == Material.REDSTONE_BLOCK)) {
                e.setCancelled(true);
            }
        }
    }

    private PlayerShop nearestShopToViewer(Player p) {
        return plugin.shops().all().stream()
                .filter(s -> s.getBaseLocation().getWorld().equals(p.getWorld()))
                .filter(s -> s.getBaseLocation().distance(p.getLocation()) <= 6.0)
                .min(Comparator.comparingDouble(s -> s.getBaseLocation().distance(p.getLocation())))
                .orElse(null);
    }

    private boolean hasItems(Player p, Material mat, int amount) {
        int count = 0;
        for (ItemStack is : p.getInventory().getStorageContents()) {
            if (is != null && is.getType() == mat) count += is.getAmount();
            if (count >= amount) return true;
        }
        return false;
    }

    private void takeItems(Player p, Material mat, int amount) {
        int left = amount;
        var inv = p.getInventory();
        for (int i = 0; i < inv.getSize() && left > 0; i++) {
            ItemStack is = inv.getItem(i);
            if (is == null || is.getType() != mat) continue;
            int take = Math.min(is.getAmount(), left);
            is.setAmount(is.getAmount() - take);
            if (is.getAmount() <= 0) inv.setItem(i, null);
            left -= take;
        }
    }
}