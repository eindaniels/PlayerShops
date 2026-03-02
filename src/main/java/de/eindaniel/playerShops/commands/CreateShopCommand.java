package de.eindaniel.playerShops.commands;

import de.eindaniel.playerShops.Main;
import de.eindaniel.playerShops.shop.PlayerShop;
import de.eindaniel.playerShops.util.MessageUtils;
import de.eindaniel.playerShops.util.MessageUtilsSingleton;
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
        MessageUtils messageUtils = MessageUtilsSingleton.getInstance();
        if (!(sender instanceof Player p)) { sender.sendMessage("Keine Berechtigung."); return true; }
        if (!p.hasPermission("playershop.create")) { messageUtils.sendLocalizedMessage(p, "noperm"); return true; }
        if (args.length < 3) {
            messageUtils.sendLocalizedMessage(p, "createshop.usage");
            return true;
        }

        double buy, sell; int amount;
        try {
            buy = Double.parseDouble(args[0]);
            sell = Double.parseDouble(args[1]);
            amount = Integer.parseInt(args[2]);
            if (amount <= 0) throw new IllegalArgumentException();
        } catch (Exception ex) {
            messageUtils.sendLocalizedMessage(p, "createshop.invalidInt");
            return true;
        }

        ItemStack hand = p.getInventory().getItemInMainHand();
        
//        if (hand.getType().equals(Material.ENCHANTED_BOOK) || hand.getType().equals(Material.POTION) || hand.getType().equals(Material.SPLASH_POTION) || hand.getType().equals(Material.LINGERING_POTION)) {
//            p.sendMessage(Main.prefix().append(MiniMessage.miniMessage().deserialize("<#ff1717>Diese Items sind aktuell nicht verfügbar. Wir arbeiten an einem Fix.")));
//            return true;
//        }
        
        if (hand == null || hand.getType() == Material.AIR) {
            messageUtils.sendLocalizedMessage(p, "createshop.handNull");
            return true;
        }

        Block look = p.getTargetBlockExact(6);
        if (look == null) {
            messageUtils.sendLocalizedMessage(p, "createshop.targetBlock");
            return true;
        }

        double pricePlayerShop = plugin.getConfig().getDouble("price-playershops");
        if (pricePlayerShop != -1 ||  pricePlayerShop != 0) {
            if (!plugin.vault().has(p, plugin.getConfig().getDouble("price-playershops"))) { p.sendMessage(Main.prefix().append(MiniMessage.miniMessage().deserialize("<#ff1717>Du hast nicht genug Geld für einen SpielerShop! (" + pricePlayerShop + ")"))); return false; }
            plugin.vault().withdraw(p, pricePlayerShop);
            messageUtils.sendLocalizedMessage(p, "createshop.price.transactionSuccess");
            messageUtils.sendLocalizedMessage(p, "createshop.price.amount", String.format("%.2f€", pricePlayerShop));
        }

        Location base = look.getLocation().add(0.5, 1.0, 0.5);

        PlayerShop shop = plugin.shops().create(p, base, hand.clone(), amount, buy, sell);
        plugin.entities().spawnFor(shop);
        try { plugin.storage().saveAll(); } catch (Exception ignored) {}

        messageUtils.sendLocalizedMessage(p, "createshop.created");
        return true;
    }
}
