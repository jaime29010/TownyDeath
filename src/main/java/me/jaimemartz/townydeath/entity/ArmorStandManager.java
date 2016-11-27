package me.jaimemartz.townydeath.entity;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.ItemStack;

import java.util.LinkedHashSet;
import java.util.Set;

public class ArmorStandManager {
    public static Set<Entity> spawn(Location loc) {
        World world = loc.getWorld();
        double x = loc.getX(), y = loc.getY(), z = loc.getZ();
        Set<Entity> list = new LinkedHashSet<>();
        list.add(new ArmorStandBuilder(world, x + 0.62500, y - 2.67043, z)
                .small(true).gravity(false).visible(false)
                .helmet(new ItemStack(Material.STEP, 1, (byte) 3)).getEntity());
        list.add(new ArmorStandBuilder(world, x - 0.03125, y - 3.29543, z)
                .small(true).gravity(false).visible(false)
                .helmet(new ItemStack(Material.COBBLESTONE)).getEntity());
        list.add(new ArmorStandBuilder(world, x - 0.68750, y - 2.67043, z)
                .small(true).gravity(false).visible(false)
                .helmet(new ItemStack(Material.STEP, 1, (byte) 3)).getEntity());
        list.add(new ArmorStandBuilder(world, x + 0.34375, y - 2.17043, z)
                .small(true).gravity(false).visible(false)
                .helmet(new ItemStack(Material.COBBLESTONE)).getEntity());
        list.add(new ArmorStandBuilder(world, x + 0.34375, y - 2.29543, z)
                .small(true).gravity(false).visible(false)
                .helmet(new ItemStack(Material.STEP, 1, (byte) 3)).getEntity());
        list.add(new ArmorStandBuilder(world, x + 0.34375, y - 1.88918, z)
                .small(true).gravity(false).visible(false)
                .helmet(new ItemStack(Material.STEP, 1, (byte) 3)).getEntity());
        list.add(new ArmorStandBuilder(world, x - 0.40625, y - 2.29543, z)
                .small(true).gravity(false).visible(false)
                .helmet(new ItemStack(Material.STEP, 1, (byte) 3)).getEntity());
        list.add(new ArmorStandBuilder(world, x - 0.40625, y - 1.88918, z)
                .small(true).gravity(false).visible(false)
                .helmet(new ItemStack(Material.STEP, 1, (byte) 3)).getEntity());
        list.add(new ArmorStandBuilder(world, x - 0.03125, y - 3.73293, z)
                .small(true).gravity(false).visible(false)
                .helmet(new ItemStack(Material.COBBLESTONE)).getEntity());
        list.add(new ArmorStandBuilder(world, x + 0.18750, y - 2.67043, z)
                .small(true).gravity(false).visible(false)
                .helmet(new ItemStack(Material.COBBLESTONE)).getEntity());
        list.add(new ArmorStandBuilder(world, x - 0.25000, y - 2.67043, z)
                .small(true).gravity(false).visible(false)
                .helmet(new ItemStack(Material.COBBLESTONE)).getEntity());
        list.add(new ArmorStandBuilder(world, x - 0.03125, y - 2.85793, z)
                .small(true).gravity(false).visible(false)
                .helmet(new ItemStack(Material.STEP, 1, (byte) 3)).getEntity());
        list.add(new ArmorStandBuilder(world, x + 0.18750, y - 1.92043, z)
                .small(true).gravity(false).visible(false)
                .helmet(new ItemStack(Material.COBBLESTONE)).getEntity());
        list.add(new ArmorStandBuilder(world, x - 0.21875, y - 1.92043, z)
                .small(true).gravity(false).visible(false)
                .helmet(new ItemStack(Material.COBBLESTONE)).getEntity());
        list.add(new ArmorStandBuilder(world, x - 0.03125, y - 1.76418, z)
                .small(true).gravity(false).visible(false)
                .helmet(new ItemStack(Material.COBBLESTONE)).getEntity());
        list.add(new ArmorStandBuilder(world, x - 0.40625, y - 2.20168, z)
                .small(true).gravity(false).visible(false)
                .helmet(new ItemStack(Material.COBBLESTONE)).getEntity());
        return list;
    }
}
