package me.jaimemartz.townydeath;

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
            if (player.getLocation().getWorld() != plugin.getServer().getWorlds().get(0)) {
                player.teleport(plugin.getSpawnPoint());
            } else {
                player.sendMessage(ChatColor.RED + "Este comando no se puede ejecutar en el mundo principal");
            }
        } else {
            sender.sendMessage("This command can only be executed by a player");
        }
        return true;
    }
}
