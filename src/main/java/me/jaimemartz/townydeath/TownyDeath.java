package me.jaimemartz.townydeath;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.google.gson.stream.JsonReader;
import me.jaimemartz.faucet.ConfigUtil;
import me.jaimemartz.townydeath.data.JsonDataPool;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.*;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

public final class TownyDeath extends JavaPlugin {
    public static int SAVE_INTERVAL = 10;
    private FileConfiguration config;
    private JsonDataPool database;
    private Location spawn;
    private Gson gson;

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

        /*
        new BukkitRunnable() {
            @Override
            public void run() {
                database.getPlayers().stream()
                        .map(Bukkit::getPlayer)
                        .filter(o -> o != null)
                        .forEach(player -> updateCompass(player));
            }
        }.runTaskTimer(this, 0, 20 * 15);
        */
        getCommand("townydeath").setExecutor(new TownyCommand(this));
        getServer().getPluginManager().registerEvents(new PlayerListener(this), this);

        if (config.getBoolean("features.spawn-command")) {
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

    public JsonDataPool getDataPool() {
        return database;
    }

    public Location getSpawnPoint() {
        return spawn;
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

    /*
    public Player getNearestPlayer(Player player) {
    double distNear = 0.0D;
    Player playerNear = null;
    for (Player player2 : Bukkit.getOnlinePlayers()) {
        //don't include the player that's checking
        if (player == player2) { continue; }
        //make sure same world (cannot measure distance between worlds)
        if (player.getWorld() != player2.getWorld()) { continue; }

        Location location2 = player.getLocation();
        double dist = location.distance(location2);
        if (playerNear == null || dist < distNear) {
            playerNear = player2;
            distNear = dist;
        }
    }
    return playerNear;
}

//you need to define the player variable
Player playerNear = getNearestPlayer(player);
if (playerNear != null) {
    player.setCompassTarget(playerNear);
}
     */
    /*
    public void updateCompass(Player player) {
        getLogger().info("Update of the compass of " + player.getName());
        List<Entity> entities = database.getEntities().stream().map(this::getEntityByUniqueId).collect(Collectors.toList());

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
            player.setCompassTarget(entities.get(ThreadLocalRandom.current().nextInt(entities.size())).getLocation());
        } else {
            player.setCompassTarget(closest.getLocation());
        }
    }*/
}
