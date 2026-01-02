package com.example.eloranking.model;

import java.util.UUID;

public class PlayerData {
    private final UUID uuid;
    private String lastKnownName;
    private int elo;
    private int kills;
    private int deaths;

    public PlayerData(UUID uuid, String lastKnownName, int elo, int kills, int deaths) {
        this.uuid = uuid;
        this.lastKnownName = lastKnownName;
        this.elo = elo;
        this.kills = kills;
        this.deaths = deaths;
    }

    public UUID getUuid() {
        return uuid;
    }

    public String getLastKnownName() {
        return lastKnownName;
    }

    public void setLastKnownName(String lastKnownName) {
        this.lastKnownName = lastKnownName;
    }

    public int getElo() {
        return elo;
    }

    public void setElo(int elo) {
        this.elo = elo;
    }

    public int getKills() {
        return kills;
    }

    public void setKills(int kills) {
        this.kills = kills;
    }

    public int getDeaths() {
        return deaths;
    }

    public void setDeaths(int deaths) {
        this.deaths = deaths;
    }

    public void adjustElo(int amount) {
        this.elo += amount;
    }

    public void addKill() {
        this.kills++;
    }

    public void addDeath() {
        this.deaths++;
    }
}
