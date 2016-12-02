package me.jaimemartz.townydeath;

import com.palmergames.bukkit.towny.exceptions.TownyException;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Town;
import me.jaimemartz.townydeath.data.JsonLocation;
import me.jaimemartz.townydeath.event.PlayerGhostEvent;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.*;
import org.bukkit.event.player.*;
import org.bukkit.event.vehicle.VehicleEnterEvent;
import org.bukkit.inventory.ItemStack;

public class PlayerListener implements Listener {
    private final TownyDeath plugin;
    public PlayerListener(TownyDeath plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void on(PlayerDeathEvent event) {
        Player player = event.getEntity();

        if (plugin.getDataPool().getRevived().containsKey(player)) {
            event.setDeathMessage(null);
            event.getDrops().clear();
            return;
        }

        PlayerGhostEvent call = new PlayerGhostEvent(player);
        plugin.getServer().getPluginManager().callEvent(call);
        if (call.isCancelled()) return;

        plugin.applyDeath(player);

        if (player.isInsideVehicle()) {
            player.leaveVehicle();
        }

        Location location = player.getLocation();
        World world = location.getWorld();
        ExperienceOrb exp = (ExperienceOrb) world.spawnEntity(location, EntityType.EXPERIENCE_ORB);
        exp.setExperience(event.getDroppedExp());
        event.getDrops().forEach(item -> world.dropItemNaturally(location, item));
        event.setDroppedExp(0);
        event.getDrops().clear();

        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            Resident resident = TownyUtils.getResident(player);
            if (resident != null && resident.isJailed()) {
                Town town = TownyUtils.getTown(resident.getJailTown());
                if (town != null) {
                    try {
                        Location target = town.getJailSpawn(resident.getJailSpawn());
                        resident.setJailed(false);
                        plugin.safeTeleport(player, target);
                        resident.setJailed(true);
                    } catch (TownyException ignored) {}
                }
            }
        }, 20);

        player.spigot().respawn();

        if (location.getWorld() != plugin.getServer().getWorlds().get(0)) {
            plugin.safeTeleport(player, plugin.getSpawnPoint());
        } else if (player.getLocation().getBlockY() <= -5) {
            plugin.safeTeleport(player, plugin.getSpawnPoint());
        } else {
            int highest = location.getWorld().getHighestBlockYAt(location);
            if (location.getBlockY() != highest) {
                Location target = location.clone();
                target.setY(highest + 1);
                plugin.safeTeleport(player, target);
            }
        }

        findNearest(player);
    }

    @EventHandler
    public void on(PlayerInteractAtEntityEvent event) {
        Player player = event.getPlayer();
        Entity clicked = event.getRightClicked();
        if (plugin.getDataPool().getEntities().contains(clicked.getUniqueId())) {
            if (plugin.checkRevive(player)) {
                player.sendMessage(ChatColor.GREEN + "Enhorabuena, has revivido");
            }
        } else if (player.getItemInHand().isSimilar(plugin.getHealer())) {
            if (plugin.getDataPool().getDied().contains(clicked.getUniqueId())) {
                Player target = plugin.getServer().getPlayer(clicked.getUniqueId());
                if (target != null) {
                    ItemStack item = player.getItemInHand();
                    if (item.getAmount() <= 1) {
                        item.setType(Material.AIR);
                    } else {
                        item.setAmount(item.getAmount() - 1);
                    }
                    player.setItemInHand(item);
                    if (plugin.checkRevive(target)) {
                        target.sendMessage(ChatColor.GREEN + String.format("%s te ha revivido", player.getName()));
                        player.sendMessage(ChatColor.GREEN + String.format("Has revivido a %s", target.getName()));
                    }
                }
            }
        }
        checkCancel(event);
    }

    @EventHandler
    public void on(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        if (plugin.getDataPool().getRevived().containsKey(player)) {
            JsonLocation object = plugin.getDataPool().getRevived().remove(player);
            if (object != null) {
                event.setRespawnLocation(object.toBukkit());
            }
        } else {
            if (plugin.getDataPool().getDied().contains(player.getUniqueId())) {
                plugin.applyDeath(player);
                findNearest(player);
            }
        }
    }

    @EventHandler
    public void on(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (plugin.getDataPool().getDied().contains(player.getUniqueId())) {
            plugin.applyDeath(player);
            findNearest(player);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void on(PlayerTeleportEvent event) {
        Player player = event.getPlayer();
        if (plugin.getDataPool().getDied().contains(player.getUniqueId())) {
            if (!plugin.getTpBypass().contains(player.getUniqueId())) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void on(PlayerArmorStandManipulateEvent event) {
        if (plugin.getDataPool().getEntities().contains(event.getRightClicked().getUniqueId())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void on(AsyncPlayerChatEvent event) {
        checkCancel(event);
        plugin.getDataPool().getDied().forEach(other -> event.getRecipients().remove(plugin.getServer().getPlayer(other)));
    }

    @EventHandler
    public void on(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player) {
            checkCancel((Player) event.getEntity(), event);
        }
    }

    @EventHandler
    public void on(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player) {
            checkCancel((Player) event.getDamager(), event);
        }
    }

    @EventHandler
    public void on(EntityTargetLivingEntityEvent event) {
        if (event.getTarget() instanceof Player) {
            checkCancel((Player) event.getTarget(), event);
        }
    }

    @EventHandler
    public void on(EntityRegainHealthEvent event) {
        if (event.getEntity() instanceof Player) {
            checkCancel((Player) event.getEntity(), event);
        }
    }

    @EventHandler
    public void on(VehicleEnterEvent event) {
        if (event.getEntered() instanceof Player) {
            checkCancel((Player) event.getEntered(), event);
        }
    }

    @EventHandler
    public void on(FoodLevelChangeEvent event) {
        checkCancel((Player) event.getEntity(), event);
    }

    @EventHandler
    public void on(PlayerQuitEvent event) {
        plugin.checkClear(event.getPlayer());
    }

    @EventHandler
    public void on(PlayerPickupItemEvent event) {
        checkCancel(event);
    }

    @EventHandler
    public void on(PlayerDropItemEvent event) {
        checkCancel(event);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void on(PlayerCommandPreprocessEvent event) {
        checkCancel(event);
    }

    @EventHandler
    public void on(PlayerInteractEntityEvent event) {
        checkCancel(event);
    }

    @EventHandler
    public void on(PlayerInteractEvent event) {
        checkCancel(event);
    }

    @EventHandler
    public void on(PlayerExpChangeEvent event) {
        checkCancel(event);
    }

    public void findNearest(Player player) {
        Entity nearest = plugin.getNearest(player);
        if (nearest != null) {
            Location loc = nearest.getLocation();
            plugin.getFindTasks().put(player, plugin.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, () -> {
                player.sendMessage(ChatColor.YELLOW + "Estas muerto, busca una cruz para revivir");
                player.sendMessage(ChatColor.GREEN + String.format("La cruz mas cercana esta en x:%s y:%s z:%s", loc.getBlockX(), loc.getBlockY(), loc.getBlockZ()));
            }, 10, 20 * 10));
        }
    }

    public boolean checkCancel(PlayerEvent ins) {
        if (ins instanceof Cancellable) {
            return checkCancel(ins.getPlayer(), (Cancellable) ins);
        }
        return false;
    }

    public boolean checkCancel(Player player, Cancellable ins) {
        if (plugin.getDataPool().getDied().contains(player.getUniqueId())) {
            ins.setCancelled(true);
            return true;
        }
        return false;
    }
}