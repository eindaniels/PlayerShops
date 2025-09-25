package de.eindaniel.playerShops.commands;

import de.eindaniel.playerShops.Main;
import de.eindaniel.playerShops.shop.PlayerShop;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class CreateShopCommand extends Command {

    private final Main plugin;

    public CreateShopCommand(Main plugin) {
        super("createshop");
        this.plugin = plugin;
    }

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String @NotNull [] args) {
        if (!(sender instanceof Player p)) { sender.sendMessage("Nur Spieler."); return true; }
        if (!p.hasPermission("playershop.create")) { p.sendMessage("Keine Berechtigung."); return true; }
        if (args.length < 3) {
            Component usage = MiniMessage.miniMessage().deserialize("<gray>Richtige Verwendung <dark_gray>→ <#fbecab>/createshop <Verkaufspreis> <Ankaufspreis> <Menge>");
            p.sendMessage(Main.prefix().append(usage));
            return true;
        }

        double buy, sell; int amount;
        try {
            buy = Double.parseDouble(args[0]);
            sell = Double.parseDouble(args[1]);
            amount = Integer.parseInt(args[2]);
            if (amount <= 0) throw new IllegalArgumentException();
        } catch (Exception ex) {
            Component invalidInt = MiniMessage.miniMessage().deserialize("<#ff1717>Ungültige Zahlen!");
            p.sendMessage(Main.prefix().append(invalidInt)); return true;
        }

        ItemStack hand = p.getInventory().getItemInMainHand();
        if (hand == null || hand.getType() == Material.AIR) {
            Component holdItem = MiniMessage.miniMessage().deserialize("Halte das Item, was du verkaufen möchtest, in deiner Hand.");
            p.sendMessage(Main.prefix().append(holdItem)); return true;
        }

        Block look = p.getTargetBlockExact(6);
        Component targetBlock = MiniMessage.miniMessage().deserialize("<#ff1717>Ziele auf einen Block im Umkreis von 6 Blöcken!");
        if (look == null) { p.sendMessage(Main.prefix().append(targetBlock)); return true; }

        double pricePlayerShop = plugin.getConfig().getDouble("price-playershops");
        if (pricePlayerShop != -1 ||  pricePlayerShop != 0) {
            if (!plugin.vault().has(p, plugin.getConfig().getDouble("price-playershops"))) { p.sendMessage(Main.prefix().append(MiniMessage.miniMessage().deserialize("<#ff1717>Du hast nicht genug Geld für einen SpielerShop! (" + pricePlayerShop + ")"))); return false; }
            plugin.vault().withdraw(p, pricePlayerShop);
            Component transaction = MiniMessage.miniMessage().deserialize("<#1fff17>Transaktion erfolgreich!");
            Component price = MiniMessage.miniMessage().deserialize("<#ff1717>- " + String.format("%.2f€", pricePlayerShop));
            p.sendMessage(Main.prefix().append(transaction));
            p.sendMessage(Main.prefix().append(price));

        }

        Location base = look.getLocation().add(0.5, 1.0, 0.5);

        PlayerShop shop = plugin.shops().create(p, base, hand.getType(), amount, buy, sell);
        plugin.entities().spawnFor(shop);
        try { plugin.storage().saveAll(); } catch (Exception ignored) {}

        Component shopCreated = MiniMessage.miniMessage().deserialize("<#1fff17>Dein Spielershop wurde erfolgreich erstellt.");
        p.sendMessage(Main.prefix().append(shopCreated));
        return true;
    }
}
