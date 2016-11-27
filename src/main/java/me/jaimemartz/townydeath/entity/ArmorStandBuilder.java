package me.jaimemartz.townydeath.entity;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

public class ArmorStandBuilder {
    private final ArmorStand stand;
    public ArmorStandBuilder(World world, double x, double y, double z) {
        this(new Location(world, x, y, z));
    }

    public ArmorStandBuilder(Location loc) {
        stand = (ArmorStand) loc.getWorld().spawnEntity(loc, EntityType.ARMOR_STAND);
    }

    public ArmorStandBuilder small(boolean small) {
        stand.setSmall(small);
        return this;
    }

    public ArmorStandBuilder visible(boolean visible) {
        stand.setVisible(visible);
        return this;
    }

    public ArmorStandBuilder gravity(boolean gravity) {
        stand.setGravity(gravity);
        return this;
    }

    public ArmorStandBuilder marker(boolean marker) {
        stand.setMarker(marker);
        return this;
    }

    public ArmorStandBuilder base(boolean base) {
        stand.setBasePlate(base);
        return this;
    }

    public ArmorStandBuilder arms(boolean arms) {
        stand.setArms(arms);
        return this;
    }

    public ArmorStandBuilder helmet(ItemStack item) {
        stand.setHelmet(item);
        return this;
    }

    public ArmorStandBuilder chestplate(ItemStack item) {
        stand.setChestplate(item);
        return this;
    }

    public ArmorStandBuilder leggings(ItemStack item) {
        stand.setLeggings(item);
        return this;
    }

    public ArmorStandBuilder boots(ItemStack item) {
        stand.setBoots(item);
        return this;
    }

    public ArmorStandBuilder health(double health) {
        stand.setHealth(health);
        return this;
    }

    public ArmorStand getEntity() {
        return stand;
    }
}
