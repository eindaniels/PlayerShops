package de.eindaniel.playerShops.commands;

import de.eindaniel.playerShops.Main;
import de.eindaniel.playerShops.shop.PlayerShop;
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
    private static final MiniMessage MM = MiniMessage.miniMessage();

    public CreateShopCommand(Main plugin) {
        super("createshop");
        this.plugin = plugin;
        setDescription("Erstellt einen neuen Spielershop.");
        setPermission("playershop.create");
    }

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player p)) {
            sender.sendMessage("Nur Spieler können diesen Befehl nutzen.");
            return true;
        }

        if (!p.hasPermission("playershop.create")) {
            p.sendMessage(Main.prefix().append(MM.deserialize(plugin.i18n().get("noperm"))));
            return true;
        }

        if (args.length < 3) {
            p.sendMessage(Main.prefix().append(MM.deserialize(plugin.i18n().get("createshop.usage"))));
            return true;
        }

        double buy, sell;
        int amount;
        try {
            buy = Double.parseDouble(args[0]);
            sell = Double.parseDouble(args[1]);
            amount = Integer.parseInt(args[2]);
            if (buy < 0 || sell < 0 || amount <= 0) throw new IllegalArgumentException();
        } catch (Exception ex) {
            p.sendMessage(Main.prefix().append(MM.deserialize(plugin.i18n().get("createshop.invalidInt"))));
            return true;
        }

        ItemStack hand = p.getInventory().getItemInMainHand();
        if (hand.getType() == Material.AIR) {
            p.sendMessage(Main.prefix().append(MM.deserialize(plugin.i18n().get("createshop.handNull"))));
            return true;
        }

        Block look = p.getTargetBlockExact(6);
        if (look == null) {
            p.sendMessage(Main.prefix().append(MM.deserialize(plugin.i18n().get("createshop.targetBlock"))));
            return true;
        }

        double pricePlayerShop = plugin.config().getDouble("price-playershops", 0);
        if (pricePlayerShop > 0) {
            if (!plugin.vault().has(p, pricePlayerShop)) {
                p.sendMessage(Main.prefix().append(MM.deserialize(
                        plugin.i18n().get("createshop.price.noMoney", String.format("%.2f€", pricePlayerShop)))));
                return true;
            }
            if (!plugin.vault().withdraw(p, pricePlayerShop)) {
                p.sendMessage(Main.prefix().append(MM.deserialize(plugin.i18n().get("interaction.ecoError"))));
                return true;
            }
            p.sendMessage(Main.prefix().append(MM.deserialize(plugin.i18n().get("createshop.price.transactionSuccess"))));
            p.sendMessage(Main.prefix().append(MM.deserialize(
                    plugin.i18n().get("createshop.price.amount", String.format("%.2f€", pricePlayerShop)))));
        }

        Location base = look.getLocation().add(0.5, 1.0, 0.5);
        PlayerShop shop = plugin.shops().create(p, base, hand.clone(), amount, buy, sell);
        plugin.entities().spawnFor(shop);

        try { plugin.storage().saveAll(); } catch (Exception ignored) {}

        p.sendMessage(Main.prefix().append(MM.deserialize(plugin.i18n().get("createshop.created"))));
        return true;
    }
}
