package me.jaimemartz.townydeath;

import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SpawnCommand implements CommandExecutor {
    private final TownyDeath plugin;
    public SpawnCommand(TownyDeath plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            if (player.getLocation().getWorld() == plugin.getServer().getWorlds().get(0)) {
                int price = plugin.getConfig().getInt("features.spawn-command.price");
                if (plugin.getEconomy().has(player, price)) {
                    EconomyResponse response = plugin.getEconomy().withdrawPlayer(player, price);
                    if (response.transactionSuccess()) {
                        player.teleport(plugin.getSpawnPoint());
                        player.sendMessage(ChatColor.GREEN + String.format("Se te ha cobrado %s por llevarte al spawn", price));
                    } else {
                        player.sendMessage(ChatColor.RED + "Ha ocurrido un error al cobrarte");
                    }
                } else {
                    player.sendMessage(ChatColor.RED + "No tienes suficiente dinero para ejecutar este comando");
                }
            } else {
                player.teleport(plugin.getSpawnPoint());
            }
        } else {
            sender.sendMessage("This command can only be executed by a player");
        }
        return true;
    }
}
