package de.eindaniel.playerShops.listener;

import de.eindaniel.playerShops.Main;
import de.eindaniel.playerShops.exceptions.StashFullException;
import de.eindaniel.playerShops.gui.ShopGui;
import de.eindaniel.playerShops.gui.ShopStashGui;
import de.eindaniel.playerShops.shop.PlayerShop;
import de.eindaniel.playerShops.util.ChatInputHandler;
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

import java.util.Comparator;
import java.util.Optional;
import java.util.UUID;

public class InteractionListener implements Listener {

    private final Main plugin;
    private static final MiniMessage MM = MiniMessage.miniMessage();

    public InteractionListener(Main plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInteract(PlayerInteractEntityEvent e) {
        if (!(e.getRightClicked() instanceof Interaction it)) return;

        Optional<String> keyOpt = plugin.entities().readKey(it.getPersistentDataContainer());
        if (keyOpt.isEmpty()) return;

        Optional<PlayerShop> shopOpt = plugin.shops().getByKey(keyOpt.get());
        if (shopOpt.isEmpty()) return;

        e.setCancelled(true);
        PlayerShop shop = shopOpt.get();
        Player p = e.getPlayer();

        if (shop.getOwner().equals(p.getUniqueId())) {
            new ShopStashGui(plugin, shop).openFor(p);
        } else {
            p.openInventory(new ShopGui(shop, plugin).build());
        }
    }

    @EventHandler
    public void onClose(InventoryCloseEvent e) {
        if (!(e.getPlayer() instanceof Player p)) return;
        if (!ShopStashGui.isStash(e.getView().title())) return;

        String key = ShopStashGui.OPEN.remove(p.getUniqueId());
        if (key == null) return;

        plugin.shops().getByKey(key).ifPresent(shop -> {
            ShopStashGui.saveBack(shop, e.getInventory(), p);
            plugin.entities().updateLabel(shop);
            shop.updateDisplay();
            try { plugin.storage().saveAll(); } catch (Exception ignored) {}
        });
    }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player p)) return;

        // ---- Shop-GUI (Kauf / Verkauf für andere Spieler) ----
        if (ShopGui.isShop(e.getView().title())) {
            e.setCancelled(true);

            PlayerShop shop = nearestShop(p);
            if (shop == null) { p.closeInventory(); return; }

            if (shop.getOwner().equals(p.getUniqueId())) {
                p.sendMessage(Main.prefix().append(MM.deserialize(plugin.i18n().get("interaction.ownShop"))));
                return;
            }

            int slot = e.getRawSlot();
            if (slot == 11 && shop.isBuyEnabled()) handleBuy(p, shop);
            else if (slot == 15 && shop.isSellEnabled()) handleSell(p, shop);
            return;
        }

        // ---- Stash-GUI (Owner) ----
        if (ShopStashGui.isStash(e.getView().title())) {
            int slot = e.getRawSlot();

            if (slot >= 36 && slot <= 44) {
                e.setCancelled(true);
                String key = ShopStashGui.OPEN.get(p.getUniqueId());
                if (key == null) return;
                plugin.shops().getByKey(key).ifPresent(shop -> handleStashControl(p, shop, slot));
                return;
            }

            ItemStack clicked = e.getCurrentItem();
            if (clicked != null && (clicked.getType() == Material.EMERALD_BLOCK
                    || clicked.getType() == Material.REDSTONE_BLOCK
                    || clicked.getType() == Material.NAME_TAG)) {
                e.setCancelled(true);
            }
        }
    }


    private void handleBuy(Player p, PlayerShop shop) {
        int need = shop.getAmountPerTrade();
        if (shop.countStashMaterial() < need) {
            p.sendMessage(Main.prefix().append(MM.deserialize(plugin.i18n().get("interaction.emptyStash"))));
            return;
        }
        double price = shop.getBuyPrice();
        if (!plugin.vault().has(p, price)) {
            p.sendMessage(Main.prefix().append(MM.deserialize(plugin.i18n().get("interaction.notEnoughMoney"))));
            return;
        }
        if (!plugin.vault().withdraw(p, price)) {
            p.sendMessage(Main.prefix().append(MM.deserialize(plugin.i18n().get("interaction.ecoError"))));
            return;
        }

        OfflinePlayer owner = plugin.getServer().getOfflinePlayer(shop.getOwner());
        plugin.vault().deposit(owner, price);

        int taken = shop.takeFromStash(need);
        ItemStack give = shop.getDisplayItem().clone();
        give.setAmount(taken);
        var leftover = p.getInventory().addItem(give);
        leftover.values().forEach(item -> p.getWorld().dropItemNaturally(p.getLocation(), item));

        p.sendMessage(Main.prefix().append(MM.deserialize(plugin.i18n().get("interaction.transactionSuccess"))));
        p.sendMessage(Main.prefix().append(MM.deserialize(plugin.i18n().get("interaction.gotBlocks", taken, shop.getDisplayItem().translationKey()))));
        p.sendMessage(Main.prefix().append(MM.deserialize(plugin.i18n().get("interaction.tookMoney", String.format("%.2f" + plugin.config().get("economy.currency-symbol", "$"), price)))));

        plugin.notifications().notifyShopOwner(shop.getOwner(),
                MM.deserialize(plugin.i18n().get("interaction.notificationBuy", p.getName(), String.format("%.2f" + plugin.config().get("economy.currency-symbol", "$"), price))));

        updateAndSave(shop);
    }

    private void handleSell(Player p, PlayerShop shop) {
        int need = shop.getAmountPerTrade();
        if (!hasItems(p, shop.getDisplayItem(), need)) {
            p.sendMessage(Main.prefix().append(MM.deserialize(
                    plugin.i18n().get("interaction.notEnoughBlocks", shop.getDisplayItem().translationKey()))));
            return;
        }

        double price = shop.getSellPrice();
        OfflinePlayer owner = plugin.getServer().getOfflinePlayer(shop.getOwner());

        if (!plugin.vault().has(owner, price)) {
            p.sendMessage(Main.prefix().append(MM.deserialize(plugin.i18n().get("interaction.noMoneyOwner"))));
            return;
        }

        try {
            shop.addToStash(need);
        } catch (IllegalStateException | StashFullException ex) {
            p.sendMessage(Main.prefix().append(MM.deserialize(plugin.i18n().get("interaction.stashFull"))));
            return;
        }

        if (!plugin.vault().withdraw(owner, price)) {
            p.sendMessage(Main.prefix().append(MM.deserialize(plugin.i18n().get("interaction.ecoError2"))));
            return;
        }
        if (!plugin.vault().deposit(p, price)) {
            plugin.vault().deposit(owner, price); // Rollback
            p.sendMessage(Main.prefix().append(MM.deserialize(plugin.i18n().get("interaction.ecoError2"))));
            return;
        }

        takeItems(p, shop.getDisplayItem(), need);

        p.sendMessage(Main.prefix().append(MM.deserialize(plugin.i18n().get("interaction.transactionSuccess"))));
        p.sendMessage(Main.prefix().append(MM.deserialize(plugin.i18n().get("interaction.tookBlocks", need, shop.getDisplayItem().translationKey()))));
        p.sendMessage(Main.prefix().append(MM.deserialize(plugin.i18n().get("interaction.gotMoney", String.format("%.2f" + plugin.config().get("economy.currency-symbol", "$"), price)))));

        plugin.notifications().notifyShopOwner(shop.getOwner(),
                MM.deserialize(plugin.i18n().get("interaction.notificationSell", p.getName(), String.format("%.2f" + plugin.config().get("economy.currency-symbol", "$"), price))));

        updateAndSave(shop);
    }

    private void handleStashControl(Player p, PlayerShop shop, int slot) {
        switch (slot) {
            case 43 -> { // Toggle Sell
                shop.setSellEnabled(!shop.isSellEnabled());
                refreshStashSlot(p, shop, 43);
                updateAndSave(shop);
            }
            case 44 -> { // Toggle Buy
                shop.setBuyEnabled(!shop.isBuyEnabled());
                refreshStashSlot(p, shop, 44);
                updateAndSave(shop);
            }
            case 36 -> promptChatInput(p, shop,
                    plugin.i18n().get("interaction.chatInput.newBuyPrice"),
                    input -> {
                        try {
                            double price = Double.parseDouble(input);
                            if (price < 0) throw new NumberFormatException();
                            shop.setSellPrice(price);
                            p.sendMessage(Main.prefix().append(MM.deserialize(
                                    plugin.i18n().get("interaction.chatInput.changedBuyPrice", price))));
                            updateAndSave(shop);
                        } catch (NumberFormatException ex) {
                            p.sendMessage(Main.prefix().append(MM.deserialize(plugin.i18n().get("interaction.chatInput.wrongInt"))));
                        }
                        Bukkit.getScheduler().runTask(plugin, () -> new ShopStashGui(plugin, shop).openFor(p));
                    })
            ;
            case 37 -> promptChatInput(p, shop,
                    plugin.i18n().get("interaction.chatInput.newSellPrice"),
                    input -> {
                        try {
                            double price = Double.parseDouble(input);
                            if (price < 0) throw new NumberFormatException();
                            shop.setBuyPrice(price);
                            p.sendMessage(Main.prefix().append(MM.deserialize(
                                    plugin.i18n().get("interaction.chatInput.changedSellPrice", price))));
                            updateAndSave(shop);
                        } catch (NumberFormatException ex) {
                            p.sendMessage(Main.prefix().append(MM.deserialize(plugin.i18n().get("interaction.chatInput.wrongInt"))));
                        }
                        Bukkit.getScheduler().runTask(plugin, () -> new ShopStashGui(plugin, shop).openFor(p));
                    })
            ;
            case 38 -> promptChatInput(p, shop,
                    plugin.i18n().get("interaction.chatInput.newAmount"),
                    input -> {
                        try {
                            int amount = Integer.parseInt(input);
                            if (amount <= 0) throw new NumberFormatException();
                            shop.setAmountPerTrade(amount);
                            p.sendMessage(Main.prefix().append(MM.deserialize(
                                    plugin.i18n().get("interaction.chatInput.changedAmount", amount))));
                            updateAndSave(shop);
                        } catch (NumberFormatException ex) {
                            p.sendMessage(Main.prefix().append(MM.deserialize(plugin.i18n().get("interaction.chatInput.wrongInt"))));
                        }
                        Bukkit.getScheduler().runTask(plugin, () -> new ShopStashGui(plugin, shop).openFor(p));
                    })
            ;
        }
    }

    private void promptChatInput(Player p, PlayerShop shop, String prompt, java.util.function.Consumer<String> callback) {
        p.closeInventory();
        p.sendMessage(Main.prefix().append(MM.deserialize(prompt)));
        new ChatInputHandler(p, callback);
    }

    private void refreshStashSlot(Player p, PlayerShop shop, int slot) {
        Inventory openInv = p.getOpenInventory().getTopInventory();
        if (slot == 43) {
            openInv.setItem(43, new ShopStashGui(plugin, shop).build().getItem(43));
        } else if (slot == 44) {
            openInv.setItem(44, new ShopStashGui(plugin, shop).build().getItem(44));
        }
    }


    private void updateAndSave(PlayerShop shop) {
        plugin.entities().updateLabel(shop);
        shop.updateDisplay();
        try { plugin.storage().saveAll(); } catch (Exception ignored) {}
    }

    private PlayerShop nearestShop(Player p) {
        return plugin.shops().all().stream()
                .filter(s -> s.getBaseLocation().getWorld().equals(p.getWorld()))
                .filter(s -> s.getBaseLocation().distance(p.getLocation()) <= 6.0)
                .min(Comparator.comparingDouble(s -> s.getBaseLocation().distance(p.getLocation())))
                .orElse(null);
    }

    private boolean hasItems(Player p, ItemStack template, int amount) {
        int count = 0;
        for (ItemStack is : p.getInventory().getStorageContents()) {
            if (is != null && is.isSimilar(template)) count += is.getAmount();
            if (count >= amount) return true;
        }
        return false;
    }

    private void takeItems(Player p, ItemStack template, int amount) {
        int left = amount;
        var inv = p.getInventory();
        for (int i = 0; i < inv.getSize() && left > 0; i++) {
            ItemStack is = inv.getItem(i);
            if (is == null || !is.isSimilar(template)) continue;
            int take = Math.min(is.getAmount(), left);
            is.setAmount(is.getAmount() - take);
            if (is.getAmount() <= 0) inv.setItem(i, null);
            left -= take;
        }
    }
}
