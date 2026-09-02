package de.eindaniel.playerShops.commands;

import de.eindaniel.playerShops.Main;
import de.eindaniel.playerShops.config.Internationalization;
import de.eindaniel.playerShops.util.VersionChecker;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import javax.swing.text.html.MinimalHTMLWriter;
import java.util.List;

public class MasterCommand extends Command {
    final Main plugin;

    protected MasterCommand(Main plugin) {
        super("playershops");
        this.plugin = plugin;
        setAliases(List.of("ps", "pshops"));
    }

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String @NotNull [] args) {
        if (!(sender instanceof Player player)) return true;
        switch (args[0]) {
            case "reload":
                try {
                    plugin.i18n().reload();
                    plugin.reloadConfig();
                    player.sendMessage(Main.prefix().append(MiniMessage.miniMessage().deserialize(plugin.i18n().get("mainCommand.reload.success"))));
                } catch (Exception e) {
                    player.sendMessage(Main.prefix().append(MiniMessage.miniMessage().deserialize(plugin.i18n().get("mainCommand.reload.failure"))));
                    throw new RuntimeException(e);
                }
                break;
            case "reloadEntities":
                // TODO
                break;
            case "version":
                new VersionChecker(plugin, "eindaniels", "PlayerShops").check((result, latest) -> {
                    if (result == VersionChecker.Result.OUTDATED) {
                        player.sendMessage(Main.prefix().append(MiniMessage.miniMessage().deserialize(plugin.i18n().get("mainCommand.version.outdated", result, latest))));
                    } else if (result == VersionChecker.Result.UP_TO_DATE) {
                        player.sendMessage(Main.prefix().append(MiniMessage.miniMessage().deserialize(plugin.i18n().get("mainCommand.version.uptodate", result))));
                    } else {
                        player.sendMessage(Main.prefix().append(MiniMessage.miniMessage().deserialize(plugin.i18n().get("mainCommand.version.failed", result))));
                    }
                });
                break;
            default:
                player.sendMessage(Main.prefix().append(MiniMessage.miniMessage().deserialize(plugin.i18n().get("mainCommand.usage"))));
                break;
        }

        return false;
    }
}
