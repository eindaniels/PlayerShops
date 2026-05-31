package de.eindaniel.playerShops.gui;

import de.eindaniel.playerShops.Main;
import de.eindaniel.playerShops.shop.PlayerShop;
import de.eindaniel.playerShops.util.GuiTitleUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ShopStashGui implements InventoryHolder {

    private static final MiniMessage MM = MiniMessage.miniMessage();

    private final Main plugin;
    private final PlayerShop shop;
    private final Inventory inv;

    public ShopStashGui(Main plugin, PlayerShop shop) {
        this.plugin = plugin;
        this.shop = shop;
        this.inv = this.build();
    }

    public Inventory build() {
        String title = plugin.i18n().get("shopStashGui.title");
        Inventory inv = GuiTitleUtil.createCenteredInventory(this, 45, title);

        // Stash-Items (Slots 0–35)
        int idx = 0;
        for (ItemStack is : shop.getStashItems()) {
            if (is == null || idx > 35) continue;
            inv.setItem(idx++, is.clone());
        }

        // Toggle Sell (Slot 43)
        inv.setItem(43, buildToggle(
                shop.isSellEnabled(),
                plugin.i18n().get("shopStashGui.sellStatus"),
                plugin.i18n().get(shop.isSellEnabled() ? "shopStashGui.enabled" : "shopStashGui.disabled")
        ));

        // Toggle Buy (Slot 44)
        inv.setItem(44, buildToggle(
                shop.isBuyEnabled(),
                plugin.i18n().get("shopStashGui.buyStatus"),
                plugin.i18n().get(shop.isBuyEnabled() ? "shopStashGui.enabled" : "shopStashGui.disabled")
        ));

        // Change Sell Price (Slot 36)
        inv.setItem(36, buildNameTag(
                plugin.i18n().get("shopStashGui.changeSellPrice"),
                plugin.i18n().get("shopStashGui.currentSellPrice", String.format("%.2f", shop.getSellPrice()))
        ));

        // Change Buy Price (Slot 37)
        inv.setItem(37, buildNameTag(
                plugin.i18n().get("shopStashGui.changeBuyPrice"),
                plugin.i18n().get("shopStashGui.currentBuyPrice", String.format("%.2f", shop.getBuyPrice()))
        ));

        // Change Amount (Slot 38)
        inv.setItem(38, buildNameTag(
                plugin.i18n().get("shopStashGui.changeAmount"),
                plugin.i18n().get("shopStashGui.currentAmount", shop.getAmountPerTrade())
        ));

        return inv;
    }

    public void openFor(Player player) {
        player.openInventory(this.inv);
    }

    /**
     * @deprecated Checking for inventory names is highly insecure and thus prone to exploits.
     * Use <code>instanceof</code> instead
     * @param title the inventory's title
     * @return if the title is the pre-defined shop stash title
     */
    @Deprecated
    public static boolean isStash(Component title) {
        if (title == null) return false;
        return GuiTitleUtil.getRawTitle(title).contains(
                Main.get().i18n().get("shopStashGui.title"));
    }

    public static void saveBack(PlayerShop shop, Inventory inv, Player player) {
        List<ItemStack> collected = new ArrayList<>();

        for (int i = 0; i <= 35; i++) {
            ItemStack is = inv.getItem(i);
            if (is == null || is.getType() == Material.AIR) continue;

            if (!is.isSimilar(shop.getDisplayItem())) {
                var leftover = player.getInventory().addItem(is.clone());
                leftover.values().forEach(item ->
                        player.getWorld().dropItemNaturally(player.getLocation(), item));
                player.sendMessage(Main.prefix().append(
                        MiniMessage.miniMessage().deserialize(
                                Main.get().i18n().get("shopStashGui.wrongItem"))));
                continue;
            }
            collected.add(is.clone());
        }

        shop.getStashItems().clear();
        shop.getStashItems().addAll(collected);
    }

    private ItemStack buildToggle(boolean enabled, String displayName, String statusLore) {
        ItemStack item = new ItemStack(enabled ? Material.EMERALD_BLOCK : Material.REDSTONE_BLOCK);
        var meta = item.getItemMeta();
        meta.displayName(MM.deserialize(displayName).decoration(TextDecoration.ITALIC, false));
        meta.lore(List.of(MM.deserialize(statusLore).decoration(TextDecoration.ITALIC, false)));
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack buildNameTag(String displayName, String loreLine) {
        ItemStack item = new ItemStack(Material.NAME_TAG);
        var meta = item.getItemMeta();
        meta.displayName(MM.deserialize(displayName).decoration(TextDecoration.ITALIC, false));
        meta.lore(List.of(MM.deserialize(loreLine).decoration(TextDecoration.ITALIC, false)));
        item.setItemMeta(meta);
        return item;
    }

    @Override
    public @NotNull Inventory getInventory() {
        return this.inv;
    }

    public PlayerShop getShop() { return shop; }

}
