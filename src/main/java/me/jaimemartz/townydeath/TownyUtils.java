package me.jaimemartz.townydeath;

import com.palmergames.bukkit.towny.exceptions.NotRegisteredException;
import com.palmergames.bukkit.towny.exceptions.TownyException;
import com.palmergames.bukkit.towny.object.*;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class TownyUtils {
    public static String getName(Town town) {
        if (town == null) {
            return "None";
        }
        return town.getName();
    }

    public static Town getTown(Location location) {
        try {
            TownBlock block = TownyUniverse.getTownBlock(location);
            if (block == null) {
                return null;
            }
            return block.getTown();
        } catch (NotRegisteredException e) {
            return null;
        }
    }

    public static Town getTown(WorldCoord coord) {
        try {
            TownBlock block = coord.getTownBlock();
            return block.getTown();
        } catch (NotRegisteredException e) {
            return null;
        }
    }

    public static boolean townEquals(Town first, Town second) {
        if (first == null) {
            return (second == null);
        }
        return first.equals(second);
    }

    public static Resident getResident(Player player) {
        try {
            return TownyUniverse.getDataSource().getResident(player.getName());
        } catch (NotRegisteredException e) {
            return null;
        }
    }

    public static Town getTown(Player player) {
        try {
            Resident resident = TownyUniverse.getDataSource().getResident(player.getName());
            return resident.getTown();
        } catch (NotRegisteredException e) {
            return null;
        }
    }

    public static Town getTown(Resident resident) {
        try {
            return resident.getTown();
        } catch (NotRegisteredException e) {
            return null;
        }
    }

    public static Town getTown(String name) {
        try {
            return TownyUniverse.getDataSource().getTown(name);
        } catch (NotRegisteredException e) {
            return null;
        }
    }

    public static Town getTown(TownBlock block) {
        try {
            return block.getTown();
        } catch (NotRegisteredException e) {
            return null;
        }
    }

    public static Player getPlayer(Resident resident) {
        try {
            return TownyUniverse.getPlayer(resident);
        } catch (TownyException e) {
            return null;
        }
    }

    public static Nation getNation(Town town) {
        try {
            return town.getNation();
        } catch (NotRegisteredException e) {
            return null;
        }
    }
}
