package de.eindaniel.playerShops;

import de.eindaniel.playerShops.commands.CreateShopCommand;
import de.eindaniel.playerShops.commands.RemoveShopCommand;
import de.eindaniel.playerShops.economy.VaultHook;
import de.eindaniel.playerShops.entity.ShopEntityManager;
import de.eindaniel.playerShops.listener.ChatInputListener;
import de.eindaniel.playerShops.listener.InteractionListener;
import de.eindaniel.playerShops.listener.ProtectionListener;
import de.eindaniel.playerShops.notifications.NotificationManager;
import de.eindaniel.playerShops.notifications.PlayerJoinListener;
import de.eindaniel.playerShops.shop.ShopManager;
import de.eindaniel.playerShops.shop.ShopStorage;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandMap;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {

    private static Main instance;

    private VaultHook vault;
    private ShopStorage storage;
    private ShopManager shopManager;
    private ShopEntityManager entityManager;
    private NotificationManager notificationManager;

    public static Main get() { return instance; }
    public VaultHook vault() { return vault; }
    public ShopStorage storage() { return storage; }
    public ShopManager shops() { return shopManager; }
    public ShopEntityManager entities() { return entityManager; }
    public NotificationManager notifications() { return notificationManager; }
    public FileConfiguration config() { return getConfig(); }


    @Override
    public void onEnable() {
        saveConfig();
        instance = this;
        notificationManager = new NotificationManager(this);

        vault = new VaultHook(this);
        if (!vault.hook()) {
            // TODO -> Check ob "useVault" true ist -> dann deaktivieren. Falls nicht nutze "currency" Feld in der Config als Item.
            getLogger().severe("Vault oder Economy fehlt. Deaktiviere Plugin.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        storage = new ShopStorage(this);
        shopManager = new ShopManager(this, storage);
        storage.createBackupIfNeeded();
        entityManager = new ShopEntityManager(this, shopManager);

        CommandMap commandMap = Bukkit.getCommandMap();
        commandMap.register("playershops", new CreateShopCommand(this));
        commandMap.register("playershops", new RemoveShopCommand(this));

        getServer().getPluginManager().registerEvents(new InteractionListener(this), this);
        getServer().getPluginManager().registerEvents(new ProtectionListener(), this);
        getServer().getPluginManager().registerEvents(new ChatInputListener(), this);
        getServer().getPluginManager().registerEvents(new PlayerJoinListener(notificationManager), this);

        entityManager.spawnAll();

        storage.loadAll();
        entityManager.spawnAll();

        getLogger().info("PlayerShops aktiviert.");
    }

    @Override
    public void onDisable() {
        try {
            entityManager.despawnAll();
            storage.saveAll();
        } catch (Exception e) {
            e.printStackTrace();
        }
        getLogger().info("PlayerShops deaktiviert.");
    }

    public static Component prefix() {
        Component prefix = MiniMessage.miniMessage().deserialize("<dark_gray>[<#ffdd00>Spielershops<dark_gray>] <reset>");
        return prefix;
    }
}
