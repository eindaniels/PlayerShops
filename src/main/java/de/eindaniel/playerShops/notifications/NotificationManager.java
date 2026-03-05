package de.eindaniel.playerShops.notifications;

import de.eindaniel.playerShops.Main;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

public class NotificationManager {

    private final Plugin plugin;
    private final File file;
    private final YamlConfiguration config;

    public NotificationManager(Plugin plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "notifications.yml");
        this.config = YamlConfiguration.loadConfiguration(file);
    }

    public void notifyShopOwner(UUID owner, Component message) {
        Player player = Bukkit.getPlayer(owner);
        if (player != null && player.isOnline()) {
            player.sendMessage(Main.prefix().append(message));
        } else {
            // Offline: Nachricht speichern
            List<String> messages = config.getStringList(owner.toString());
            messages.add(MiniMessage.miniMessage().serialize(message));
            config.set(owner.toString(), messages);
            save();
        }
    }

    public void sendPendingNotifications(Player player) {
        UUID uuid = player.getUniqueId();
        List<String> messages = config.getStringList(uuid.toString());
        if (messages.isEmpty()) return;

        player.sendMessage(Main.prefix().append(MiniMessage.miniMessage()
                .deserialize(Main.get().i18n().get("notification.idle"))));
        for (String msg : messages) {
            player.sendMessage(Main.prefix().append(MiniMessage.miniMessage().deserialize(msg)));
        }
        config.set(uuid.toString(), null);
        save();
    }

    private void save() {
        try {
            config.save(file);
        } catch (IOException e) {
            plugin.getLogger().warning("Konnte notifications.yml nicht speichern: " + e.getMessage());
        }
    }
}
