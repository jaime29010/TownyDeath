package me.jaimemartz.townydeath;

import me.jaimemartz.faucet.Messager;
import me.jaimemartz.townydeath.entity.ArmorStandManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.Set;
import java.util.stream.Collectors;

public class TownyCommand implements CommandExecutor {
    private final TownyDeath plugin;
    public TownyCommand(TownyDeath plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Messager msgr = new Messager(sender);
        if (sender instanceof Player) {
            Player player = (Player) sender;
            if (args.length > 0) {
                switch (args[0].toLowerCase()) {
                    case "spawn": {
                        Set<Entity> result = ArmorStandManager.spawn(player.getLocation().add(0, 3, 0));
                        plugin.getDataPool().getEntities().addAll(result.stream().map(Entity::getUniqueId).collect(Collectors.toSet()));
                        msgr.send("&aHas creado una cruz en este lugar");
                        return true;
                    }
                }
            }
            msgr.send("&cUsage: /townydeath spawn");
        } else {
            msgr.send("&cThis command can only be executed by a player");
        }
        return true;
    }
}
