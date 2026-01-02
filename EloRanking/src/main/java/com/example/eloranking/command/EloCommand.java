package com.example.eloranking.command;

import com.example.eloranking.EloRankingPlugin;
import com.example.eloranking.manager.EloManager;
import com.example.eloranking.model.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class EloCommand implements CommandExecutor, TabCompleter {
    private static final String ADMIN_PERMISSION = "eloranking.admin";

    private final EloRankingPlugin plugin;
    private final EloManager eloManager;

    public EloCommand(EloRankingPlugin plugin, EloManager eloManager) {
        this.plugin = plugin;
        this.eloManager = eloManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            if (sender instanceof Player) {
                Player player = (Player) sender;
                sendStats(sender, player.getUniqueId(), player.getName());
            } else {
                sender.sendMessage(ChatColor.RED + "Usage: /" + label + " <player>");
            }
            return true;
        }

        if (args.length == 1 && args[0].equalsIgnoreCase("top")) {
            sendTopList(sender);
            return true;
        }

        if (args.length == 1 && !isAdminSubcommand(args[0])) {
            OfflinePlayer target = Bukkit.getOfflinePlayer(args[0]);
            sendStats(sender, target.getUniqueId(), target.getName());
            return true;
        }

        if (!sender.hasPermission(ADMIN_PERMISSION)) {
            sender.sendMessage(ChatColor.RED + "Brak uprawnien.");
            return true;
        }

        String sub = args[0].toLowerCase();
        switch (sub) {
            case "reload":
                plugin.reloadPlugin();
                sender.sendMessage(ChatColor.GREEN + "EloRanking przeladowany.");
                return true;
            case "set":
            case "add":
                if (args.length < 3) {
                    sender.sendMessage(ChatColor.RED + "Uzycie: /" + label + " " + sub + " <gracz> <wartosc>");
                    return true;
                }
                handleModify(sender, sub, args[1], args[2]);
                return true;
            default:
                sender.sendMessage(ChatColor.RED + "Nieznana podkomenda.");
                return true;
        }
    }

    private boolean isAdminSubcommand(String arg) {
        String lower = arg.toLowerCase();
        return lower.equals("reload") || lower.equals("set") || lower.equals("add");
    }

    private void sendStats(CommandSender viewer, UUID uuid, String fallbackName) {
        PlayerData data = eloManager.getPlayerData(uuid);
        if (data == null) {
            if (viewer instanceof Player && ((Player) viewer).getUniqueId().equals(uuid)) {
                data = eloManager.getOrCreate(uuid, fallbackName);
            } else {
                viewer.sendMessage(ChatColor.RED + "Brak danych dla podanego gracza.");
                return;
            }
        }
        String resolvedFallback = fallbackName != null ? fallbackName : data.getUuid().toString();
        String name = data.getLastKnownName() != null ? data.getLastKnownName() : resolvedFallback;
        viewer.sendMessage(ChatColor.GREEN + name + " ma " + ChatColor.AQUA + data.getElo() + " ELO.");
        viewer.sendMessage(ChatColor.GRAY + "Zabojstwa: " + data.getKills() + " | Zgony: " + data.getDeaths());
        int rank = eloManager.getRank(data.getUuid());
        if (rank > 0) {
            viewer.sendMessage(ChatColor.YELLOW + "Pozycja w rankingu: #" + rank);
        }
    }

    private void sendTopList(CommandSender sender) {
        List<PlayerData> topPlayers = eloManager.getTop10Players();
        if (topPlayers.isEmpty()) {
            sender.sendMessage(ChatColor.YELLOW + "Brak danych rankingowych.");
            return;
        }
        sender.sendMessage(ChatColor.GOLD + "Top 10 ELO:");
        int position = 1;
        for (PlayerData data : topPlayers) {
            String name = data.getLastKnownName() != null ? data.getLastKnownName() : data.getUuid().toString();
            sender.sendMessage(ChatColor.YELLOW + String.valueOf(position) + ". "
                    + ChatColor.AQUA + name + ChatColor.GRAY + " - "
                    + ChatColor.GREEN + data.getElo() + " ELO"
                    + ChatColor.GRAY + " (" + data.getKills() + "/" + data.getDeaths() + ")");
            position++;
        }
    }

    private void handleModify(CommandSender sender, String mode, String targetName, String valueRaw) {
        OfflinePlayer target = Bukkit.getOfflinePlayer(targetName);
        UUID uuid = target.getUniqueId();
        int value;
        try {
            value = Integer.parseInt(valueRaw);
        } catch (NumberFormatException ex) {
            sender.sendMessage(ChatColor.RED + "Wartosc musi byc liczba.");
            return;
        }

        String name = target.getName() != null ? target.getName() : targetName;
        if (mode.equalsIgnoreCase("set")) {
            eloManager.setElo(uuid, name, value);
            sender.sendMessage(ChatColor.GREEN + "Ustawiono ELO gracza " + name + " na " + value + ".");
        } else {
            eloManager.addElo(uuid, name, value);
            sender.sendMessage(ChatColor.GREEN + "Dodano " + value + " ELO graczowi " + name + ".");
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            List<String> options = new ArrayList<>();
            options.add("top");
            if (sender.hasPermission(ADMIN_PERMISSION)) {
                options.addAll(Arrays.asList("reload", "set", "add"));
            }
            options.removeIf(opt -> !opt.startsWith(args[0].toLowerCase()));
            return options;
        }
        if (args.length == 2 && (args[0].equalsIgnoreCase("set") || args[0].equalsIgnoreCase("add"))) {
            if (!sender.hasPermission(ADMIN_PERMISSION)) {
                return List.of();
            }
            List<String> names = new ArrayList<>();
            for (Player player : Bukkit.getOnlinePlayers()) {
                names.add(player.getName());
            }
            return names;
        }
        if (args.length == 3 && (args[0].equalsIgnoreCase("set") || args[0].equalsIgnoreCase("add"))) {
            if (!sender.hasPermission(ADMIN_PERMISSION)) {
                return List.of();
            }
            return List.of("50", "25", "10");
        }
        return List.of();
    }
}

