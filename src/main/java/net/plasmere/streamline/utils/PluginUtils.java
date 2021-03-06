package net.plasmere.streamline.utils;

import net.plasmere.streamline.StreamLine;
import net.plasmere.streamline.commands.*;
import net.plasmere.streamline.commands.servers.GoToServerLobbyCommand;
import net.plasmere.streamline.commands.servers.GoToServerVanillaCommand;
import net.plasmere.streamline.commands.staff.*;
import net.plasmere.streamline.commands.staff.events.BTagCommand;
import net.plasmere.streamline.commands.staff.events.EventReloadCommand;
import net.plasmere.streamline.commands.staff.spy.GSPYCommand;
import net.plasmere.streamline.commands.staff.spy.PSPYCommand;
import net.plasmere.streamline.commands.staff.spy.SSPYCommand;
import net.plasmere.streamline.config.ConfigUtils;
import net.plasmere.streamline.listeners.JoinLeaveListener;
import net.plasmere.streamline.listeners.ChatListener;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.Listener;

import java.util.ArrayList;
import java.util.List;

public class PluginUtils {
    public static int commandsAmount = 0;

    public static void unregisterCommand(StreamLine plugin, Command command){
        plugin.getProxy().getPluginManager().unregisterCommand(command);
    }

    public static void unregisterListener(StreamLine plugin, Listener listener){
        plugin.getProxy().getPluginManager().unregisterListener(listener);
    }

    public static void registerCommand(StreamLine plugin, Command command){
        commandsAmount++;
        plugin.getProxy().getPluginManager().registerCommand(plugin, command);
    }

    public static void registerListener(StreamLine plugin, Listener listener){
        plugin.getProxy().getPluginManager().registerListener(plugin, listener);
    }

