package de.eindaniel.playerShops.commands;

import de.eindaniel.playerShops.Main;
import de.eindaniel.playerShops.shop.PlayerShop;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.block.Block;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class RemoveShopCommand extends Command {

    private final Main plugin;
    private static final MiniMessage MM = MiniMessage.miniMessage();

    public RemoveShopCommand(Main plugin) {
        super("removeshop");
        this.plugin = plugin;
        setDescription("Entfernt deinen Spielershop.");
        setPermission("playershop.remove");
    }

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player p)) {
            sender.sendMessage("Nur Spieler können diesen Befehl nutzen.");
            return true;
        }

        if (!p.hasPermission("playershop.remove")) {
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

        if (!shop.getOwner().equals(p.getUniqueId())) {
            p.sendMessage(Main.prefix().append(MM.deserialize(plugin.i18n().get("removeshop.notOwner"))));
            return true;
        }

        if (shop.countStashMaterial() > 0) {
            p.sendMessage(Main.prefix().append(MM.deserialize(plugin.i18n().get("removeshop.itemsInStash"))));
            return true;
        }

        double refundPrice = plugin.config().getDouble("refund-playershops", 0);
        if (refundPrice > 0) {
            plugin.vault().deposit(p, refundPrice);
            p.sendMessage(Main.prefix().append(MM.deserialize(plugin.i18n().get("removeshop.price.transactionSuccess"))));
            p.sendMessage(Main.prefix().append(MM.deserialize(
                    plugin.i18n().get("removeshop.price.amount", String.format("%.2f€", refundPrice)))));
        }

        plugin.entities().despawnFor(shop);
        plugin.shops().delete(shop);
        try { plugin.storage().saveAll(); } catch (Exception ignored) {}

        p.sendMessage(Main.prefix().append(MM.deserialize(plugin.i18n().get("removeshop.removed"))));
        return true;
    }
}
