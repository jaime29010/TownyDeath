package me.jaimemartz.townydeath;

import com.palmergames.bukkit.towny.exceptions.TownyException;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Town;
import me.jaimemartz.townydeath.utils.PluginUtils;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.World;
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
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.*;

public class PlayerListener implements Listener {
    private List<UUID> tpBypass = new ArrayList<>();
    private Map<Player, Location> revived = new HashMap<>();

    private final TownyDeath plugin;
    public PlayerListener(TownyDeath plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void on(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (plugin.getDataPool().getPlayers().contains(player.getUniqueId())) {
            PluginUtils.sendBorderEffect(player);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void on(PlayerDeathEvent event) {
        Player player = event.getEntity();

        if (revived.containsKey(player)) {
            event.setDeathMessage(null);
            event.getDrops().clear();
            return;
        }

        plugin.getDataPool().getPlayers().add(player.getUniqueId());
        player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, Integer.MAX_VALUE, 0, false));
        player.setGameMode(GameMode.ADVENTURE);
        player.setHealth(1F);
        player.setFoodLevel(20);
        player.setSaturation(20);

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
                        plugin.getLogger().info("tp jail");
                        safeTeleport(player, target);
                        resident.setJailed(true);
                    } catch (TownyException ignored) {}
                }
            }
        }, 20);

        player.spigot().respawn();
        player.sendMessage(ChatColor.YELLOW + "Estas muerto, busca una cruz para revivir");

        if (player.getLocation().getWorld() != plugin.getServer().getWorlds().get(0)) {
            safeTeleport(player, plugin.getSpawnPoint());
        }

        PluginUtils.sendBorderEffect(player);
        /*
        plugin.getServer().getScheduler().runTask(plugin, () -> {
            player.setCompassTarget(plugin.getSpawnPoint());
            player.getInventory().setItem(4, new ItemStack(Material.COMPASS));
            ItemStack item = player.getInventory().getItem(4);
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName(ChatColor.GREEN + "Cruz mas cercana");
            meta.setLore(Collections.singletonList(ChatColor.GREEN + "Ve a la cruz mas cercana para revivir"));
            item.setItemMeta(meta);
            player.getInventory().setItem(4, item);
            player.updateInventory();
            plugin.getServer().getScheduler().runTask(plugin, () -> {
                plugin.updateCompass(player);
            });
        });
        */
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
    public void on(AsyncPlayerChatEvent event) {
        checkCancel(event);
        plugin.getDataPool().getPlayers().forEach(other -> event.getRecipients().remove(plugin.getServer().getPlayer(other)));
    }

    @EventHandler
    public void on(VehicleEnterEvent event) {
        if (event.getEntered() instanceof Player) {
            checkCancel((Player) event.getEntered(), event);
        }
    }

    @EventHandler
    public void on(PlayerPickupItemEvent event) {
        checkCancel(event);
    }

    @EventHandler
    public void on(PlayerDropItemEvent event) {
        checkCancel(event);
    }

    @EventHandler
    public void on(FoodLevelChangeEvent event) {
        checkCancel((Player) event.getEntity(), event);
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
    public void on(PlayerArmorStandManipulateEvent event) {
        if (plugin.getDataPool().getEntities().contains(event.getRightClicked().getUniqueId())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void on(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        Location target = revived.remove(player);
        if (target != null) {
            event.setRespawnLocation(target);
        }
    }

    @EventHandler
    public void on(PlayerInteractAtEntityEvent event) {
        Player player = event.getPlayer();
        if (plugin.getDataPool().getEntities().contains(event.getRightClicked().getUniqueId())) {
            if (plugin.getDataPool().getPlayers().contains(player.getUniqueId())) {
                plugin.getDataPool().getPlayers().remove(player.getUniqueId());
                revived.put(player, player.getLocation());
                player.setHealth(0);
                player.setGameMode(GameMode.SURVIVAL);
                PluginUtils.removeBorderEffect(player);
                player.sendMessage(ChatColor.GREEN + "Enhorabuena, has revivido");
            }
        }
        checkCancel(event);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void on(PlayerTeleportEvent event) {
        Player player = event.getPlayer();
        if (plugin.getDataPool().getPlayers().contains(player.getUniqueId()) && !tpBypass.contains(player.getUniqueId())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void on(PlayerInteractEvent event) {
        checkCancel(event);
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

    public void safeTeleport(Player player, Location location) {
        tpBypass.add(player.getUniqueId());
        player.teleport(location);
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            tpBypass.remove(player.getUniqueId());
        }, 20 * 5);
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
