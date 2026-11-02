package de.eindaniel.playerShops.commands;

import de.eindaniel.playerShops.Main;
import de.eindaniel.playerShops.util.VersionChecker;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

public class MasterCommand extends Command {
    final Main plugin;

    public MasterCommand(Main plugin) {
        super("playershops");
        this.plugin = plugin;
        setAliases(List.of("ps", "pshops"));
    }

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String @NotNull [] args) {
        if (!(sender instanceof Player player)) return true;

        if (args.length < 1) {
            player.sendMessage(Main.prefix().append(MiniMessage.miniMessage().deserialize(plugin.i18n().get("mainCommand.usage"))));
            return true;
        }

        switch (args[0]) {
            case "reload":
                try {
                    plugin.entities().despawnAll();
                    plugin.i18n().reload();
                    plugin.reloadConfig();
                    player.sendMessage(Main.prefix().append(MiniMessage.miniMessage().deserialize(plugin.i18n().get("mainCommand.reload.success"))));
                    plugin.entities().spawnAll();
                } catch (Exception e) {
                    player.sendMessage(Main.prefix().append(MiniMessage.miniMessage().deserialize(plugin.i18n().get("mainCommand.reload.failure"))));
                    throw new RuntimeException(e);
                }
                break;
            case "info":
                player.sendMessage(Main.prefix().append(MiniMessage.miniMessage().deserialize(plugin.i18n().get("mainCommand.info", plugin.i18n().get("prefix", "eindaniels (German), Kn7ghtdemon (English)")))));
                break;
            case "version":
                new VersionChecker(plugin, "eindaniels", "PlayerShops", true).check((result, latest) -> {
                    String current = getCurrentVersion();
                    String currentColor = colorFor(current);

                    if (result == VersionChecker.Result.OUTDATED) {
                        String latestColor = colorFor(latest);
                        player.sendMessage(Main.prefix().append(MiniMessage.miniMessage().deserialize(
                                plugin.i18n().get("mainCommand.version.outdated", currentColor, current, latestColor, latest))));
                    } else if (result == VersionChecker.Result.UP_TO_DATE) {
                        player.sendMessage(Main.prefix().append(MiniMessage.miniMessage().deserialize(
                                plugin.i18n().get("mainCommand.version.uptodate", currentColor, current))));
                    } else {
                        player.sendMessage(Main.prefix().append(MiniMessage.miniMessage().deserialize(
                                plugin.i18n().get("mainCommand.version.failed", currentColor, current))));
                    }
                });
                break;
            default:
                player.sendMessage(Main.prefix().append(MiniMessage.miniMessage().deserialize(plugin.i18n().get("mainCommand.usage"))));
                break;
        }

        return false;
    }

    @Override
    public @NotNull List<String> tabComplete(@NotNull CommandSender sender, @NotNull String alias, @NotNull String @NotNull [] args) throws IllegalArgumentException {
        if (args.length == 1) {
            return List.of("reload", "version", "info");
        }
        return Collections.emptyList();
    }

    public String getCurrentVersion() {
        return plugin.getDescription().getVersion().replaceFirst("^[vV]", "");
    }

    private String colorFor(String version) {
        String channel = "stable"; // kein Channel angegeben = stable
        for (String token : version.toLowerCase().split("[-.+]")) {
            switch (token) {
                case "alpha":
                case "beta":
                case "rc":
                case "stable":
                    channel = token;
                    break;
                default:
                    break;
            }
        }
        return plugin.i18n().get("versioning." + channel);
    }
}
