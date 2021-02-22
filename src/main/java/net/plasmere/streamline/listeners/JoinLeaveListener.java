package net.plasmere.streamline.listeners;

import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.group.Group;
import net.luckperms.api.node.Node;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.event.ServerConnectEvent;
import net.md_5.bungee.api.event.ServerDisconnectEvent;
import net.plasmere.streamline.StreamLine;
import net.plasmere.streamline.config.Config;
import net.plasmere.streamline.config.ConfigUtils;
import net.plasmere.streamline.config.MessageConfUtils;
import net.plasmere.streamline.events.Event;
import net.plasmere.streamline.events.EventsHandler;
import net.plasmere.streamline.objects.messaging.BungeeMassMessage;
import net.plasmere.streamline.objects.messaging.DiscordMessage;
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
import net.plasmere.streamline.utils.UUIDFetcher;
import us.myles.ViaVersion.api.Via;
import us.myles.ViaVersion.api.ViaAPI;

import java.util.*;

public class JoinLeaveListener implements Listener {
    private final Configuration config = Config.getConf();
    private final StreamLine plugin;
    private final LuckPerms api = LuckPermsProvider.get();
    private final ViaAPI viaAPI = Via.getAPI();

    public JoinLeaveListener(StreamLine streamLine){
        this.plugin = streamLine;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onJoin(PostLoginEvent ev) {
        ProxiedPlayer player = ev.getPlayer();

        Player stat = PlayerUtils.getStat(player);

        if (stat == null) {
            if (PlayerUtils.exists(player.getName())) {
                PlayerUtils.addStat(new Player(player, false));
            } else {
                PlayerUtils.createStat(player);
            }
            stat = PlayerUtils.getStat(player);
            if (stat == null) {
                StreamLine.getInstance().getLogger().severe("CANNOT INSTANTIATE THE PLAYER: " + player.getName());
                return;
            }
        }

        try {
            for (ProxiedPlayer pl : StreamLine.getInstance().getProxy().getPlayers()){
                Player p = PlayerUtils.getStat(pl);

                if (p == null) {
                    if (PlayerUtils.exists(pl.getName())) {
                        PlayerUtils.addStat(new Player(pl, false));
                    } else {
                        PlayerUtils.createStat(pl);
                    }
                    p = PlayerUtils.getStat(pl);
                    if (p == null) {
                        StreamLine.getInstance().getLogger().severe("CANNOT INSTANTIATE THE PLAYER: " + pl.getName());
                        continue;
                    }
                }

                if (stat.guild == null) continue;
                if (stat.guild.equals("")) continue;
                if (p.guild.equals(stat.guild) && ! p.equals(stat)) continue;

                try {
                    GuildUtils.addGuild(new Guild(UUID.fromString(stat.guild), false));
                } catch (Exception e) {
                    // Do nothing.
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        switch (ConfigUtils.moduleBPlayerJoins) {
            case "yes":
                MessagingUtils.sendBungeeMessage(new BungeeMassMessage(plugin.getProxy().getConsole(),
                        MessageConfUtils.bungeeOnline.replace("%player%", PlayerUtils.getOffOnRegBungee(Objects.requireNonNull(UUIDFetcher.getPlayer(player)))),
                        "streamline.staff"));
                break;
            case "staff":
                if (player.hasPermission("streamline.staff")) {
                    MessagingUtils.sendBungeeMessage(new BungeeMassMessage(plugin.getProxy().getConsole(),
                            MessageConfUtils.bungeeOnline.replace("%player%", PlayerUtils.getOffOnRegBungee(Objects.requireNonNull(UUIDFetcher.getPlayer(player)))),
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
                        MessageConfUtils.discordOnline.replace("%player%", PlayerUtils.getOffOnRegDiscord(Objects.requireNonNull(UUIDFetcher.getPlayer(player)))),
                        ConfigUtils.textChannelBJoins));
                break;
            case "staff":
                if (player.hasPermission("streamline.staff")) {
                    MessagingUtils.sendDiscordEBMessage(new DiscordMessage(plugin.getProxy().getConsole(),
                            MessageConfUtils.discordOnlineEmbed,
                            MessageConfUtils.discordOnline.replace("%player%", PlayerUtils.getOffOnRegDiscord(Objects.requireNonNull(UUIDFetcher.getPlayer(player)))),
                            ConfigUtils.textChannelBJoins));
                }
                break;
            case "no":
            default:
                break;
        }

        for (Event event : EventsHandler.getEvents()) {
            if (! EventsHandler.checkTags(event, stat)) continue;

            if (! (event.condition.equals(Event.Condition.JOIN) && event.conVal.toLowerCase(Locale.ROOT).equals("network"))) continue;

            EventsHandler.runEvent(event, stat);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onServer(ServerConnectEvent ev){
        ProxiedPlayer player = ev.getPlayer();

        boolean hasServer = false;
        ServerInfo server = ev.getTarget();

        Player stat = PlayerUtils.getStat(player);

        if (stat == null) {
            if (PlayerUtils.exists(player.getName())) {
                PlayerUtils.addStat(new Player(player, false));
            } else {
                PlayerUtils.createStat(player);
            }
            stat = PlayerUtils.getStat(player);
            if (stat == null) {
                StreamLine.getInstance().getLogger().severe("CANNOT INSTANTIATE THE PLAYER: " + player.getName());
                return;
            }
        }

        if (ev.getReason().equals(ServerConnectEvent.Reason.JOIN_PROXY)) {
            for (ServerInfo s : StreamLine.getInstance().getProxy().getServers().values()) {
                String sv = s.getName();
                if (player.hasPermission(ConfigUtils.redirectPre + sv)) {
                    Group group = api.getGroupManager().getGroup(Objects.requireNonNull(api.getUserManager().getUser(player.getName())).getPrimaryGroup());

                    if (group == null) {
                        hasServer = true;
                        server = s;
                        break;
                    }

                    Collection<Node> nodes = group.getNodes();

                    for (Node node : nodes) {
                        if (node.getKey().equals(ConfigUtils.redirectPre + sv)) {
                            hasServer = true;
                            server = s;
                            break;
                        }
                    }

                    Collection<Node> nods = Objects.requireNonNull(api.getUserManager().getUser(player.getName())).getNodes();

                    for (Node node : nods) {
                        if (node.getKey().equals(ConfigUtils.redirectPre + sv)) {
                            hasServer = true;
                            server = s;
                            break;
                        }
                    }

                    hasServer = false;
                    break;
                }
            }

            if (!hasServer) {
                server = StreamLine.getInstance().getProxy().getServerInfo(ConfigUtils.redirectMain);
            }
        }

        if (! ev.getReason().equals(ServerConnectEvent.Reason.JOIN_PROXY)){
            if (! player.hasPermission(ConfigUtils.vbOverridePerm)) {
                int version = viaAPI.getPlayerVersion(player.getUniqueId());

                boolean bool = StreamLine.serverPermissions.isAllowed(version, server.getName());

                //StreamLine.getInstance().getLogger().info("Joining " + server.getName() + " tested as: " + bool);

                if (! StreamLine.serverPermissions.isAllowed(version, server.getName())) {
                    MessagingUtils.sendBUserMessage(ev.getPlayer(), MessageConfUtils.vbBlocked);
                    ev.setCancelled(true);
                    return;
                }
            }
        }

        ev.setTarget(server);

        for (Event event : EventsHandler.getEvents()) {
            if (! EventsHandler.checkTags(event, stat)) continue;

            if (! (event.condition.equals(Event.Condition.JOIN) && event.conVal.toLowerCase(Locale.ROOT).equals(server.getName()))) continue;

            EventsHandler.runEvent(event, stat);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onServerDiscon(ServerDisconnectEvent ev) {
        ProxiedPlayer player = ev.getPlayer();

        ServerInfo server = ev.getTarget();

        Player stat = PlayerUtils.getStat(player);

        if (stat == null) {
            if (PlayerUtils.exists(player.getName())) {
                PlayerUtils.addStat(new Player(player, false));
            } else {
                PlayerUtils.createStat(player);
            }
            stat = PlayerUtils.getStat(player);
            if (stat == null) {
                StreamLine.getInstance().getLogger().severe("CANNOT INSTANTIATE THE PLAYER: " + player.getName());
                return;
            }
        }

        for (Event event : EventsHandler.getEvents()) {
            if (! EventsHandler.checkTags(event, stat)) continue;

            if (! event.condition.equals(Event.Condition.LEAVE) && ! event.conVal.toLowerCase(Locale.ROOT).equals(server.getName())) continue;

            EventsHandler.runEvent(event, stat);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onLeave(PlayerDisconnectEvent ev) {
        ProxiedPlayer player = ev.getPlayer();

        Player stat = PlayerUtils.getStat(player);

        if (stat == null) {
            if (PlayerUtils.exists(player.getName())) {
                PlayerUtils.addStat(new Player(player, false));
            } else {
                PlayerUtils.createStat(player);
            }
            stat = PlayerUtils.getStat(player);
            if (stat == null) {
                StreamLine.getInstance().getLogger().severe("CANNOT INSTANTIATE THE PLAYER: " + player.getName());
                return;
            }
        }

        switch (ConfigUtils.moduleBPlayerLeaves) {
            case "yes":
                MessagingUtils.sendBungeeMessage(new BungeeMassMessage(plugin.getProxy().getConsole(),
                        MessageConfUtils.bungeeOffline.replace("%player%", PlayerUtils.getOffOnRegBungee(Objects.requireNonNull(UUIDFetcher.getPlayer(player)))),
                        "streamline.staff"));
                break;
            case "staff":
                if (player.hasPermission("streamline.staff")) {
                    MessagingUtils.sendBungeeMessage(new BungeeMassMessage(plugin.getProxy().getConsole(),
                            MessageConfUtils.bungeeOffline.replace("%player%", PlayerUtils.getOffOnRegBungee(Objects.requireNonNull(UUIDFetcher.getPlayer(player)))),
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
                        MessageConfUtils.discordOffline.replace("%player%", PlayerUtils.getOffOnRegDiscord(Objects.requireNonNull(UUIDFetcher.getPlayer(player)))),
                        ConfigUtils.textChannelBLeaves));
                break;
            case "staff":
                if (player.hasPermission("streamline.staff")) {
                    MessagingUtils.sendDiscordEBMessage(new DiscordMessage(plugin.getProxy().getConsole(),
                            MessageConfUtils.discordOfflineEmbed,
                            MessageConfUtils.discordOffline.replace("%player%", PlayerUtils.getOffOnRegDiscord(Objects.requireNonNull(UUIDFetcher.getPlayer(player)))),
                            ConfigUtils.textChannelBLeaves));
                }
                break;
            case "no":
            default:
                break;
        }

        try {
            for (ProxiedPlayer pl : StreamLine.getInstance().getProxy().getPlayers()){
                Player p = PlayerUtils.getStat(pl);

                if (p == null) {
                    if (PlayerUtils.exists(pl.getName())) {
                        PlayerUtils.addStat(new Player(pl, false));
                    } else {
                        PlayerUtils.createStat(pl);
                    }
                    p = PlayerUtils.getStat(pl);
                    if (p == null) {
                        StreamLine.getInstance().getLogger().severe("CANNOT INSTANTIATE THE PLAYER: " + pl.getName());
                        continue;
                    }
                }

                if (GuildUtils.pHasGuild(stat)) {
                    Guild guild = GuildUtils.getGuild(stat);

                    if (guild == null || p.equals(stat)) continue;
                    if (guild.hasMember(p)) break;
                    if (stat.guild.equals(p.guild)) break;

                    GuildUtils.removeGuild(guild);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        for (Event event : EventsHandler.getEvents()) {
            if (! EventsHandler.checkTags(event, stat)) continue;

            if (! (event.condition.equals(Event.Condition.LEAVE) && event.conVal.toLowerCase(Locale.ROOT).equals("network"))) continue;

            EventsHandler.runEvent(event, stat);
        }

        try {
            PlayerUtils.removeStat(stat);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
