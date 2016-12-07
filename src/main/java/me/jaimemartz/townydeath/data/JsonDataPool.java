package me.jaimemartz.townydeath.data;

import java.util.*;

public final class JsonDataPool {
    private final Set<UUID> entities;
    private final Set<UUID> players;
    private final Map<UUID, JsonLocation> revived;

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

    public Map<UUID, JsonLocation> getRevived() {
        return revived;
    }
}
