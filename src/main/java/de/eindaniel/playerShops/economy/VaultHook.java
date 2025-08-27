package de.eindaniel.playerShops.economy;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.Plugin;

public class VaultHook {

    private final Plugin plugin;
    private Economy economy;

    public VaultHook(Plugin plugin) { this.plugin = plugin; }

    public boolean hook() {
        if (plugin.getServer().getPluginManager().getPlugin("Vault") == null) return false;
        RegisteredServiceProvider<Economy> rsp = Bukkit.getServicesManager().getRegistration(Economy.class);
        if (rsp == null) return false;
        economy = rsp.getProvider();
        return economy != null;
    }

    public Economy eco() { return economy; }

    public boolean has(OfflinePlayer p, double amount) { return eco().has(p, amount); }
    public boolean deposit(OfflinePlayer p, double amount) { return eco().depositPlayer(p, amount).transactionSuccess(); }
    public boolean withdraw(OfflinePlayer p, double amount) { return eco().withdrawPlayer(p, amount).transactionSuccess(); }
}
