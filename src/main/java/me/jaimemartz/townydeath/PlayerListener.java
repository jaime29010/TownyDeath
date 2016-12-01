package me.jaimemartz.townydeath;

import com.palmergames.bukkit.towny.exceptions.TownyException;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Town;
import me.jaimemartz.townydeath.event.PlayerGhostEvent;
import me.jaimemartz.townydeath.utils.PluginUtils;
import me.jaimemartz.townydeath.utils.TitleUtils;
import org.bukkit.*;
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
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class PlayerListener implements Listener {
    private final TownyDeath plugin;
    public PlayerListener(TownyDeath plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void on(PlayerDeathEvent event) {
        Player player = event.getEntity();

        if (plugin.getRevived().containsKey(player)) {
            event.setDeathMessage(null);
            event.getDrops().clear();
            return;
        }

        PlayerGhostEvent call = new PlayerGhostEvent(player);
        plugin.getServer().getPluginManager().callEvent(call);
        if (call.isCancelled()) return;

        plugin.getDataPool().getPlayers().add(player.getUniqueId());
        applyDeath(player);

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
    public void on(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        if (plugin.getRevived().containsKey(player)) {
            Location target = plugin.getRevived().remove(player);
            if (target != null) {
                event.setRespawnLocation(target);
            }
        } else {
            if (plugin.getDataPool().getPlayers().contains(player.getUniqueId())) {
                applyDeath(player);
                findNearest(player);
            }
        }
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
            if (plugin.getDataPool().getPlayers().contains(clicked.getUniqueId())) {
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
    public void on(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (plugin.getDataPool().getPlayers().contains(player.getUniqueId())) {
            applyDeath(player);
            findNearest(player);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void on(PlayerTeleportEvent event) {
        Player player = event.getPlayer();
        if (plugin.getDataPool().getPlayers().contains(player.getUniqueId())) {
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
        plugin.getDataPool().getPlayers().forEach(other -> event.getRecipients().remove(plugin.getServer().getPlayer(other)));
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

    public void applyDeath(Player player) {
        plugin.getReviveTasks().put(player, plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, () -> {
            if (plugin.checkRevive(player)) {
                player.sendMessage(ChatColor.GREEN + "Has sido revivido por la bendición de los dioses");
            }
        }, 20 * 60 * 10));
        player.getActivePotionEffects().clear();
        player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, Integer.MAX_VALUE, 1, false, false));
        player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, 20 * 30, 8, false, false));
        player.setGameMode(GameMode.ADVENTURE);
        player.setHealth(1F);
        player.setFoodLevel(20);
        player.setFireTicks(0);
        player.setSaturation(20);
        plugin.getServer().getScheduler().runTask(plugin, () -> PluginUtils.sendBorderEffect(player));
    }

    public void findNearest(Player player) {
        TitleUtils.sendTitle(player, 20, 300, 20, ChatColor.RED + ChatColor.BOLD.toString() + "¡ESTAS MUERTO!", ChatColor.GRAY + "Busca una cruz para revivir");
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
        if (plugin.getDataPool().getPlayers().contains(player.getUniqueId())) {
            ins.setCancelled(true);
            return true;
        }
        return false;
    }
}