package com.example.eloranking.manager;

import com.example.eloranking.EloRankingPlugin;
import com.example.eloranking.model.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class EloManager {
    private final EloRankingPlugin plugin;
    private final ConfigManager configManager;
    private final DataManager dataManager;
    private final Map<UUID, PlayerData> cache = new ConcurrentHashMap<>();
    private final List<UUID> rankingCache = new ArrayList<>();
    private final Map<UUID, Map<UUID, Long>> killCooldowns = new ConcurrentHashMap<>();
    private boolean rankingDirty = true;

    public EloManager(EloRankingPlugin plugin, ConfigManager configManager, DataManager dataManager) {
        this.plugin = plugin;
        this.configManager = configManager;
        this.dataManager = dataManager;
    }

    public void loadAll(Map<UUID, PlayerData> loaded) {
        cache.clear();
        cache.putAll(loaded);
        rankingDirty = true;
    }

    public PlayerData getOrCreate(UUID uuid, String name) {
        PlayerData data = cache.computeIfAbsent(uuid, id -> new PlayerData(id, name, configManager.getStartingElo(), 0, 0));
        if (name != null && !name.equalsIgnoreCase(data.getLastKnownName())) {
            data.setLastKnownName(name);
        }
        return data;
    }

    public void handleKill(Player killer, Player victim) {
        UUID killerId = killer.getUniqueId();
        UUID victimId = victim.getUniqueId();

        if (isOnCooldown(killerId, victimId)) {
            long secondsLeft = getCooldownSecondsLeft(killerId, victimId);
            String cooldownMessage = configManager.formatCooldownMessage(victim.getName(), secondsLeft);
            if (!cooldownMessage.isEmpty()) {
                killer.sendMessage(cooldownMessage);
            }
            return;
        }

        PlayerData killerData = getOrCreate(killerId, killer.getName());
        PlayerData victimData = getOrCreate(victimId, victim.getName());

        if (victimData.getElo() <= 0) {
            String zeroMessage = configManager.getZeroEloMessage(victim.getName());
            if (!zeroMessage.isEmpty()) {
                killer.sendMessage(zeroMessage);
            }
            return;
        }

        int change = calculateEloChange(killerData.getElo(), victimData.getElo());
        int actualChange = Math.min(change, victimData.getElo());
        if (actualChange <= 0) {
            return;
        }

        killerData.adjustElo(actualChange);
        killerData.addKill();

        victimData.adjustElo(-actualChange);
        if (victimData.getElo() < 0) {
            victimData.setElo(0);
        }
        victimData.addDeath();

        String killMessage = configManager.formatKillMessage(actualChange, victim.getName());
        String deathMessage = configManager.formatDeathMessage(actualChange, killer.getName());

        if (!killMessage.isEmpty()) {
            killer.sendMessage(killMessage);
        }
        if (!deathMessage.isEmpty()) {
            victim.sendMessage(deathMessage);
        }

        markRankingDirty();
        applyCooldown(killerId, victimId);
        savePlayersAsync(killerData, victimData);
    }

    private void savePlayersAsync(PlayerData... players) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            for (PlayerData player : players) {
                dataManager.savePlayer(player);
            }
        });
    }

    private int calculateEloChange(int killerElo, int victimElo) {
        double expected = 1.0 / (1.0 + Math.pow(10, (victimElo - killerElo) / 400.0));
        double kFactor = configManager.getMaxChange();
        int delta = (int) Math.round(kFactor * (1 - expected));
        delta = Math.max(configManager.getMinChange(), delta);
        delta = Math.min(configManager.getMaxChange(), delta);
        return delta;
    }

    private boolean isOnCooldown(UUID killerId, UUID victimId) {
        long duration = configManager.getKillCooldownSeconds();
        if (duration <= 0) {
            return false;
        }
        Map<UUID, Long> targets = killCooldowns.get(killerId);
        if (targets == null) {
            return false;
        }
        Long expiry = targets.get(victimId);
        if (expiry == null) {
            return false;
        }
        if (expiry <= System.currentTimeMillis()) {
            targets.remove(victimId);
            return false;
        }
        return true;
    }

    private long getCooldownSecondsLeft(UUID killerId, UUID victimId) {
        Map<UUID, Long> targets = killCooldowns.get(killerId);
        if (targets == null) {
            return 0;
        }
        Long expiry = targets.get(victimId);
        if (expiry == null) {
            return 0;
        }
        long remainingMillis = Math.max(0L, expiry - System.currentTimeMillis());
        return Math.max(1L, (long) Math.ceil(remainingMillis / 1000.0));
    }

    private void applyCooldown(UUID killerId, UUID victimId) {
        long duration = configManager.getKillCooldownSeconds();
        if (duration <= 0) {
            return;
        }
        long expiry = System.currentTimeMillis() + duration * 1000L;
        killCooldowns.computeIfAbsent(killerId, id -> new ConcurrentHashMap<>()).put(victimId, expiry);
    }

    public void markRankingDirty() {
        rankingDirty = true;
    }

    public int getRank(UUID uuid) {
        rebuildRankingIfNeeded();
        int index = rankingCache.indexOf(uuid);
        return index >= 0 ? index + 1 : -1;
    }

    public List<PlayerData> getTopPlayers(int limit) {
        rebuildRankingIfNeeded();
        return rankingCache.stream()
                .limit(limit)
                .map(cache::get)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    public List<PlayerData> getTop10Players() {
        return getTopPlayers(10);
    }

    private void rebuildRankingIfNeeded() {
        if (!rankingDirty) {
            return;
        }
        Comparator<PlayerData> comparator = Comparator
                .comparingInt(PlayerData::getElo).reversed()
                .thenComparing(Comparator.comparingInt(PlayerData::getKills).reversed())
                .thenComparing(PlayerData::getLastKnownName, Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER));
        rankingCache.clear();
        rankingCache.addAll(cache.values().stream()
                .sorted(comparator)
                .map(PlayerData::getUuid)
                .collect(Collectors.toList()));
        rankingDirty = false;
    }

    public PlayerData getPlayerData(UUID uuid) {
        return cache.get(uuid);
    }

    public Collection<PlayerData> getAllPlayers() {
        return Collections.unmodifiableCollection(cache.values());
    }

    public void saveAllSync() {
        dataManager.saveAll(cache.values());
    }

    public void savePlayerSync(UUID uuid) {
        PlayerData data = cache.get(uuid);
        if (data != null) {
            dataManager.savePlayer(data);
        }
    }

    public void setElo(UUID uuid, String name, int value) {
        PlayerData data = getOrCreate(uuid, name);
        data.setElo(value);
        markRankingDirty();
        dataManager.savePlayer(data);
    }

    public void addElo(UUID uuid, String name, int value) {
        PlayerData data = getOrCreate(uuid, name);
        data.adjustElo(value);
        markRankingDirty();
        dataManager.savePlayer(data);
    }
}

