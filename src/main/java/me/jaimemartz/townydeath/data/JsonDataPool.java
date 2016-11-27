package me.jaimemartz.townydeath.data;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class JsonDataPool {
    private final List<UUID> entities;
    private final List<UUID> players;

    public JsonDataPool() {
        entities = new ArrayList<>();
        players = new ArrayList<>();
    }

    public List<UUID> getEntities() {
        synchronized (entities) {
            return entities;
        }
    }

    public List<UUID> getPlayers() {
        synchronized (players) {
            return players;
        }
    }
}
