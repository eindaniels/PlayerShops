package de.eindaniel.playerShops;

import de.eindaniel.playerShops.commands.CreateShopCommand;
import de.eindaniel.playerShops.commands.RemoveShopCommand;
import de.eindaniel.playerShops.config.Internationalization;
import de.eindaniel.playerShops.economy.VaultHook;
import de.eindaniel.playerShops.entity.ShopEntityManager;
import de.eindaniel.playerShops.listener.ChatInputListener;
import de.eindaniel.playerShops.listener.InteractionListener;
import de.eindaniel.playerShops.listener.ProtectionListener;
import de.eindaniel.playerShops.notifications.NotificationManager;
import de.eindaniel.playerShops.notifications.PlayerJoinListener;
import de.eindaniel.playerShops.shop.ShopManager;
import de.eindaniel.playerShops.shop.ShopStorage;
import de.eindaniel.playerShops.util.VersionChecker;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandMap;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {

    private static Main instance;

    private VaultHook vault;
    private ShopStorage storage;
    private ShopManager shopManager;
    private ShopEntityManager entityManager;
    private NotificationManager notificationManager;
    private Internationalization i18n;

    public static Main get() { return instance; }
    public VaultHook vault() { return vault; }
    public ShopStorage storage() { return storage; }
    public ShopManager shops() { return shopManager; }
    public ShopEntityManager entities() { return entityManager; }
    public NotificationManager notifications() { return notificationManager; }
    public Internationalization i18n() { return i18n; }
    public FileConfiguration config() { return getConfig(); }

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();

        i18n = new Internationalization(this);

        notificationManager = new NotificationManager(this);

        vault = new VaultHook(this);
        if (!vault.hook()) {
            // TODO: Config-Option "useVault: false" → Item-Währung statt Vault
            getLogger().severe("Vault oder Economy fehlt. Deaktiviere Plugin.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        new VersionChecker(this, "eindaniels", "PlayerShops").check((result, latest) -> {
            if (result == VersionChecker.Result.OUTDATED) {
                getLogger().warning("Neue Version verfügbar: " + latest);
                getLogger().warning("Download: https://github.com/eindaniels/PlayerShops/releases/latest");
            } else if (result == VersionChecker.Result.UP_TO_DATE) {
                getLogger().info("Plugin ist aktuell.");
            } else {
                getLogger().warning("Plugin-Version konnte nicht geprüft werden.");
            }
        });

        storage = new ShopStorage(this);
        shopManager = new ShopManager(this, storage);
        storage.createBackupIfNeeded();

        storage.loadAll();

        entityManager = new ShopEntityManager(this, shopManager);
        entityManager.spawnAll();

        CommandMap commandMap = Bukkit.getCommandMap();
        commandMap.register("playershops", new CreateShopCommand(this));
        commandMap.register("playershops", new RemoveShopCommand(this));

        getServer().getPluginManager().registerEvents(new InteractionListener(this), this);
        getServer().getPluginManager().registerEvents(new ProtectionListener(), this);
        getServer().getPluginManager().registerEvents(new ChatInputListener(), this);
        getServer().getPluginManager().registerEvents(new PlayerJoinListener(notificationManager), this);

        getLogger().info("PlayerShops v" + getDescription().getVersion() + " aktiviert.");
    }

    @Override
    public void onDisable() {
        try {
            if (entityManager != null) entityManager.despawnAll();
            if (storage != null) storage.saveAll();
        } catch (Exception e) {
            e.printStackTrace();
        }
        getLogger().info("PlayerShops deaktiviert.");
    }

    public static Component prefix() {
        String raw = instance != null
                ? instance.i18n().get("prefix")
                : "<dark_gray>[<#ffdd00>Spielershops<dark_gray>] <reset>";
        return MiniMessage.miniMessage().deserialize(raw);
    }
}
