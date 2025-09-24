package de.eindaniel.playerShops.notifications;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerJoinListener implements Listener {

    private final NotificationManager notificationManager;

    public PlayerJoinListener(NotificationManager notificationManager) {
        this.notificationManager = notificationManager;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        notificationManager.sendPendingNotifications(event.getPlayer());
    }
}