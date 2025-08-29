package de.eindaniel.playerShops.util;

import de.eindaniel.playerShops.Main;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

public class ChatInputHandler {
    private static final Map<UUID, ChatInputHandler> handlers = new HashMap<>();
    private final Player player;
    private final long endTime;
    private int taskId;
    private final Consumer<String> inputConsumer; // Generischer Callback

    public ChatInputHandler(Player player, Consumer<String> inputConsumer) {
        this.player = player;
        this.inputConsumer = inputConsumer;
        this.endTime = System.currentTimeMillis() + 30000; // 30 Sekunden
        handlers.put(player.getUniqueId(), this);
        player.sendMessage(Main.prefix().append(MiniMessage.miniMessage().deserialize("<gray>Schreibe \"<#fbecab>abbrechen<gray>\" oder \"<#fbecab>cancel<gray>\" um den Vorgang abzubrechen.")));
        startTitleTask();
    }

    private void startTitleTask() {
        taskId = new BukkitRunnable() {
            @Override
            public void run() {
                long remainingTime = (endTime - System.currentTimeMillis()) / 1000;
                if (remainingTime <= 0) {
                    player.sendTitle("", "", 0, 0, 0);
                    cancel();
                    return;
                }
                final String title = LegacyComponentSerializer.legacyAmpersand()
                        .serialize(MiniMessage.miniMessage().deserialize("<#FCC500>Chateingabe"))
                        .replace("&", "§");
                final String subtitle = LegacyComponentSerializer.legacyAmpersand()
                        .serialize(MiniMessage.miniMessage().deserialize("<#fbecab>" + remainingTime + " Sekunden"))
                        .replace("&", "§");
                player.sendTitle(title, subtitle, 0, 20, 10);
            }
        }.runTaskTimer(Main.getPlugin(Main.class), 0, 20).getTaskId();
    }

    public static ChatInputHandler getHandler(Player player) {
        return handlers.get(player.getUniqueId());
    }

    public static void removeHandler(Player player) {
        ChatInputHandler handler = handlers.remove(player.getUniqueId());
        if (handler != null) {
            Bukkit.getScheduler().cancelTask(handler.taskId);
            player.sendTitle("", "", 0, 0, 0);
        }
    }

    public boolean isExpired() {
        return System.currentTimeMillis() > endTime;
    }

    public void setExpired() {
        handlers.remove(player.getUniqueId());
        Bukkit.getScheduler().cancelTask(taskId);
        player.sendTitle("", "", 0, 0, 0);
    }

    public Player getPlayer() {
        return player;
    }

    public void handleInput(String input) {
        removeHandler(player);

        Bukkit.getScheduler().runTask(Main.getPlugin(Main.class), () -> {
            inputConsumer.accept(input);
        });
    }

}
