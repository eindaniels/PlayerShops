package de.eindaniel.playerShops.notifications;

import de.eindaniel.playerShops.Main;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.configuration.file.YamlConfiguration;
import java.io.File;
import java.io.IOException;
import java.util.*;

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
            player.sendMessage(message);
        } else {
            List<String> messages = config.getStringList(owner.toString());
            messages.add(MiniMessage.miniMessage().serialize(message));
            config.set(owner.toString(), messages);
            save();
        }
    }

    public void sendPendingNotifications(Player player) {
        UUID owner = player.getUniqueId();
        List<String> messages = config.getStringList(owner.toString());
        if (!messages.isEmpty()) {
            player.sendMessage(Main.prefix().append(MiniMessage.miniMessage().deserialize("<#1fff17>Während deiner Abwesenheit hast du verdient:")));
            for (String msg : messages) {
                player.sendMessage(Main.prefix().append(MiniMessage.miniMessage().deserialize(msg)));
            }
            config.set(owner.toString(), null);
            save();
        }
    }

    private void save() {
        try {
            config.save(file);
        } catch (IOException e) {
            plugin.getLogger().warning("Konnte notifications.yml nicht speichern!");
        }
    }
}