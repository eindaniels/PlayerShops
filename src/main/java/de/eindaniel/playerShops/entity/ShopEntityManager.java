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
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.Transformation;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.*;

public class ShopEntityManager {

    private final Main plugin;
    private final Map<String, UUID> blockIds = new HashMap<>();
    private final Map<String, UUID> textIds = new HashMap<>();
    private final Map<String, UUID> interactIds = new HashMap<>();

    private int animTask = -1;
    private float phase = 0f;

    public ShopEntityManager(Main plugin, de.eindaniel.playerShops.shop.ShopManager manager) {
        this.plugin = plugin;
    }

    public void spawnAll() {
        despawnAll();
        for (PlayerShop s : plugin.shops().all()) spawnFor(s);
    }

    public void spawnFor(PlayerShop shop) {
        Location base = shop.getBaseLocation().clone();

        ItemDisplay bd = base.getWorld().spawn(base.clone().add(0, 0.3, 0), ItemDisplay.class, d -> {
            d.setItemStack(shop.getDisplayItem());
            d.setBrightness(new Display.Brightness(15, 15));
            d.setBillboard(Display.Billboard.VERTICAL);
            d.setTransformation(new Transformation(
                    new Vector3f(0, 0, 0),
                    new Quaternionf(),
                    new Vector3f(0.4f, 0.4f, 0.4f),
                    new Quaternionf()
            ));
            tag(d.getPersistentDataContainer(), shop.key());
        });

        shop.setItemDisplayUUID(bd.getUniqueId());

        TextDisplay td = base.getWorld().spawn(base.clone().add(0, 0.6, 0), TextDisplay.class, t -> {
            t.setBillboard(Display.Billboard.CENTER);
            t.setBillboard(Display.Billboard.VERTICAL);
            t.setBackgroundColor(Color.fromARGB(0,0,0,0));
            t.setShadowed(true);
            t.setTransformation(new Transformation(
                    new Vector3f(0, 0, 0),
                    new Quaternionf(),
                    new Vector3f(0.4f, 0.4f, 0.4f),
                    new Quaternionf()
            ));
            t.text(label(shop));
            tag(t.getPersistentDataContainer(), shop.key());
        });

        shop.setTextDisplayUUID(td.getUniqueId());

        Interaction it = base.getWorld().spawn(base.clone().add(0, 0, 0), Interaction.class, i -> {
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
        for (UUID id : blockIds.values()) { var e = plugin.getServer().getEntity(id); if (e != null) e.remove(); }
        for (UUID id : textIds.values()) { var e = plugin.getServer().getEntity(id); if (e != null) e.remove(); }
        for (UUID id : interactIds.values()) { var e = plugin.getServer().getEntity(id); if (e != null) e.remove(); }
        blockIds.clear(); textIds.clear(); interactIds.clear();
    }

    public void updateLabel(PlayerShop shop) {
        UUID id = textIds.get(shop.key());
        if (id == null) return;
        var e = plugin.getServer().getEntity(id);
        if (e instanceof TextDisplay td) td.text(label(shop));
    }

    public Optional<String> readKey(PersistentDataContainer pdc) {
        return Optional.ofNullable(pdc.get(Keys.SHOP_KEY, PersistentDataType.STRING));
    }

    private void tag(PersistentDataContainer pdc, String key) {
        pdc.set(Keys.SHOP_KEY, PersistentDataType.STRING, key);
    }

    public void despawnFor(PlayerShop shop) {
        UUID bid = blockIds.remove(shop.key());
        if (bid != null) {
            var e = plugin.getServer().getEntity(bid);
            if (e != null) e.remove();
        }
        UUID tid = textIds.remove(shop.key());
        if (tid != null) {
            var e = plugin.getServer().getEntity(tid);
            if (e != null) e.remove();
        }
        UUID iid = interactIds.remove(shop.key());
        if (iid != null) {
            var e = plugin.getServer().getEntity(iid);
            if (e != null) e.remove();
        }
    }

    private net.kyori.adventure.text.Component label(PlayerShop shop) {
        String item = shop.getDisplayItem().translationKey();
        return MiniMessage.miniMessage().deserialize("<gray>" + shop.getAmountPerTrade() + "x <white><lang:" + item + ">\n" + (shop.isBuyEnabled() ? "<#fbecab>Verkauf: <#a3ff2b>" + shop.getBuyPrice() + "€" : "<#ff1717><st>Verkauf: " + shop.getBuyPrice() + "€<reset>") + "\n" + (shop.isSellEnabled() ? "<#fbecab>Ankauf: <#a3ff2b>" + shop.getSellPrice() + "€" : "<#ff1717><st>Ankauf: " + shop.getSellPrice() + "€<reset>"));
    }

    public static final class Keys {
        public static final org.bukkit.NamespacedKey SHOP_KEY =
                new org.bukkit.NamespacedKey(Main.get(), "shop_key");
    }

    public void hardDespawnAll() {
        for (World world : Bukkit.getWorlds()) {
            for (Entity entity : world.getEntities()) {
                if (entity instanceof Display || entity instanceof Interaction) {
                    // nur löschen, wenn keine Shop-NBT dran hängt
                    var pdc = entity.getPersistentDataContainer();
                    if (!pdc.has(ShopEntityManager.Keys.SHOP_KEY, PersistentDataType.STRING)) {
                        entity.remove();
                    }
                }
            }
        }
    }
}
