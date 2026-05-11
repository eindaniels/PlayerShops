package de.eindaniel.playerShops.entity;

import de.eindaniel.playerShops.Main;
import de.eindaniel.playerShops.shop.PlayerShop;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.*;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.Transformation;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.*;

public class ShopEntityManager {

    private final Main plugin;
    private static final MiniMessage MM = MiniMessage.miniMessage();

    private final Map<String, UUID> blockIds    = new HashMap<>();
    private final Map<String, UUID> textIds     = new HashMap<>();
    private final Map<String, UUID> interactIds = new HashMap<>();

    public ShopEntityManager(Main plugin, de.eindaniel.playerShops.shop.ShopManager manager) {
        this.plugin = plugin;
    }

    public void spawnAll() {
        despawnAll();
        for (PlayerShop s : plugin.shops().all()) spawnFor(s);
    }

    public void spawnFor(PlayerShop shop) {
        Location base = shop.getBaseLocation().clone();

        // Item Display
        ItemDisplay bd = base.getWorld().spawn(base.clone().add(0, 0.3, 0), ItemDisplay.class, d -> {
            d.setItemStack(shop.getDisplayItem());
            d.setBrightness(new Display.Brightness(15, 15));
            d.setBillboard(Display.Billboard.VERTICAL);
            d.setTransformation(new Transformation(
                    new Vector3f(0, 0, 0), new Quaternionf(),
                    new Vector3f(0.4f, 0.4f, 0.4f), new Quaternionf()
            ));
            tag(d.getPersistentDataContainer(), shop.key());
        });
        shop.setItemDisplayUUID(bd.getUniqueId());

        // Text Display
        TextDisplay td = base.getWorld().spawn(base.clone().add(0, 0.6, 0), TextDisplay.class, t -> {
            t.setBillboard(Display.Billboard.VERTICAL);
            t.setBackgroundColor(Color.fromARGB(0, 0, 0, 0));
            t.setShadowed(true);
            t.setTransformation(new Transformation(
                    new Vector3f(0, 0, 0), new Quaternionf(),
                    new Vector3f(0.4f, 0.4f, 0.4f), new Quaternionf()
            ));
            t.text(buildLabel(shop));
            tag(t.getPersistentDataContainer(), shop.key());
        });
        shop.setTextDisplayUUID(td.getUniqueId());

        // Interaction
        Interaction it = base.getWorld().spawn(base.clone(), Interaction.class, i -> {
            i.setInteractionWidth(1.4f);
            i.setInteractionHeight(1.4f);
            i.setResponsive(true);
            tag(i.getPersistentDataContainer(), shop.key());
        });
        shop.setInteractionUUID(it.getUniqueId());

        blockIds.put(shop.key(), bd.getUniqueId());
        textIds.put(shop.key(), td.getUniqueId());
        interactIds.put(shop.key(), it.getUniqueId());
    }

    public void despawnAll() {
        removeEntities(blockIds);
        removeEntities(textIds);
        removeEntities(interactIds);
        blockIds.clear();
        textIds.clear();
        interactIds.clear();
    }

    public void despawnFor(PlayerShop shop) {
        removeEntity(blockIds.remove(shop.key()));
        removeEntity(textIds.remove(shop.key()));
        removeEntity(interactIds.remove(shop.key()));
    }

    public void updateLabel(PlayerShop shop) {
        UUID id = textIds.get(shop.key());
        if (id == null) return;
        var e = plugin.getServer().getEntity(id);
        if (e instanceof TextDisplay td) td.text(buildLabel(shop));
    }

    public Optional<String> readKey(PersistentDataContainer pdc) {
        return Optional.ofNullable(pdc.get(Keys.SHOP_KEY, PersistentDataType.STRING));
    }

    private Component buildLabel(PlayerShop shop) {
        String item  = shop.getDisplayItem().translationKey();
        int    amt   = shop.getAmountPerTrade();
        String buy   = shop.isBuyEnabled()
                ? plugin.i18n().get("shop.buyEnabled",  String.format("%.2f" + plugin.config().get("economy.currency-symbol", "$"), shop.getBuyPrice()))
                : plugin.i18n().get("shop.buyDisabled", String.format("%.2f" + plugin.config().get("economy.currency-symbol", "$"), shop.getBuyPrice()));
        String sell  = shop.isSellEnabled()
                ? plugin.i18n().get("shop.sellEnabled",  String.format("%.2f" + plugin.config().get("economy.currency-symbol", "$"), shop.getSellPrice()))
                : plugin.i18n().get("shop.sellDisabled", String.format("%.2f" + plugin.config().get("economy.currency-symbol", "$"), shop.getSellPrice()));

        String label = plugin.i18n().get("shop.label", amt, item, buy, sell);
        return MM.deserialize(label);
    }

    private void tag(PersistentDataContainer pdc, String key) {
        pdc.set(Keys.SHOP_KEY, PersistentDataType.STRING, key);
    }

    private void removeEntities(Map<String, UUID> map) {
        map.values().forEach(this::removeEntity);
    }

    private void removeEntity(UUID id) {
        if (id == null) return;
        var e = plugin.getServer().getEntity(id);
        if (e != null) e.remove();
    }

    public static final class Keys {
        public static final org.bukkit.NamespacedKey SHOP_KEY =
                new org.bukkit.NamespacedKey(Main.get(), "shop_key");
    }
}