    public static void loadCommands(StreamLine plugin){
        commandsAmount = 0;

        // Setup...
        List<Command> commands = new ArrayList<>();

        Command stream = new StreamCommand(plugin, ConfigUtils.comBStreamPerm, getAliases(ConfigUtils.comBStreamAliases));
        Command staffChat = new StaffChatCommand(plugin, ConfigUtils.comBStaffChatPerm, getAliases(ConfigUtils.comBStaffChatAliases));
        Command ping = new PingCommand(plugin, ConfigUtils.comBPingPerm, getAliases(ConfigUtils.comBPingAliases));
        Command plugins = new PluginsCommand(plugin, ConfigUtils.comBPluginsPerm, getAliases(ConfigUtils.comBPluginsAliases));
        Command staffOnline = new StaffOnlineCommand(plugin, ConfigUtils.comBStaffOnlinePerm, getAliases(ConfigUtils.comBStaffOnlineAliases));
        Command globalOnline = new GlobalOnlineCommand(plugin, ConfigUtils.comBGlobalOnlinePerm, getAliases(ConfigUtils.comBGlobalOnlineAliases));
        Command reload = new ReloadCommand(plugin, ConfigUtils.comBReloadPerm, getAliases(ConfigUtils.comBReloadAliases));

        Command lobby = new GoToServerLobbyCommand(plugin, ConfigUtils.comBLobbyPerm, getAliases(ConfigUtils.comBLobbyAliases));
        Command fabric = new GoToServerVanillaCommand(plugin, ConfigUtils.comBFabricPerm);

        Command report = new ReportCommand(plugin, ConfigUtils.comBReportPerm, getAliases(ConfigUtils.comBReportAliases));
        Command bsudo = new SudoCommand(plugin, ConfigUtils.comBSudoPerm, getAliases(ConfigUtils.comBSudoAliases));


        Command parties = new PartiesCommand(plugin, ConfigUtils.comBPartiesPerm, getAliases(ConfigUtils.comBPartiesAliases));
        Command guilds = new GuildsCommand(plugin, ConfigUtils.comBGuildsPerm, getAliases(ConfigUtils.comBGuildsAliases));
        Command party = new PartyCommand(plugin, ConfigUtils.comBParPerm, getAliases(ConfigUtils.comBParMainAliases));
        Command guild = new GuildCommand(plugin, ConfigUtils.comBGuildPerm, getAliases(ConfigUtils.comBGuildMainAliases));
        List<String> pca = new ArrayList<>();
        pca.add("pch");
        pca.add("pchat");
        List<String> gca = new ArrayList<>();
        gca.add("gch");
        gca.add("gchat");
        Command pc = new PCQuickCommand(plugin, ConfigUtils.comBParPerm, getAliases(pca));
        Command gc = new GCQuickCommand(plugin, ConfigUtils.comBGuildPerm, getAliases(gca));

        Command stats = new StatsCommand(plugin, ConfigUtils.comBStatsPerm, getAliases(ConfigUtils.comBStatsAliases));

        Command sspy = new SSPYCommand(ConfigUtils.comBSSPYPerm, getAliases(ConfigUtils.comBSSPYAliases));
        Command gspy = new GSPYCommand(ConfigUtils.comBGSPYPerm, getAliases(ConfigUtils.comBGSPYAliases));
        Command pspy = new PSPYCommand(ConfigUtils.comBPSPYPerm, getAliases(ConfigUtils.comBPSPYAliases));

        Command btag = new BTagCommand(ConfigUtils.comBBTagPerm, getAliases(ConfigUtils.comBBTagAliases));
        Command reev = new EventReloadCommand(ConfigUtils.comBEReloadPerm, getAliases(ConfigUtils.comBEReloadAliases));

        commands.add(stream);
        commands.add(staffChat);
        commands.add(ping);
        commands.add(plugins);
        commands.add(staffOnline);
        commands.add(globalOnline);
        commands.add(reload);

        commands.add(lobby);
        commands.add(fabric);

        commands.add(report);
        commands.add(bsudo);

        commands.add(parties);
        commands.add(guilds);
        commands.add(party);
        commands.add(guild);
        commands.add(pc);
        commands.add(gc);

        commands.add(stats);

        commands.add(sspy);
        commands.add(gspy);
        commands.add(pspy);

        commands.add(btag);
        commands.add(reev);

        try {
            for (Command command : commands) {
                plugin.getProxy().getPluginManager().unregisterCommand(command);
            }
        } catch (Exception e){
            MessagingUtils.logWarning("Command discriminator broke for the following reason: " + e.getMessage());
        }


        // Commands.
        if (ConfigUtils.comBStream)
            registerCommand(plugin, stream);
        if (ConfigUtils.comBStaffChat)
            registerCommand(plugin, staffChat);
        if (ConfigUtils.comBSudo)
            registerCommand(plugin, bsudo);

        // Utils.
        if (ConfigUtils.comBPing)
            registerCommand(plugin, ping);
        if (ConfigUtils.comBPlugins)
            registerCommand(plugin, plugins);
        if (ConfigUtils.comBStaffOnline)
            registerCommand(plugin, staffOnline);
        if (ConfigUtils.comBGlobalOnline)
            registerCommand(plugin, globalOnline);
        registerCommand(plugin, reload);

        // All players.
        if (ConfigUtils.comBReport)
            registerCommand(plugin, report);

        // JDA
        //registerCommand(plugin, new JDAPingerCommand(plugin));

        // servers
        if (ConfigUtils.comBLobby)
            registerCommand(plugin, lobby);
        if (ConfigUtils.comBFabric)
            registerCommand(plugin, fabric);


        if (ConfigUtils.comBParties)
            registerCommand(plugin, parties);
        if (ConfigUtils.comBGuilds)
            registerCommand(plugin, guilds);
        if (ConfigUtils.comBParty)
            registerCommand(plugin, party);
        if (ConfigUtils.comBGuild)
            registerCommand(plugin, guild);
        if (ConfigUtils.comBParQuick)
            registerCommand(plugin, pc);
        if (ConfigUtils.comBGuildQuick)
            registerCommand(plugin, gc);

        if (ConfigUtils.comBStats)
            registerCommand(plugin, stats);

        if (ConfigUtils.comBSSPY)
            registerCommand(plugin, sspy);
        if (ConfigUtils.comBGSPY)
            registerCommand(plugin, gspy);
        if (ConfigUtils.comBPSPY)
            registerCommand(plugin, pspy);
        if (ConfigUtils.comBBTag)
            registerCommand(plugin, btag);
        if (ConfigUtils.comBEReload)
            registerCommand(plugin, reev);

        plugin.getLogger().info("Loaded " + commands.size() + " commands into memory...!");
    }

    private static String[] getAliases(List<String> aliases){
        String[] a = new String[aliases.size()];

        int i = 0;
        for (String alias : aliases){
            a[i] = alias;
            i++;
        }

        return a;
    }

    public static void loadListeners(StreamLine plugin){
        List<Listener> listeners = new ArrayList<>();
        Listener staffchat = new ChatListener(plugin);
        Listener joinleave = new JoinLeaveListener(plugin);

        listeners.add(staffchat);
        listeners.add(joinleave);

        try {
            for (Listener listener : listeners) {
                plugin.getProxy().getPluginManager().unregisterListener(listener);
            }
        } catch (Exception e){
            MessagingUtils.logWarning("Listener discriminator broke for the following reason: " + e.getMessage());
        }

        registerListener(plugin, staffchat);
        registerListener(plugin, joinleave);

        plugin.getLogger().info("Loaded " + listeners.size() + " listener into memory...!");
    }
}
