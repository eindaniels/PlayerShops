package de.eindaniel.playerShops.shop;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class PlayerShop {

    private final UUID owner;
    private final Location baseLocation;          // Mittelpunkt des Blocks
    private final Material material;              // Welches Item wird gehandelt
    private final int amountPerTrade;             // Menge
    private double buyPrice;                      // Spieler zahlt (Shop verkauft)  -> "Verkauf:"
    private double sellPrice;                     // Spieler bekommt (Shop kauft)   -> "Ankauf:"

    private boolean buyEnabled = true;            // Kaufen (Shop verkauft an Spieler)
    private boolean sellEnabled = true;           // Verkaufen (Shop kauft vom Spieler)

    private final List<ItemStack> stashItems = new ArrayList<>();

    public PlayerShop(UUID owner, Location baseLocation, Material material, int amountPerTrade,
                      double buyPrice, double sellPrice) {
        this.owner = owner;
        this.baseLocation = baseLocation.clone();
        this.material = material;
        this.amountPerTrade = amountPerTrade;
        this.buyPrice = buyPrice;
        this.sellPrice = sellPrice;
    }

    public UUID getOwner() { return owner; }
    public Location getBaseLocation() { return baseLocation.clone(); }
    public Material getMaterial() { return material; }
    public int getAmountPerTrade() { return amountPerTrade; }
    public double getBuyPrice() { return buyPrice; }
    public double getSellPrice() { return sellPrice; }
    public boolean isBuyEnabled() { return buyEnabled; }
    public boolean isSellEnabled() { return sellEnabled; }

    public void setBuyEnabled(boolean b) { this.buyEnabled = b; }
    public void setSellEnabled(boolean b) { this.sellEnabled = b; }

    public List<ItemStack> getStashItems() { return stashItems; }

    public String key() {
        return baseLocation.getWorld().getName() + ":" +
                baseLocation.getBlockX() + ":" + baseLocation.getBlockY() + ":" + baseLocation.getBlockZ();
    }

    public void setSellPrice(double sellPrice) {
        this.sellPrice = sellPrice;
    }

    public void setBuyPrice(double buyPrice) {
        this.buyPrice = buyPrice;
    }

    public int countStashMaterial() {
        int n = 0;
        for (ItemStack is : stashItems) {
            if (is == null) continue;
            if (is.getType() == material) n += is.getAmount();
        }
        return n;
    }

    public int takeFromStash(int amount) {
        int left = amount;
        for (int i = 0; i < stashItems.size() && left > 0; i++) {
            ItemStack is = stashItems.get(i);
            if (is == null) continue;
            if (is.getType() != material) continue;
            int take = Math.min(is.getAmount(), left);
            is.setAmount(is.getAmount() - take);
            left -= take;
            if (is.getAmount() <= 0) stashItems.set(i, null);
        }
        return amount - left;
    }

    public void addToStash(int amount) {
        int left = amount;
        while (left > 0) {
            int add = Math.min(left, material.getMaxStackSize());
            stashItems.add(new ItemStack(material, add));
            left -= add;
        }
    }
}
