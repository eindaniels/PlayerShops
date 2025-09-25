package de.eindaniel.playerShops.commands;

import de.eindaniel.playerShops.Main;
import de.eindaniel.playerShops.shop.PlayerShop;
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
        if (!(sender instanceof Player p)) { sender.sendMessage("Nur Spieler."); return true; }
        if (!p.hasPermission("playershop.remove")) { p.sendMessage("Keine Berechtigung."); return true; }

        Block look = p.getTargetBlockExact(6);
        Component targetBlock = MiniMessage.miniMessage().deserialize("<#ff1717>Ziele auf den Block unter dem Shop.");
        if (look == null) { p.sendMessage(Main.prefix().append(targetBlock)); return true; }

        String key = look.getWorld().getName() + ":" + (look.getX()+0) + ":" + (look.getY()+1) + ":" + (look.getZ()+0);
        Optional<PlayerShop> shopOpt = plugin.shops().getByKey(key);
        Component noShop = MiniMessage.miniMessage().deserialize("<#ff1717>Hier ist kein Spielershop!");
        if (shopOpt.isEmpty()) { p.sendMessage(Main.prefix().append(noShop)); return true; }
        PlayerShop shop = shopOpt.get();

        Component notOwner = MiniMessage.miniMessage().deserialize("<#ff1717>Du bist nicht der Besitzer!");
        if (!shop.getOwner().equals(p.getUniqueId())) { p.sendMessage(Main.prefix().append(notOwner)); return true; }

        if (shop.countStashMaterial() > 0) {
            Component notEmptyStash = MiniMessage.miniMessage().deserialize("<#ff1717>Du hast noch Sachen in deinem Lager!");
            p.sendMessage(Main.prefix().append(notEmptyStash));
            return true;
        }

        double refundPrice = plugin.config().getDouble("refund-playershops");

        if (refundPrice != 0 || refundPrice != -1) {
            plugin.vault().deposit(p, refundPrice);
            Component transaction = MiniMessage.miniMessage().deserialize("<#1fff17>Transaktion erfolgreich! (Shop-Refund)");
            Component price = MiniMessage.miniMessage().deserialize("<#ff1717>- " + String.format("%.2f€", refundPrice));
            p.sendMessage(Main.prefix().append(transaction));
            p.sendMessage(Main.prefix().append(price));
        }

        plugin.entities().despawnAll(); // einfache Variante: alles weg
        plugin.shops().delete(shop);
        try { plugin.storage().saveAll(); } catch (Exception ignored) {}
        plugin.entities().spawnAll();

        Component removedShop = MiniMessage.miniMessage().deserialize("<#1fff17>Dein Shop wurde erfolgreich entfernt.");
        p.sendMessage(Main.prefix().append(removedShop));
        return true;
    }
}
