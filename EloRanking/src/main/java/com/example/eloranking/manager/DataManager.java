package com.example.eloranking.manager;

import com.example.eloranking.model.PlayerData;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class DataManager {
    private final JavaPlugin plugin;
    private final File file;
    private final Object lock = new Object();
    private FileConfiguration data;

    public DataManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "elo.yml");
        ensureFile();
        load();
    }

    private void ensureFile() {
        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().severe("Unable to create elo.yml: " + e.getMessage());
            }
        }
    }

    public void load() {
        data = new YamlConfiguration();
        try {
            data.load(file);
        } catch (IOException | InvalidConfigurationException e) {
            plugin.getLogger().severe("Failed to load elo.yml: " + e.getMessage());
        }
    }

    public Map<UUID, PlayerData> loadAll(int defaultElo) {
        Map<UUID, PlayerData> results = new HashMap<>();
        if (data == null) {
            return results;
        }
        for (String key : data.getKeys(false)) {
            try {
                UUID uuid = UUID.fromString(key);
                int elo = data.getInt(key + ".elo", defaultElo);
                int kills = data.getInt(key + ".kills", 0);
                int deaths = data.getInt(key + ".deaths", 0);
                String name = data.getString(key + ".name", "Unknown");
                results.put(uuid, new PlayerData(uuid, name, elo, kills, deaths));
            } catch (IllegalArgumentException ex) {
                plugin.getLogger().warning("Skipping invalid UUID in elo.yml: " + key);
            }
        }
        return results;
    }

    public void savePlayer(PlayerData playerData) {
        if (playerData == null) {
            return;
        }
        synchronized (lock) {
            String key = playerData.getUuid().toString();
            data.set(key + ".name", playerData.getLastKnownName());
            data.set(key + ".elo", playerData.getElo());
            data.set(key + ".kills", playerData.getKills());
            data.set(key + ".deaths", playerData.getDeaths());
            saveFile();
        }
    }

    public void saveAll(Collection<PlayerData> players) {
        synchronized (lock) {
            if (data == null) {
                data = new YamlConfiguration();
            }
            for (PlayerData player : players) {
                String key = player.getUuid().toString();
                data.set(key + ".name", player.getLastKnownName());
                data.set(key + ".elo", player.getElo());
                data.set(key + ".kills", player.getKills());
                data.set(key + ".deaths", player.getDeaths());
            }
            saveFile();
        }
    }

    private void saveFile() {
        try {
            data.save(file);
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to save elo.yml: " + e.getMessage());
        }
    }
}
