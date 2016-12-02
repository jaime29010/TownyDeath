package me.jaimemartz.townydeath.data;

import org.bukkit.entity.Player;

import java.util.*;

public final class JsonDataPool {
    private final Set<UUID> entities;
    private final Set<UUID> players;
    private final Map<Player, JsonLocation> revived;

    public JsonDataPool() {
        entities = Collections.synchronizedSet(new HashSet<>());
        players = Collections.synchronizedSet(new HashSet<>());
        revived = Collections.synchronizedMap(new HashMap<>());
    }

    public Set<UUID> getEntities() {
        return entities;
    }

    public Set<UUID> getDied() {
        return players;
    }

    public Map<Player, JsonLocation> getRevived() {
        return revived;
    }
}
