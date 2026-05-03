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
    private final Consumer<String> inputConsumer;

    public ChatInputHandler(Player player, Consumer<String> inputConsumer) {
        this.player = player;
        this.inputConsumer = inputConsumer;
        this.endTime = System.currentTimeMillis() + 30_000L;

        handlers.put(player.getUniqueId(), this);

        player.sendMessage(Main.prefix().append(MiniMessage.miniMessage().deserialize(
                "<gray>Schreibe \"<#fbecab>abbrechen<gray>\" oder \"<#fbecab>cancel<gray>\" um abzubrechen.")));

        startTitleTask();
    }

    private void startTitleTask() {
        taskId = new BukkitRunnable() {
            @Override
            public void run() {
                long remaining = (endTime - System.currentTimeMillis()) / 1000;
                if (remaining <= 0) {
                    player.sendTitle("", "", 0, 0, 0);
                    ChatInputHandler.removeHandler(player);
                    cancel();
                    return;
                }
                String title = toLegacy(Main.get().i18n().get("chatInput.title"));
                String subtitle = toLegacy(Main.get().i18n().get("chatInput.subtitle", remaining));
                player.sendTitle(title, subtitle, 0, 20, 10);
            }
        }.runTaskTimer(Main.get(), 0, 20).getTaskId();
    }

    private String toLegacy(String miniMsg) {
        return LegacyComponentSerializer.legacySection()
                .serialize(MiniMessage.miniMessage().deserialize(miniMsg));
    }

    public static ChatInputHandler getHandler(Player player) {
        return handlers.get(player.getUniqueId());
    }

    public static void removeHandler(Player player) {
        ChatInputHandler h = handlers.remove(player.getUniqueId());
        if (h != null) {
            Bukkit.getScheduler().cancelTask(h.taskId);
            player.sendTitle("", "", 0, 0, 0);
        }
    }

    public void handleInput(String input) {
        removeHandler(player);
        Bukkit.getScheduler().runTask(Main.get(), () -> inputConsumer.accept(input));
    }
}
