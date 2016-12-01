package me.jaimemartz.townydeath.data;

import org.bukkit.entity.Player;

import java.util.*;

public final class JsonDataPool {
    private final List<UUID> entities;
    private final List<UUID> players;
    private final Map<Player, JsonLocation> revived;

    public JsonDataPool() {
        entities = Collections.synchronizedList(new ArrayList<>());
        players = Collections.synchronizedList(new ArrayList<>());
        revived = Collections.synchronizedMap(new HashMap<>());
    }

    public List<UUID> getEntities() {
        return entities;
    }

    public List<UUID> getDied() {
        return players;
    }

    public Map<Player, JsonLocation> getRevived() {
        return revived;
    }
}
