package de.eindaniel.playerShops.gui;

import de.eindaniel.playerShops.Main;
import de.eindaniel.playerShops.shop.PlayerShop;
import de.eindaniel.playerShops.util.GuiTitleUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class ShopGui {

    private static final MiniMessage MM = MiniMessage.miniMessage();

    private final PlayerShop shop;

    public ShopGui(PlayerShop shop) {
        this.shop = shop;
    }

    public Inventory build() {
        String title = Main.get().i18n().get("shopGui.title");
        Inventory inv = GuiTitleUtil.createCenteredInventory(27, title);

        ItemStack core = shop.getDisplayItem().clone();
        var cm = core.getItemMeta();
        String itemLabel = Main.get().i18n().get("shopGui.selledItem",
                shop.getAmountPerTrade(), shop.getDisplayItem().translationKey());
        cm.displayName(MM.deserialize(itemLabel).decoration(TextDecoration.ITALIC, false));
        core.setItemMeta(cm);
        inv.setItem(13, core);

        if (shop.isBuyEnabled()) {
            ItemStack buy = new ItemStack(Material.LIME_WOOL);
            var bm = buy.getItemMeta();
            bm.displayName(MM.deserialize(Main.get().i18n().get("shopGui.buyItem.title"))
                    .decoration(TextDecoration.ITALIC, false));
            bm.lore(List.of(MM.deserialize(Main.get().i18n().get("shopGui.buyItem.lore",
                    String.format("%.2f€", shop.getBuyPrice())))
                    .decoration(TextDecoration.ITALIC, false)));
            buy.setItemMeta(bm);
            inv.setItem(11, buy);
        }

        if (shop.isSellEnabled()) {
            ItemStack sell = new ItemStack(Material.RED_WOOL);
            var sm = sell.getItemMeta();
            sm.displayName(MM.deserialize(Main.get().i18n().get("shopGui.sellItem.title"))
                    .decoration(TextDecoration.ITALIC, false));
            sm.lore(List.of(MM.deserialize(Main.get().i18n().get("shopGui.sellItem.lore",
                    String.format("%.2f€", shop.getSellPrice())))
                    .decoration(TextDecoration.ITALIC, false)));
            sell.setItemMeta(sm);
            inv.setItem(15, sell);
        }

        return inv;
    }

    public PlayerShop getShop() { return shop; }

    public static boolean isShop(Component title) {
        if (title == null) return false;
        return GuiTitleUtil.getRawTitle(title).contains(
                Main.get().i18n().get("shopGui.title"));
    }
}
