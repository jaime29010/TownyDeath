package me.jaimemartz.townydeath.utils;

import com.mojang.authlib.GameProfile;
import net.minecraft.server.v1_8_R3.*;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.UUID;

public class NPC extends EntityPlayer {
    public NPC(GameProfile gameprofile, Location location) {
        super(MinecraftServer.getServer(),
                ((CraftWorld) location.getWorld()).getHandle(),
                gameprofile,
                new PlayerInteractManager(((CraftWorld) location.getWorld()).getHandle()));
        playerInteractManager.b(WorldSettings.EnumGamemode.SURVIVAL);
        NetworkManager manager = new NetworkManager(EnumProtocolDirection.CLIENTBOUND);
        playerConnection = new PlayerConnection(server, manager, this) {
            @Override
            public void sendPacket(Packet packet) {
            }

            @Override
            public void disconnect(String s) {
            }
        };
        manager.a(playerConnection);
        setLocation(location.getX(),
                location.getY(),
                location.getZ(),
                (float) Math.random() * 5,
                (float) Math.random() * 5);
    }

    @Override
    public boolean damageEntity(DamageSource damagesource, float f) {
        return false;
    }

    private static GameProfile profile(Player player, boolean nametag) {
        GameProfile profile;
        if (nametag) {
            profile = new GameProfile(player.getUniqueId(), player.getName());
        } else {
            profile = new GameProfile(UUID.randomUUID(), "");
        }
        profile.getProperties().putAll(((CraftPlayer) player).getProfile().getProperties());
        return profile;
    }

    public static NPC newNPC(Location location, Player player, boolean nametag) {
        return new NPC(profile(player, nametag), location).name(nametag ? player.getName() : "");
    }

    public NPC name(String name) {
        this.setCustomName(name);
        this.setCustomNameVisible(true);
        return this;
    }

    public NPC spawn(Plugin plugin, Location location, boolean nametag) {
        if (!nametag) {
            PacketPlayOutPlayerInfo packet = new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.ADD_PLAYER, this);
            for (Player player : Bukkit.getOnlinePlayers()) {
                ((CraftPlayer) player).getHandle().playerConnection.sendPacket(packet);
            }
        }
        ((CraftWorld) location.getWorld()).getHandle().addEntity(this);
        if (!nametag) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    PacketPlayOutPlayerInfo packet = new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.REMOVE_PLAYER, NPC.this);
                    for (Player player : Bukkit.getOnlinePlayers()) {
                        ((CraftPlayer) player).getHandle().playerConnection.sendPacket(packet);
                    }
                }
            }.runTaskLater(plugin, 2);
        }
        return this;
    }

    public NPC bed() {
        Location location = new Location(world.getWorld(), locX, locY - 2, locZ, yaw, pitch);
        PacketPlayOutEntity.PacketPlayOutRelEntityMove move = new PacketPlayOutEntity.PacketPlayOutRelEntityMove(
                getId(), (byte) 0, (byte) 2, (byte) 0, false
        );
        PacketPlayOutBed bed = new PacketPlayOutBed(this, new BlockPosition(
                location.getX(), location.getY(), location.getZ()
        ));
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.sendBlockChange(location, Material.BED_BLOCK, (byte) 8);
            ((CraftPlayer) player).getHandle().playerConnection.sendPacket(bed);
            ((CraftPlayer) player).getHandle().playerConnection.sendPacket(move);
        }
        return this;
    }

    public NPC remove(Location location, Material material) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.sendBlockChange(location, material, location.getBlock().getData());
        }
        ((CraftWorld) location.getWorld()).getHandle().removeEntity(this);
        return this;
    }
}