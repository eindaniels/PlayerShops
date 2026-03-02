package de.eindaniel.playerShops.commands;

import com.mojang.brigadier.Message;
import de.eindaniel.playerShops.Main;
import de.eindaniel.playerShops.shop.PlayerShop;
import de.eindaniel.playerShops.util.MessageUtils;
import de.eindaniel.playerShops.util.MessageUtilsSingleton;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.block.Block;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class RemoveShopCommand extends Command {

    private final Main plugin;

    public RemoveShopCommand(Main plugin) {
        super("removeshop");
        this.plugin = plugin;
    }


    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String @NotNull [] args) {
        MessageUtils messageUtils = MessageUtilsSingleton.getInstance();
        if (!(sender instanceof Player p)) { sender.sendMessage("Nur Spieler."); return true; }
        if (!p.hasPermission("playershop.remove")) { messageUtils.sendLocalizedMessage(p, "noperm"); return true; }

        Block look = p.getTargetBlockExact(6);
        if (look == null) {
            messageUtils.sendLocalizedMessage(p, "removeshop.targetBlock");
            return true;
        }

        String key = look.getWorld().getName() + ":" + (look.getX()+0) + ":" + (look.getY()+1) + ":" + (look.getZ()+0);
        Optional<PlayerShop> shopOpt = plugin.shops().getByKey(key);
        if (shopOpt.isEmpty()) {
            messageUtils.sendLocalizedMessage(p, "removeshop.noShop");
            return true;
        }
        PlayerShop shop = shopOpt.get();

        if (!shop.getOwner().equals(p.getUniqueId())) {
            messageUtils.sendLocalizedMessage(p, "removeshop.notOwner");
            return true;
        }

        if (shop.countStashMaterial() > 0) {
            messageUtils.sendLocalizedMessage(p, "removeshop.itemsInStash");
            return true;
        }

        double refundPrice = plugin.config().getDouble("refund-playershops");

        if (refundPrice != 0 || refundPrice != -1) {
            plugin.vault().deposit(p, refundPrice);
            messageUtils.sendLocalizedMessage(p, "removeshop.price.transactionSuccess");
            messageUtils.sendLocalizedMessage(p, "removeshop.price.amount", String.format("%.2f€", refundPrice));
        }

        plugin.entities().despawnAll(); // einfache Variante: alles weg
        plugin.shops().delete(shop);
        try { plugin.storage().saveAll(); } catch (Exception ignored) {}
        plugin.entities().spawnAll();

        messageUtils.sendLocalizedMessage(p, "removeshop.removed");
        return true;
    }
}
