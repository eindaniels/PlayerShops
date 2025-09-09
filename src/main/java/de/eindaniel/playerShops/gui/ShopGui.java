package de.eindaniel.playerShops.gui;

import de.eindaniel.playerShops.shop.PlayerShop;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.*;

import java.util.List;

public class ShopGui {

    public static final String TITLE = "Kauf abschließen";

    private final PlayerShop shop;

    public ShopGui(PlayerShop shop) {
        this.shop = shop;
    }

    public Inventory build() {
        Inventory inv = Bukkit.createInventory(null, 27, Component.text(TITLE));

        ItemStack core = new ItemStack(shop.getMaterial(), 1);
        var cm = core.getItemMeta();
        cm.displayName(MiniMessage.miniMessage().deserialize("<gray>" + shop.getAmountPerTrade() + "x <white><lang:" + shop.getMaterial().translationKey() + ">"));
        core.setItemMeta(cm);
        inv.setItem(13, core);

        if (shop.isBuyEnabled()) {
            ItemStack buy = new ItemStack(Material.LIME_WOOL);
            var bm = buy.getItemMeta();
            bm.displayName(MiniMessage.miniMessage().deserialize("<#ffc900>Kaufen").decoration(TextDecoration.ITALIC,false));
            bm.lore(List.of(
                    MiniMessage.miniMessage().deserialize("<gray>Ankaufspreis <dark_gray>→ <#a3ff2b>" + price(getBuyPrice())).decoration(TextDecoration.ITALIC, false)
            ));
            buy.setItemMeta(bm);
            inv.setItem(11, buy);
        }

        if (shop.isSellEnabled()) {
            ItemStack sell = new ItemStack(Material.RED_WOOL);
            var sm = sell.getItemMeta();
            sm.displayName(MiniMessage.miniMessage().deserialize("<#ffc900>Verkaufen").decoration(TextDecoration.ITALIC,false));
            sm.lore(List.of(
                    MiniMessage.miniMessage().deserialize("<gray>Verkaufspreis <dark_gray>→ <#a3ff2b>" + price(shop.getSellPrice())).decoration(TextDecoration.ITALIC,false)
            ));
            sell.setItemMeta(sm);
            inv.setItem(15, sell);
        }

        return inv;
    }

    private double getBuyPrice() {
        return shop.getBuyPrice();
    }

    private String price(double v) { return String.format("%.2f€", v); }

    public PlayerShop getShop() { return shop; }

    public static boolean isShop(Component title) {
        return title != null && Component.text(TITLE).equals(title);
    }
}
