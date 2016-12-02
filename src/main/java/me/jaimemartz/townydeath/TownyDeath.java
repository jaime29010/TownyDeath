package me.jaimemartz.townydeath;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.google.gson.stream.JsonReader;
import me.jaimemartz.faucet.ConfigUtil;
import me.jaimemartz.townydeath.data.JsonDataPool;
import me.jaimemartz.townydeath.data.JsonLocation;
import me.jaimemartz.townydeath.event.PlayerReviveEvent;
import me.jaimemartz.townydeath.utils.PluginUtils;
import me.jaimemartz.townydeath.utils.TitleUtils;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.*;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

public final class TownyDeath extends JavaPlugin {
    private Map<Player, Integer> reviveTasks = new HashMap<>();
    private Map<Player, Integer> findTasks = new HashMap<>();
    private Map<Player, Integer> titleTasks = new HashMap<>();
    private List<Entity> entities = new LinkedList<>();
    private List<UUID> tpBypass = new ArrayList<>();
    private FileConfiguration config;
    private JsonDataPool database;
    private Economy economy;
    private ItemStack item;
    private Location spawn;
    private Gson gson;

    public static int SAVE_INTERVAL = 10;

    @Override
    public void onEnable() {
        //Loading the config
        this.getConfig();
        spawn = new Location(
                getServer().getWorld(config.getString("spawn.world")),
                config.getInt("spawn.x"),
                config.getInt("spawn.y"),
                config.getInt("spawn.z")
        );

        //Setting up gson
        GsonBuilder builder = new GsonBuilder();
        builder.setPrettyPrinting();
        builder.enableComplexMapKeySerialization();

        //fix for map key that is null
        //gson does UUID.fromString even if the object is null, and it does not accept nulls
        builder.registerTypeAdapter(UUID.class, (JsonDeserializer<UUID>) (element, type, context) -> {
            if (element.isJsonNull() || element.getAsString().equals("null")) {
                return null;
            }
            return UUID.fromString(element.getAsString());
        });
        gson = builder.create();

        //Loading database
        File file = new File(getDataFolder(), "data.json");
        if (file.exists()) {
            getLogger().info("Database exists, reading data...");
            try (JsonReader reader = new JsonReader(new FileReader(file))) {
                database = gson.fromJson(reader, JsonDataPool.class);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            getLogger().fine("Database does not exist, it will be created on server shutdown");
            database = new JsonDataPool();
        }

        //Database save task
        getLogger().info(String.format("The database will be saved every %s minutes", SAVE_INTERVAL));
        new BukkitRunnable() {
            @Override
            public void run() {
                getLogger().info("Periodically saving database...");
                saveDatabase();
            }
        }.runTaskTimerAsynchronously(this, SAVE_INTERVAL * 60 * 20, SAVE_INTERVAL * 60 * 20);

        //Setting up the item
        item = new ItemStack(Material.PAPER);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.GREEN + "Botiquín");
        meta.setLore(Collections.singletonList(ChatColor.GREEN + "Haz click con este item en un fantasma para revivirlo"));
        item.setItemMeta(meta);

        //Caching the entities
        entities.addAll(database.getEntities().stream().map(this::getEntityByUniqueId).filter(o -> o != null).collect(Collectors.toList()));

        //Setting up vault
        RegisteredServiceProvider<Economy> service = getServer().getServicesManager().getRegistration(Economy.class);
        economy = service.getProvider();

        //Setting command and events
        getCommand("townydeath").setExecutor(new TownyCommand(this));
        getServer().getPluginManager().registerEvents(new PlayerListener(this), this);

        if (config.getBoolean("features.spawn-command.enabled")) {
            getCommand("spawn").setExecutor(new SpawnCommand(this));
        }
    }

    @Override
    public void onDisable() {
        getServer().getScheduler().cancelTasks(this);
        if (database != null) {
            getLogger().info("Saving database...");
            saveDatabase();
        } else {
            getLogger().info("Database is null, not saving database...");
        }
    }

    private void saveDatabase() {
        try (Writer writer = new FileWriter(new File(getDataFolder(), "data.json"))) {
            String output = gson.toJson(database, JsonDataPool.class);
            writer.write(output);
        } catch (IOException e) {
            getLogger().severe("Something went terribly wrong, couldn't save the database");
            e.printStackTrace();
        }
    }

    @Override
    public FileConfiguration getConfig() {
        if (config == null) {
            reloadConfig();
        }
        return config;
    }

