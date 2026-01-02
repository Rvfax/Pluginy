package com.example.eloranking.manager;

import com.example.eloranking.EloRankingPlugin;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;

public class ConfigManager {
    private final EloRankingPlugin plugin;
    private int startingElo;
    private int minChange;
    private int maxChange;
    private long saveIntervalSeconds;
    private long killCooldownSeconds;
    private String killMessage;
    private String deathMessage;
    private String cooldownMessage;
    private String zeroEloMessage;

    public ConfigManager(EloRankingPlugin plugin) {
        this.plugin = plugin;
    }

    public void load() {
        FileConfiguration config = plugin.getConfig();
        startingElo = config.getInt("starting-elo", 1000);
        minChange = Math.max(1, config.getInt("min-change", 1));
        maxChange = Math.max(minChange, config.getInt("max-change", 50));
        long interval = config.getLong("save-interval-seconds", 60L);
        saveIntervalSeconds = interval <= 0 ? 60L : interval;
        killCooldownSeconds = Math.max(0L, config.getLong("kill-cooldown-seconds", 300L));
        killMessage = config.getString("messaging.kill", "&a+{points} ELO &7(killed {victim})");
        deathMessage = config.getString("messaging.death", "&c-{points} ELO &7(by {killer})");
        cooldownMessage = config.getString("messaging.cooldown", "&eJuz zabiles {victim}. Odczekaj {time}s.");
        zeroEloMessage = config.getString("messaging.zero-elo", "&e{victim} nie ma juz ELO do stracenia.");
    }

    public void reload() {
        plugin.reloadConfig();
        load();
    }

    public int getStartingElo() {
        return startingElo;
    }

    public int getMinChange() {
        return minChange;
    }

    public int getMaxChange() {
        return maxChange;
    }

    public long getSaveIntervalSeconds() {
        return saveIntervalSeconds;
    }

    public long getKillCooldownSeconds() {
        return killCooldownSeconds;
    }

    public String formatKillMessage(int points, String victimName) {
        return formatMessage(killMessage, points, victimName, null);
    }

    public String formatDeathMessage(int points, String killerName) {
        return formatMessage(deathMessage, points, null, killerName);
    }

    public String formatCooldownMessage(String victimName, long secondsLeft) {
        if (cooldownMessage == null) {
            return "";
        }
        String formatted = cooldownMessage
                .replace("{victim}", victimName == null ? "Unknown" : victimName)
                .replace("{time}", String.valueOf(secondsLeft));
        return ChatColor.translateAlternateColorCodes('&', formatted);
    }

    public String getZeroEloMessage(String victimName) {
        if (zeroEloMessage == null) {
            return "";
        }
        String formatted = zeroEloMessage.replace("{victim}", victimName == null ? "Unknown" : victimName);
        return ChatColor.translateAlternateColorCodes('&', formatted);
    }

    private String formatMessage(String template, int points, String victimName, String killerName) {
        if (template == null) {
            return "";
        }
        String formatted = template.replace("{points}", String.valueOf(points));
        formatted = formatted.replace("{victim}", victimName == null ? "Unknown" : victimName);
        formatted = formatted.replace("{killer}", killerName == null ? "Unknown" : killerName);
        return ChatColor.translateAlternateColorCodes('&', formatted);
    }
}
