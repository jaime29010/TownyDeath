package me.jaimemartz.townydeath.data;

import org.bukkit.Bukkit;
import org.bukkit.Location;

public final class JsonLocation {
    private final String world;
    private final double x, y, z;

    public JsonLocation(String world, double x, double y, double z) {
        this.world = world;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public String getWorld() {
        return world;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getZ() {
        return z;
    }

    public Location toBukkit() {
        return new Location(Bukkit.getWorld(world), x, y, z);
    }

    public static JsonLocation fromBukkit(Location location) {
        return new JsonLocation(location.getWorld().getName(), location.getX(), location.getY(), location.getZ());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        JsonLocation that = (JsonLocation) o;

        if (Double.compare(that.x, x) != 0) return false;
        if (Double.compare(that.y, y) != 0) return false;
        if (Double.compare(that.z, z) != 0) return false;
        return world != null ? world.equals(that.world) : that.world == null;

    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        result = world != null ? world.hashCode() : 0;
        temp = Double.doubleToLongBits(x);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(y);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(z);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        return result;
    }
}
