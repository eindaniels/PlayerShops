package de.eindaniel.playerShops.gui;

import de.eindaniel.playerShops.Main;
import de.eindaniel.playerShops.shop.PlayerShop;
import de.eindaniel.playerShops.util.ItemSerializer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.*;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ShopStashGui {

    public static final String TITLE = "Shop Lager";

    private final Main plugin;
    private final PlayerShop shop;

    public static final Map<UUID, String> OPEN = new ConcurrentHashMap<>();

    public ShopStashGui(Main plugin, PlayerShop shop) {
        this.plugin = plugin;
        this.shop = shop;
    }

    public Inventory build() {
        Inventory inv = Bukkit.createInventory(null, 45, Component.text(TITLE));

        int idx = 0;
        for (ItemStack is : shop.getStashItems()) {
            if (is == null) continue;
            if (idx > 35) break;
            inv.setItem(idx++, is.clone());
        }

        ItemStack info = new ItemStack(Material.PAPER);
        var im = info.getItemMeta();
        im.displayName(MiniMessage.miniMessage().deserialize("<#fbecab>ℹ Shop Informationen").decoration(TextDecoration.ITALIC,false));
        Component item = MiniMessage.miniMessage().deserialize("<gray>Item <dark_gray>→ <#fbecab><lang:" + shop.getMaterial().translationKey() + ">");
        Component menge = MiniMessage.miniMessage().deserialize("<gray>Menge/Trade <dark_gray>→ <#fbecab>" + shop.getAmountPerTrade() + "x");
        Component verkauf = MiniMessage.miniMessage().deserialize("<gray>Verkauf (Kaufen) <dark_gray>→ <#fbecab>" + fmt(shop.getBuyPrice()));
        Component ankauf = MiniMessage.miniMessage().deserialize("<gray>Ankauf (Verkaufen) <dark_gray>→ <#fbecab>" + fmt(shop.getSellPrice()));
        im.lore(List.of(
                item.decoration(TextDecoration.ITALIC,false),
                menge.decoration(TextDecoration.ITALIC,false),
                verkauf.decoration(TextDecoration.ITALIC,false),
                ankauf.decoration(TextDecoration.ITALIC,false)
        ));
        info.setItemMeta(im);
        inv.setItem(40, info);

        ItemStack toggleSell = new ItemStack(shop.isSellEnabled() ? Material.EMERALD_BLOCK : Material.REDSTONE_BLOCK);
        var sm = toggleSell.getItemMeta();
        sm.displayName(MiniMessage.miniMessage().deserialize("<#fbecab>Verkaufen an Shop: " + (shop.isSellEnabled() ? "<#1fff17>Aktiviert" : "<#ff1717>Deaktiviert")).decoration(TextDecoration.ITALIC,false));
        toggleSell.setItemMeta(sm);
        inv.setItem(43, toggleSell);

        ItemStack toggleBuy = new ItemStack(shop.isBuyEnabled() ? Material.EMERALD_BLOCK : Material.REDSTONE_BLOCK);
        var bm = toggleBuy.getItemMeta();
        bm.displayName(MiniMessage.miniMessage().deserialize("<#fbecab>Verkaufen an Shop: " + (shop.isBuyEnabled() ? "<#1fff17>Aktiviert" : "<#ff1717>Deaktiviert")).decoration(TextDecoration.ITALIC,false));
        toggleBuy.setItemMeta(bm);
        inv.setItem(44, toggleBuy);

        return inv;
    }

    public void openFor(Player player) {
        OPEN.put(player.getUniqueId(), shop.key());
        player.openInventory(build());
    }

    public static boolean isStash(Component title) {
        return title != null && Component.text(TITLE).equals(title);
    }

    private String fmt(double v) { return String.format("%.2f€", v); }

    public static void saveBack(PlayerShop shop, Inventory inv, Player player) {
        List<ItemStack> collected = new ArrayList<>();

        for (int i = 0; i <= 35; i++) {
            ItemStack is = inv.getItem(i);
            if (is == null || is.getType() == Material.AIR) continue;

            if (is.getType() != shop.getMaterial()) {
                var leftover = player.getInventory().addItem(is.clone());
                leftover.values().forEach(item ->
                        player.getWorld().dropItemNaturally(player.getLocation(), item)
                );
                player.sendMessage(MiniMessage.miniMessage().deserialize("<#ff1717>Dieses Item gehört nicht in dem Lager und wurde dir zurückgegeben!"));
                continue;
            }

            collected.add(is.clone());
        }

        shop.getStashItems().clear();
        shop.getStashItems().addAll(collected);
    }

    public static List<String> serializeStash(PlayerShop shop) {
        List<String> out = new ArrayList<>();
        for (ItemStack is : shop.getStashItems()) {
            try {
                out.add(ItemSerializer.itemToBase64(is));
            } catch (Exception ex) {
                pluginLogWarn("Failed serializing item in stash: " + ex.getMessage());
            }
        }
        return out;
    }

    private static void pluginLogWarn(String s) {
        try {
            Main.get().getLogger().warning(s);
        } catch (Throwable ignored) {}
    }
}