    @Override
    public void reloadConfig() {
        config = ConfigUtil.loadConfig("config.yml", this);
    }

    @Override
    public void saveDefaultConfig() {
        this.reloadConfig();
    }

    @Override
    public void saveConfig() {
        ConfigUtil.saveConfig(config, "config.yml", this);
    }

    public List<Entity> getEntitiesCache() {
        return entities;
    }

    public JsonDataPool getDataPool() {
        return database;
    }

    public Location getSpawnPoint() {
        return spawn;
    }

    public ItemStack getHealer() {
        return item;
    }

    public Economy getEconomy() {
        return economy;
    }

    public Map<Player, Integer> getReviveTasks() {
        return reviveTasks;
    }

    public Map<Player, Integer> getFindTasks() {
        return findTasks;
    }

    public List<UUID> getTpBypass() {
        return tpBypass;
    }

    public Entity getEntityByUniqueId(UUID uniqueId) {
        for (World world : Bukkit.getWorlds()) {
            for (Entity entity : world.getEntities()) {
                if (entity.getUniqueId().equals(uniqueId)) {
                    return entity;
                }
            }
        }
        return null;
    }

    public void applyDeath(Player player) {
        database.getDied().add(player.getUniqueId());
        reviveTasks.put(player, getServer().getScheduler().scheduleSyncDelayedTask(this, () -> {
            if (checkRevive(player)) {
                player.sendMessage(ChatColor.GREEN + "Has sido revivido por la bendición de los dioses");
            }
        }, 20 * 60 * 10));
        titleTasks.put(player, getServer().getScheduler().scheduleSyncRepeatingTask(this, () -> {

        }, 0, 20 * 30));
        TitleUtils.sendTitle(player, 20, 300, 20, ChatColor.RED + ChatColor.BOLD.toString() + "¡ESTAS MUERTO!", ChatColor.GRAY + "Busca una cruz para revivir");
        player.getActivePotionEffects().clear();
        player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, Integer.MAX_VALUE, 1, false, false));
        player.setGameMode(GameMode.ADVENTURE);
        player.setHealth(1F);
        player.setFoodLevel(20);
        player.setFireTicks(0);
        player.setSaturation(20);
        getServer().getScheduler().runTask(this, () -> PluginUtils.sendBorderEffect(player));
    }

    public boolean checkRevive(Player player) {
        if (database.getDied().contains(player.getUniqueId())) {
            PlayerReviveEvent call = new PlayerReviveEvent(player);
            getServer().getPluginManager().callEvent(call);
            if (call.isCancelled()) return false;

            database.getDied().remove(player.getUniqueId());
            database.getRevived().put(player, JsonLocation.fromBukkit(player.getLocation()));
            player.setHealth(0);
            player.setGameMode(GameMode.SURVIVAL);
            PluginUtils.removeBorderEffect(player);
            checkClear(player);
            return true;
        }
        return false;
    }

    public void checkClear(Player player) {
        if (reviveTasks.containsKey(player)) {
            int taskId = reviveTasks.remove(player);
            getServer().getScheduler().cancelTask(taskId);
            getLogger().info(String.format("Cancelled task %s for player %s", taskId, player.getName()));
        }

        if (findTasks.containsKey(player)) {
            int taskId = findTasks.remove(player);
            getServer().getScheduler().cancelTask(taskId);
            getLogger().info(String.format("Cancelled task %s for player %s", taskId, player.getName()));
        }

        if (titleTasks.containsKey(player)) {
            int taskId = titleTasks.remove(player);
            getServer().getScheduler().cancelTask(taskId);
            getLogger().info(String.format("Cancelled task %s for player %s", taskId, player.getName()));
        }
    }

    public Entity getNearest(Player player) {
        double distance = 200;
        Entity closest = null;

        for (Entity other : entities) {
            if (other == null) continue;
            double dist = other.getLocation().distance(player.getLocation());
            if (distance == 200 || dist < distance) {
                distance = dist;
                closest = other;
            }
        }

        if (closest == null) {
            return entities.get(ThreadLocalRandom.current().nextInt(entities.size()));
        } else {
            return closest;
        }
    }

    public void safeTeleport(Player player, Location target) {
        tpBypass.add(player.getUniqueId());
        player.teleport(target);
        getServer().getScheduler().runTaskLater(this, () -> {
            tpBypass.remove(player.getUniqueId());
        }, 20 * 5);
    }
}
