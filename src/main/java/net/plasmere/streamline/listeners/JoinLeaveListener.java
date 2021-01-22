package net.plasmere.streamline.listeners;

import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.event.ServerConnectEvent;
import net.plasmere.streamline.StreamLine;
import net.plasmere.streamline.config.Config;
import net.plasmere.streamline.config.ConfigUtils;
import net.plasmere.streamline.config.MessageConfUtils;
import net.plasmere.streamline.objects.BungeeMassMessage;
import net.plasmere.streamline.objects.DiscordMessage;
import net.plasmere.streamline.objects.Guild;
import net.plasmere.streamline.objects.Player;
import net.plasmere.streamline.utils.GuildUtils;
import net.plasmere.streamline.utils.MessagingUtils;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;
import net.plasmere.streamline.utils.PlayerUtils;

import java.io.File;
import java.util.Objects;

public class JoinLeaveListener implements Listener {
    private final Configuration config = Config.getConf();
    private final StreamLine plugin;

    public JoinLeaveListener(StreamLine streamLine){
        this.plugin = streamLine;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onJoin(PostLoginEvent ev) {
        Player player = (Player) ev.getPlayer();

        try {
            for (ProxiedPlayer pl : StreamLine.getInstance().getProxy().getPlayers()){
                Player p = (Player) pl;
                if (GuildUtils.getGuild(p) == null && ! p.equals(player)) continue;
                if (GuildUtils.getGuild(p) != null) {
                    if (Objects.requireNonNull(GuildUtils.getGuild(p)).hasMember(player)) break;
                }
                if (GuildUtils.pHasGuild(player)) {
                    GuildUtils.addGuild(new Guild(player.getUniqueId(), false));
                }
                break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            if (PlayerUtils.getStat(player) == null) {
                File file = new File(StreamLine.getInstance().getDataFolder() + File.separator + "players" + File.separator + player.getUniqueId().toString() + ".properties");

                if (file.exists()) {
                    PlayerUtils.addStats(new Player(player, false));
                } else {
                    PlayerUtils.createStat(player);
                }
            }
        } catch (Exception e) {
          e.printStackTrace();
        }

        switch (ConfigUtils.moduleBPlayerJoins) {
            case "yes":
                MessagingUtils.sendBungeeMessage(new BungeeMassMessage(plugin.getProxy().getConsole(),
                        MessageConfUtils.bungeeOnline.replace("%player%", PlayerUtils.getOffOnReg(Objects.requireNonNull(PlayerUtils.getStat(player)))),
                        "streamline.staff"));
                break;
            case "staff":
                if (player.hasPermission("streamline.staff")) {
                    MessagingUtils.sendBungeeMessage(new BungeeMassMessage(plugin.getProxy().getConsole(),
                            MessageConfUtils.bungeeOnline.replace("%player%", PlayerUtils.getOffOnReg(Objects.requireNonNull(PlayerUtils.getStat(player)))),
                            "streamline.staff"));
                }
                break;
            case "no":
            default:
                break;
        }
        switch (ConfigUtils.moduleDPlayerJoins) {
            case "yes":
                MessagingUtils.sendDiscordEBMessage(new DiscordMessage(plugin.getProxy().getConsole(),
                        MessageConfUtils.discordOnlineEmbed,
                        MessageConfUtils.discordOnline.replace("%player%", PlayerUtils.getOffOnReg(Objects.requireNonNull(PlayerUtils.getStat(player)))),
                        ConfigUtils.textChannelBJoins));
                break;
            case "staff":
                if (player.hasPermission("streamline.staff")) {
                    MessagingUtils.sendDiscordEBMessage(new DiscordMessage(plugin.getProxy().getConsole(),
                            MessageConfUtils.discordOnlineEmbed,
                            MessageConfUtils.discordOnline.replace("%player%", PlayerUtils.getOffOnReg(Objects.requireNonNull(PlayerUtils.getStat(player)))),
                            ConfigUtils.textChannelBJoins));
                }
                break;
            case "no":
            default:
                break;
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onServer(ServerConnectEvent ev){
        Player player = (Player) ev.getPlayer();

        boolean hasServer = false;
        ServerInfo server = null;

        for (ServerInfo s : StreamLine.getInstance().getProxy().getServers().values()) {
            String sv = s.getName();
            if (player.hasPermission(ConfigUtils.redirectPre + sv)) {
                hasServer = true;
                server = s;
                break;
            }
        }

        if (!hasServer) {
            server = StreamLine.getInstance().getProxy().getServerInfo(ConfigUtils.redirectMain);
        }
        if (! ev.getReason().equals(ServerConnectEvent.Reason.JOIN_PROXY)){
            return;
        }
        ev.setTarget(server);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onLeave(PlayerDisconnectEvent ev) {
        Player player = (Player) ev.getPlayer();

        try {
            for (ProxiedPlayer pl : StreamLine.getInstance().getProxy().getPlayers()){
                Player p = (Player) pl;
                if (Objects.requireNonNull(GuildUtils.getGuild(player)).hasMember(p) && ! p.equals(player)) break;


                GuildUtils.removeGuild(Objects.requireNonNull(GuildUtils.getGuild(player)));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            PlayerUtils.removeStat(Objects.requireNonNull(PlayerUtils.getStat(player)));
        } catch (Exception e) {
            e.printStackTrace();
        }

        switch (ConfigUtils.moduleBPlayerLeaves) {
            case "yes":
                MessagingUtils.sendBungeeMessage(new BungeeMassMessage(plugin.getProxy().getConsole(),
                        MessageConfUtils.bungeeOffline.replace("%player%", PlayerUtils.getOffOnReg(Objects.requireNonNull(PlayerUtils.getStat(player)))),
                        "streamline.staff"));
                break;
            case "staff":
                if (player.hasPermission("streamline.staff")) {
                    MessagingUtils.sendBungeeMessage(new BungeeMassMessage(plugin.getProxy().getConsole(),
                            MessageConfUtils.bungeeOffline.replace("%player%", PlayerUtils.getOffOnReg(Objects.requireNonNull(PlayerUtils.getStat(player)))),
                            "streamline.staff"));
                }
                break;
            case "no":
            default:
                break;
        }
        switch (ConfigUtils.moduleDPlayerLeaves) {
            case "yes":
                MessagingUtils.sendDiscordEBMessage(new DiscordMessage(plugin.getProxy().getConsole(),
                        MessageConfUtils.discordOfflineEmbed,
                        MessageConfUtils.discordOffline.replace("%player%", PlayerUtils.getOffOnReg(Objects.requireNonNull(PlayerUtils.getStat(player)))),
                        ConfigUtils.textChannelBLeaves));
                break;
            case "staff":
                if (player.hasPermission("streamline.staff")) {
                    MessagingUtils.sendDiscordEBMessage(new DiscordMessage(plugin.getProxy().getConsole(),
                            MessageConfUtils.discordOfflineEmbed,
                            MessageConfUtils.discordOffline.replace("%player%", PlayerUtils.getOffOnReg(Objects.requireNonNull(PlayerUtils.getStat(player)))),
                            ConfigUtils.textChannelBLeaves));
                }
                break;
            case "no":
            default:
                break;
        }
    }
}
