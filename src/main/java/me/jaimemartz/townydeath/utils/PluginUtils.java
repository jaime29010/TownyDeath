package me.jaimemartz.townydeath.utils;

import net.minecraft.server.v1_8_R3.*;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class PluginUtils {
    public static void sendWorldEnvironment(Player player, World.Environment environment){
        CraftPlayer craftPlayer = (CraftPlayer) player;
        CraftWorld world = (CraftWorld) player.getWorld();
        Location location = player.getLocation();
        Chunk chunk = location.getChunk();

        PacketPlayOutRespawn packet = new PacketPlayOutRespawn(environment.getId(), EnumDifficulty.getById(world.getDifficulty().getValue()), WorldType.NORMAL, WorldSettings.EnumGamemode.getById(player.getGameMode().getValue()));
        craftPlayer.getHandle().playerConnection.sendPacket(packet);

        int viewDistance = Bukkit.getViewDistance();

        int xMin = chunk.getX() - viewDistance;
        int xMax = chunk.getX() + viewDistance;
        int zMin = chunk.getZ() - viewDistance;
        int zMax = chunk.getZ() + viewDistance;

        for (int x = xMin; x < xMax; ++x){
            for (int z = zMin; z < zMax; ++z){
                world.refreshChunk(x, z);
            }
        }

        player.updateInventory();
    }

    public static void sendBorderEffect(Player player) {
        CraftPlayer craftPlayer = (CraftPlayer) player;

        WorldBorder border = new WorldBorder();
        border.setSize(1);
        border.setCenter(player.getLocation().getX() + 10000, player.getLocation().getZ() + 10000);
        craftPlayer.getHandle().playerConnection.sendPacket(new PacketPlayOutWorldBorder(border, PacketPlayOutWorldBorder.EnumWorldBorderAction.INITIALIZE));
    }

    public static void removeBorderEffect(Player player) {
        CraftPlayer craftPlayer = (CraftPlayer) player;

        WorldBorder border = new WorldBorder();
        border.setSize(30000000);
        border.setCenter(player.getLocation().getX(), player.getLocation().getZ());
        craftPlayer.getHandle().playerConnection.sendPacket(new PacketPlayOutWorldBorder(border, PacketPlayOutWorldBorder.EnumWorldBorderAction.INITIALIZE));
    }

    public static List<Entity> getEntitiesAroundPoint(Location location, double radius) {
        List<Entity> entities = new ArrayList<>();
        World world = location.getWorld();

        // To find chunks we use chunk coordinates (not block coordinates!)
        int smallX = MathHelper.floor((location.getX() - radius) / 16.0D);
        int bigX = MathHelper.floor((location.getX() + radius) / 16.0D);
        int smallZ = MathHelper.floor((location.getZ() - radius) / 16.0D);
        int bigZ = MathHelper.floor((location.getZ() + radius) / 16.0D);

        for (int x = smallX; x <= bigX; x++) {
            for (int z = smallZ; z <= bigZ; z++) {
                if (world.isChunkLoaded(x, z)) {
                    entities.addAll(Arrays.asList(world.getChunkAt(x, z).getEntities())); // Add all entities from this chunk to the list
                }
            }
        }

        // Remove the entities that are within the box above but not actually in the sphere we defined with the radius and location
        // This code below could probably be replaced in Java 8 with a stream -> filter
        Iterator<Entity> entityIterator = entities.iterator(); // Create an iterator so we can loop through the list while removing entries
        while (entityIterator.hasNext()) {
            if (entityIterator.next().getLocation().distanceSquared(location) > radius * radius) { // If the entity is outside of the sphere...
                entityIterator.remove(); // Remove it
            }
        }
        return entities;
    }
}
