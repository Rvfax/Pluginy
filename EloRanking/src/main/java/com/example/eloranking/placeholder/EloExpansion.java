package com.example.eloranking.placeholder;

import com.example.eloranking.EloRankingPlugin;
import com.example.eloranking.manager.EloManager;
import com.example.eloranking.model.PlayerData;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class EloExpansion extends PlaceholderExpansion {
    private final EloRankingPlugin plugin;
    private final EloManager eloManager;

    public EloExpansion(EloRankingPlugin plugin, EloManager eloManager) {
        this.plugin = plugin;
        this.eloManager = eloManager;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "elo";
    }

    @Override
    public @NotNull String getAuthor() {
        return plugin.getDescription().getAuthors().isEmpty() ? "Unknown" : plugin.getDescription().getAuthors().get(0);
    }

    @Override
    public @NotNull String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public @Nullable String onPlaceholderRequest(Player player, @NotNull String params) {
        if (player == null) {
            return "";
        }
        PlayerData data = eloManager.getOrCreate(player.getUniqueId(), player.getName());
        switch (params.toLowerCase()) {
            case "points":
                return String.valueOf(data.getElo());
            case "kills":
                return String.valueOf(data.getKills());
            case "deaths":
                return String.valueOf(data.getDeaths());
            case "rank":
                int rank = eloManager.getRank(player.getUniqueId());
                return rank > 0 ? String.valueOf(rank) : "-";
            default:
                return null;
        }
    }
}
