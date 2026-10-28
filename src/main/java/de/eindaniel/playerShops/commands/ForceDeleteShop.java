package de.eindaniel.playerShops.commands;

import de.eindaniel.playerShops.Main;
import de.eindaniel.playerShops.shop.PlayerShop;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;

public class ForceDeleteShop extends Command {

    private final Main plugin;
    private static final MiniMessage MM = MiniMessage.miniMessage();


    public ForceDeleteShop(Main plugin) {
        super("forcedeleteshop");
        this.plugin = plugin;
        setAliases(List.of("frcdltshop"));
        setDescription("Force delete a shop with Permission");
    }

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String @NotNull [] args) {
        if (!(sender instanceof Player p)) {
            sender.sendMessage("Only players can use this command.");
            return true;
        }

        if (!p.hasPermission("playershop.forcedelete")) {
            p.sendMessage(Main.prefix().append(MM.deserialize(plugin.i18n().get("noperm"))));
            return true;
        }

        Block look = p.getTargetBlockExact(6);
        if (look == null) {
            p.sendMessage(Main.prefix().append(MM.deserialize(plugin.i18n().get("removeshop.targetBlock"))));
            return true;
        }

        String key = look.getWorld().getName() + ":"
                + look.getX() + ":" + (look.getY() + 1) + ":" + look.getZ();
        Optional<PlayerShop> shopOpt = plugin.shops().getByKey(key);

        if (shopOpt.isEmpty()) {
            p.sendMessage(Main.prefix().append(MM.deserialize(plugin.i18n().get("removeshop.noShop"))));
            return true;
        }

        PlayerShop shop = shopOpt.get();

        plugin.entities().despawnFor(shop);
        plugin.shops().delete(shop);
        try { plugin.storage().saveAll(); } catch (Exception ignored) {}

        plugin.playerData().removeShopAmount(p.getUniqueId());

        p.sendMessage(Main.prefix().append(MM.deserialize(plugin.i18n().get("forceremoveshop.removed"))));
        return true;
    }
}
