package de.eindaniel.playerShops.listener;

import org.bukkit.entity.Display;
import org.bukkit.entity.Interaction;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;

public class ProtectionListener implements Listener {

    @EventHandler
    public void onDamage(EntityDamageEvent e) {
        if (e.getEntity() instanceof Display || e.getEntity() instanceof Interaction) {
            e.setCancelled(true);
        }
    }
}