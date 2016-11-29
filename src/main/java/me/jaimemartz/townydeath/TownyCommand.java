package me.jaimemartz.townydeath;

import me.jaimemartz.faucet.Messager;
import me.jaimemartz.townydeath.entity.ArmorStandManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
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
                        plugin.getEntitiesCache().addAll(result);
                        msgr.send("&aHas creado una cruz en este lugar");
                        return true;
                    }
                    case "respawn": {
                        if (args.length == 2) {
                            Player target = Bukkit.getPlayerExact(args[1]);
                            if (target != null) {
                                plugin.checkRevive(target);
                                target.sendMessage(ChatColor.GREEN + "Un administrador te ha revivido");
                            } else {
                                player.sendMessage(ChatColor.RED + "The player specified is not online");
                            }
                        } else break;
                        return true;
                    }

                    case "item": {
                        player.getInventory().addItem(plugin.getHealer());
                        msgr.send("&aAhora tienes un item con el que revivir a los jugadores muertos");
                        return true;
                    }
                }
            }

            msgr.send(
                    "&e=====================================================",
                    "&7Usage for TownyDeath:",
                    "&3/.. spawn &7- &cCrea una cruz en el lugar en el que estas",
                    "&3/.. item &7- &cTe da un kit para revivir a un jugador",
                    "&3/.. respawn <player> &7- &cRevive a un jugador",
                    "&e=====================================================");
        } else {
            msgr.send("&cThis command can only be executed by a player");
        }
        return true;
    }
}
