package net.plasmere.streamline.utils;

import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.config.Configuration;
import net.plasmere.streamline.config.Config;
import net.plasmere.streamline.config.ConfigUtils;
import net.plasmere.streamline.config.MessageConfUtils;
import net.plasmere.streamline.objects.Player;

import java.io.IOException;
import java.util.*;

public class PlayerUtils {
    private static final List<Player> stats = new ArrayList<>();
    private static final Configuration message = Config.getMess();

    private static final LuckPerms api = LuckPermsProvider.get();

    public static List<Player> getStats() {
        return stats;
    }

    public static Player getStat(ProxiedPlayer player) {
        try {
            for (Player stat : stats) {
                if (stat.player.equals(player)) {
                    return stat;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        Player p = new Player(player.getName());

        addStats(p);

        return p;
    }

    public static Player getStat(CommandSender player) {
        try {
            for (Player stat : stats) {
                if (stat.player.equals(player)) {
                    return stat;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        Player p = new Player(player.getName());

        addStats(p);

        return p;
    }

    public static Player getStat(String name) {
        try {
            for (Player stat : stats) {
                if (stat.player.getName().equals(name)) {
                    return stat;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        Player player = new Player(name);

        addStats(player);

        return player;
    }

    public static List<Player> transposeList(List<ProxiedPlayer> players){
        List<Player> ps = new ArrayList<>();
        for (ProxiedPlayer player : players){
            ps.add(PlayerUtils.getStat(player));
        }

        return ps;
    }

    public static String getOffOnDisplay(Player player){
        if (player.online) {
            return MessageConfUtils.online.replace("%player%", player.displayName);
        } else {
            return MessageConfUtils.offline.replace("%player%", player.displayName);
        }
    }

    public static String getOffOnReg(Player player){
        if (player.online) {
            return MessageConfUtils.online.replace("%player%", player.latestName);
        } else {
            return MessageConfUtils.offline.replace("%player%", player.latestName);
        }
    }

    public static boolean isStats(Player stat){
        return stats.contains(stat);
    }

    public static void reloadStats(Player stat) {
        stats.remove(getStat(stat.player));
        stats.add(stat);
    }

    public static void createStat(Player player) {
        try {
            Player stat = new Player(player);

            addStats(stat);

            if (ConfigUtils.statsTell) {
                MessagingUtils.sendStatUserMessage(stat, player, player, create);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void addStats(Player stat){
        stats.add(stat);
    }

    public static void removeStat(Player stat){
        try {
            stat.saveInfo();
        } catch (IOException e) {
            e.printStackTrace();
        }
        stats.remove(stat);
    }

    public static void info(Player sender, Player of){
        Player stat = getStat(of);

        if (! isStats(stat) || stat == null) {
            MessagingUtils.sendBUserMessage(sender, noStatsFound);
            return;
        }

        if (! sender.hasPermission(ConfigUtils.comBStatsPerm)) {
            MessagingUtils.sendBUserMessage(sender, noPermission);
        }

        MessagingUtils.sendStatUserMessage(stat, sender, sender, info);
    }

    // No stats.
    public static final String noStatsFound = message.getString("stats.no-stats");
    // Not high enough permissions.
    public static final String noPermission = message.getString("stats.no-permission");
    // Create.
    public static final String create = message.getString("stats.create");
    // Info.
    public static final String info = message.getString("stats.info");
}
