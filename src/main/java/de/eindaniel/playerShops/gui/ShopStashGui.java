package de.eindaniel.playerShops.gui;

import com.mojang.brigadier.Message;
import de.eindaniel.playerShops.Main;
import de.eindaniel.playerShops.shop.PlayerShop;
import de.eindaniel.playerShops.util.GuiTitleUtil;
import de.eindaniel.playerShops.util.ItemSerializer;
import de.eindaniel.playerShops.util.MessageUtils;
import de.eindaniel.playerShops.util.MessageUtilsSingleton;
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

    private final Main plugin;
    private final PlayerShop shop;

    public static final Map<UUID, String> OPEN = new ConcurrentHashMap<>();

    public ShopStashGui(Main plugin, PlayerShop shop) {
        this.plugin = plugin;
        this.shop = shop;
    }

    public Inventory build(Player player) {
        MessageUtils messageUtils = MessageUtilsSingleton.getInstance();
        Inventory inv = GuiTitleUtil.createCenteredInventory(45, messageUtils.getLocalizedMessageStringNoPrefix(player, "shopStashGui.title"));

        int idx = 0;
        for (ItemStack is : shop.getStashItems()) {
            if (is == null) continue;
            if (idx > 35) break;
            inv.setItem(idx++, is.clone());
        }

        ItemStack toggleSell = new ItemStack(shop.isSellEnabled() ? Material.EMERALD_BLOCK : Material.REDSTONE_BLOCK);
        var sm = toggleSell.getItemMeta();
        sm.displayName(messageUtils.getLocalizedMessagComponentNoPrefix(player, "shopStashGui.sellStatus").decoration(TextDecoration.ITALIC,false));
        sm.lore(List.of(
                (shop.isSellEnabled() ? messageUtils.getLocalizedMessagComponentNoPrefix(player, "shopStashGui.enabled") : messageUtils.getLocalizedMessagComponentNoPrefix(player, "shopStashGui.disabled")).decoration(TextDecoration.ITALIC,false)
        ));
        toggleSell.setItemMeta(sm);
        inv.setItem(43, toggleSell);

        ItemStack toggleBuy = new ItemStack(shop.isBuyEnabled() ? Material.EMERALD_BLOCK : Material.REDSTONE_BLOCK);
        var bm = toggleBuy.getItemMeta();
        bm.displayName(messageUtils.getLocalizedMessagComponentNoPrefix(player, "shopStashGui.buyStatus").decoration(TextDecoration.ITALIC,false));
        bm.lore(List.of(
                (shop.isBuyEnabled() ? messageUtils.getLocalizedMessagComponentNoPrefix(player, "shopStashGui.enabled") : messageUtils.getLocalizedMessagComponentNoPrefix(player, "shopStashGui.disabled")).decoration(TextDecoration.ITALIC,false)
        ));
        toggleBuy.setItemMeta(bm);
        inv.setItem(44, toggleBuy);

        ItemStack changeSellPrice = new ItemStack(Material.NAME_TAG);
        var spm = changeSellPrice.getItemMeta();
        spm.displayName(messageUtils.getLocalizedMessagComponentNoPrefix(player, "shopStashGui.changeSellPrice").decoration(TextDecoration.ITALIC,false));
        spm.lore(List.of(
                messageUtils.getLocalizedMessage(player, "shopStashGui.currentSellPrice", shop.getSellPrice()).decoration(TextDecoration.ITALIC,false)
        ));
        changeSellPrice.setItemMeta(spm);
        inv.setItem(36, changeSellPrice);

        ItemStack changeBuyPrice = new ItemStack(Material.NAME_TAG);
        var bpm = changeBuyPrice.getItemMeta();
        bpm.displayName(messageUtils.getLocalizedMessagComponentNoPrefix(player, "shopStashGui.changeBuyPrice").decoration(TextDecoration.ITALIC,false));
        bpm.lore(List.of(
                messageUtils.getLocalizedMessagComponentNoPrefix(player, "shopStashGui.currentBuyPrice", shop.getBuyPrice()).decoration(TextDecoration.ITALIC,false)
        ));
        changeBuyPrice.setItemMeta(bpm);
        inv.setItem(37, changeBuyPrice);

        ItemStack changeAmount = new ItemStack(Material.NAME_TAG);
        var am = changeAmount.getItemMeta();
        am.displayName(messageUtils.getLocalizedMessagComponentNoPrefix(player, "shopStashGui.changeAmount").decoration(TextDecoration.ITALIC,false));
        am.lore(List.of(
                messageUtils.getLocalizedMessagComponentNoPrefix(player, "shopStashGui.currentAmount", shop.getAmountPerTrade()).decoration(TextDecoration.ITALIC,false)
        ));
        changeAmount.setItemMeta(am);
        inv.setItem(38, changeAmount);

        return inv;
    }

    public void openFor(Player player) {
        OPEN.put(player.getUniqueId(), shop.key());
        player.openInventory(build(player));
    }

    public static boolean isStash(Component title, Player player) {
        if (title == null) return false;
        String raw = GuiTitleUtil.getRawTitle(title); // entfernt Padding
        MessageUtils messageUtils = MessageUtilsSingleton.getInstance();
        return raw.contains(messageUtils.getLocalizedMessageStringNoPrefix(player, "shopStashGui.title"));
    }

    private String fmt(double v) { return String.format("%.2f€", v); }

    public static void saveBack(PlayerShop shop, Inventory inv, Player player) {
        List<ItemStack> collected = new ArrayList<>();
        MessageUtils messageUtils = MessageUtilsSingleton.getInstance();
        for (int i = 0; i <= 35; i++) {
            ItemStack is = inv.getItem(i);
            if (is == null || is.getType() == Material.AIR) continue;

            if (!is.isSimilar(shop.getDisplayItem())) {
                var leftover = player.getInventory().addItem(is.clone());
                leftover.values().forEach(item ->
                        player.getWorld().dropItemNaturally(player.getLocation(), item)
                );
                messageUtils.sendLocalizedMessage(player, "shopStashGui.wrongItem");
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
