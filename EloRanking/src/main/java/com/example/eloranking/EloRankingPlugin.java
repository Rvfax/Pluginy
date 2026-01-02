package com.example.eloranking;

import com.example.eloranking.command.EloCommand;
import com.example.eloranking.listener.PlayerListener;
import com.example.eloranking.manager.ConfigManager;
import com.example.eloranking.manager.DataManager;
import com.example.eloranking.manager.EloManager;
import com.example.eloranking.placeholder.EloExpansion;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

public class EloRankingPlugin extends JavaPlugin {
    private ConfigManager configManager;
    private DataManager dataManager;
    private EloManager eloManager;
    private BukkitTask saveTask;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        configManager = new ConfigManager(this);
        configManager.load();
        dataManager = new DataManager(this);
        eloManager = new EloManager(this, configManager, dataManager);
        eloManager.loadAll(dataManager.loadAll(configManager.getStartingElo()));

        PluginManager pluginManager = getServer().getPluginManager();
        pluginManager.registerEvents(new PlayerListener(eloManager), this);

        PluginCommand eloCommand = getCommand("elo");
        if (eloCommand != null) {
            EloCommand executor = new EloCommand(this, eloManager);
            eloCommand.setExecutor(executor);
            eloCommand.setTabCompleter(executor);
        }

        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            new EloExpansion(this, eloManager).register();
            getLogger().info("PlaceholderAPI detected - enabling elo placeholders.");
        } else {
            getLogger().warning("PlaceholderAPI not found - placeholders disabled.");
        }

        for (Player player : Bukkit.getOnlinePlayers()) {
            eloManager.getOrCreate(player.getUniqueId(), player.getName());
        }

        startAutoSave();
    }

    @Override
    public void onDisable() {
        stopAutoSave();
        if (eloManager != null) {
            eloManager.saveAllSync();
        }
    }

    public void reloadPlugin() {
        stopAutoSave();
        reloadConfig();
        configManager.load();
        dataManager.load();
        eloManager.loadAll(dataManager.loadAll(configManager.getStartingElo()));
        for (Player player : Bukkit.getOnlinePlayers()) {
            eloManager.getOrCreate(player.getUniqueId(), player.getName());
        }
        startAutoSave();
        getLogger().info("EloRanking configuration reloaded.");
    }

    private void startAutoSave() {
        long interval = configManager.getSaveIntervalSeconds() * 20L;
        if (interval <= 0) {
            return;
        }
        stopAutoSave();
        saveTask = Bukkit.getScheduler().runTaskTimerAsynchronously(this, () -> eloManager.saveAllSync(), interval, interval);
    }

    private void stopAutoSave() {
        if (saveTask != null) {
            saveTask.cancel();
            saveTask = null;
        }
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public EloManager getEloManager() {
        return eloManager;
    }
}
