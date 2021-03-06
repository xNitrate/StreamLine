package net.plasmere.streamline.discordbot;

import net.plasmere.streamline.StreamLine;
import net.plasmere.streamline.config.Config;
import net.plasmere.streamline.config.ConfigUtils;
import net.plasmere.streamline.config.MessageConfUtils;
import net.plasmere.streamline.discordbot.commands.*;
import net.plasmere.streamline.utils.*;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.md_5.bungee.config.Configuration;

import javax.annotation.Nonnull;
import java.util.Objects;

public class MessageListener extends ListenerAdapter {
    private final StreamLine plugin;

    public MessageListener(StreamLine streamLine){
        this.plugin = streamLine;
    }

    private final Configuration config = Config.getConf();

    private static final EmbedBuilder eb = new EmbedBuilder();

    @Override
    public void onMessageReceived(@Nonnull MessageReceivedEvent event) {
        String em = event.getMessage().getContentRaw();
        String prefix = ConfigUtils.botPrefix;

        if (event.getAuthor().isBot()) return;

        if (ConfigUtils.moduleStaffChatToMinecraft && event.getChannel().equals(event.getJDA().getTextChannelById(ConfigUtils.textChannelStaffChat))) {
            if (ConfigUtils.moduleSCOnlyStaffRole){
                try {
                    if (Objects.requireNonNull(event.getMessage().getMember()).getRoles().contains(event.getJDA().getRoleById(ConfigUtils.roleStaff))) {
                        MessagingUtils.sendStaffMessageSC(event.getAuthor().getName(), MessageConfUtils.discordStaffChatFrom, em, plugin);
                    } else
                        return;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                MessagingUtils.sendStaffMessageSC(event.getAuthor().getName(), MessageConfUtils.discordStaffChatFrom, em, plugin);
            }

            plugin.getLogger().info("Someone talked in staffchat (discord)... sending to bungee...");
        }

        if (! event.getMessage().getContentRaw().toLowerCase().startsWith(prefix)) return;

        String[] args = event.getMessage().getContentRaw().toLowerCase().substring(ConfigUtils.botPrefix.length()).split(" ");

        plugin.getLogger().info("[ " + event.getAuthor().getName() + " ] " + event.getMessage().getContentRaw());

        // Commands.
        if (MessagingUtils.compareWithList(args[0], ConfigUtils.comDCommandsAliases)) {
            if (ConfigUtils.comDCommands && PermissionHelper.checkRoleIDPerms(event, ConfigUtils.comDCommandsPerm)) {
                plugin.getLogger().info("So... Switching on case \"commands\"...");
                try {
                    CommandsCommand.sendMessage(args[0], event, plugin);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else if (ConfigUtils.moduleSayCommandDisabled.equals("yes")) {
                event.getChannel().sendMessage(eb.setTitle("Error!").setDescription(MessageConfUtils.discordCommandDisabled.replace("%newline%", "\n")).build()).queue();
            } else if (ConfigUtils.moduleSayCommandDisabled.equals("staff")) {
                if (Objects.requireNonNull(event.getMessage().getMember()).getRoles().contains(event.getJDA().getRoleById(ConfigUtils.roleStaff)))
                    event.getChannel().sendMessage(eb.setTitle("Error!").setDescription(MessageConfUtils.discordCommandDisabled.replace("%newline%", "\n")).build()).queue();
            } else if (ConfigUtils.moduleSayCommandDisabled.equals("owner")) {
                if (Objects.requireNonNull(event.getMessage().getMember()).isOwner())
                    event.getChannel().sendMessage(eb.setTitle("Error!").setDescription(MessageConfUtils.discordCommandDisabled.replace("%newline%", "\n")).build()).queue();
            }
        // Online.
        } else if (MessagingUtils.compareWithList(args[0], ConfigUtils.comDOnlineAliases)) {
            if (ConfigUtils.comDOnline && PermissionHelper.checkRoleIDPerms(event, ConfigUtils.comDOnlinePerm)) {
                plugin.getLogger().info("So... Switching on case \"online\"...");
                try {
                    OnlineCommand.sendMessage(args[0], event, plugin);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else if (ConfigUtils.moduleSayCommandDisabled.equals("yes")) {
                event.getChannel().sendMessage(eb.setTitle("Error!").setDescription(MessageConfUtils.discordCommandDisabled.replace("%newline%", "\n")).build()).queue();
            } else if (ConfigUtils.moduleSayCommandDisabled.equals("staff")) {
                if (Objects.requireNonNull(event.getMessage().getMember()).getRoles().contains(event.getJDA().getRoleById(ConfigUtils.roleStaff)))
                    event.getChannel().sendMessage(eb.setTitle("Error!").setDescription(MessageConfUtils.discordCommandDisabled.replace("%newline%", "\n")).build()).queue();
            } else if (ConfigUtils.moduleSayCommandDisabled.equals("owner")) {
                if (Objects.requireNonNull(event.getMessage().getMember()).isOwner())
                    event.getChannel().sendMessage(eb.setTitle("Error!").setDescription(MessageConfUtils.discordCommandDisabled.replace("%newline%", "\n")).build()).queue();
            }
        // Report.
        } else if (MessagingUtils.compareWithList(args[0], ConfigUtils.comDReportAliases)) {
            if (ConfigUtils.comDReport && PermissionHelper.checkRoleIDPerms(event, ConfigUtils.comDReportPerm)) {
                plugin.getLogger().info("So... Switching on case \"report\"...");
                try {
                    ReportCommand.sendMessage(args[0], event, plugin);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else if (ConfigUtils.moduleSayCommandDisabled.equals("yes")) {
                event.getChannel().sendMessage(eb.setTitle("Error!").setDescription(MessageConfUtils.discordCommandDisabled.replace("%newline%", "\n")).build()).queue();
            } else if (ConfigUtils.moduleSayCommandDisabled.equals("staff")) {
                if (Objects.requireNonNull(event.getMessage().getMember()).getRoles().contains(event.getJDA().getRoleById(ConfigUtils.roleStaff)))
                    event.getChannel().sendMessage(eb.setTitle("Error!").setDescription(MessageConfUtils.discordCommandDisabled.replace("%newline%", "\n")).build()).queue();
            } else if (ConfigUtils.moduleSayCommandDisabled.equals("owner")) {
                if (Objects.requireNonNull(event.getMessage().getMember()).isOwner())
                    event.getChannel().sendMessage(eb.setTitle("Error!").setDescription(MessageConfUtils.discordCommandDisabled.replace("%newline%", "\n")).build()).queue();
            }
        // StaffChat.
        } else if (MessagingUtils.compareWithList(args[0], ConfigUtils.comDStaffChatAliases)) {
            if (ConfigUtils.comDStaffChat && PermissionHelper.checkRoleIDPerms(event, ConfigUtils.comDStaffChatPerm)) {
                plugin.getLogger().info("So... Switching on case \"staffchat\"...");
                try {
                    StaffChatCommand.sendMessage(args[0], event, plugin);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else if (ConfigUtils.moduleSayCommandDisabled.equals("yes")) {
                event.getChannel().sendMessage(eb.setTitle("Error!").setDescription(MessageConfUtils.discordCommandDisabled.replace("%newline%", "\n")).build()).queue();
            } else if (ConfigUtils.moduleSayCommandDisabled.equals("staff")) {
                if (Objects.requireNonNull(event.getMessage().getMember()).getRoles().contains(event.getJDA().getRoleById(ConfigUtils.roleStaff)))
                    event.getChannel().sendMessage(eb.setTitle("Error!").setDescription(MessageConfUtils.discordCommandDisabled.replace("%newline%", "\n")).build()).queue();
            } else if (ConfigUtils.moduleSayCommandDisabled.equals("owner")) {
                if (Objects.requireNonNull(event.getMessage().getMember()).isOwner())
                    event.getChannel().sendMessage(eb.setTitle("Error!").setDescription(MessageConfUtils.discordCommandDisabled.replace("%newline%", "\n")).build()).queue();
            }
        // StaffOnline.
        } else if (MessagingUtils.compareWithList(args[0], ConfigUtils.comDStaffOnlineAliases)) {
            if (ConfigUtils.comDStaffOnline && PermissionHelper.checkRoleIDPerms(event, ConfigUtils.comDStaffOnlinePerm)) {
                plugin.getLogger().info("So... Switching on case \"staffonline\"...");
                try {
                    StaffOnlineCommand.sendMessage(args[0], event, plugin);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else if (ConfigUtils.moduleSayCommandDisabled.equals("yes")) {
                event.getChannel().sendMessage(eb.setTitle("Error!").setDescription(MessageConfUtils.discordCommandDisabled.replace("%newline%", "\n")).build()).queue();
            } else if (ConfigUtils.moduleSayCommandDisabled.equals("staff")) {
                if (Objects.requireNonNull(event.getMessage().getMember()).getRoles().contains(event.getJDA().getRoleById(ConfigUtils.roleStaff)))
                    event.getChannel().sendMessage(eb.setTitle("Error!").setDescription(MessageConfUtils.discordCommandDisabled.replace("%newline%", "\n")).build()).queue();
            } else if (ConfigUtils.moduleSayCommandDisabled.equals("owner")) {
                if (Objects.requireNonNull(event.getMessage().getMember()).isOwner())
                    event.getChannel().sendMessage(eb.setTitle("Error!").setDescription(MessageConfUtils.discordCommandDisabled.replace("%newline%", "\n")).build()).queue();
            }
        // IF NOT.
        } else {
            plugin.getLogger().info("So... Switching on case default...");
            if (ConfigUtils.moduleSayNotACommand.equals("yes")) {
                event.getChannel().sendMessage(eb.setDescription(MessageConfUtils.discordNotACommand.replace("%newline%","\n")).build()).queue();
            } else if (ConfigUtils.moduleSayNotACommand.equals("staff")) {
                if (Objects.requireNonNull(event.getMessage().getMember()).getRoles().contains(event.getJDA().getRoleById(ConfigUtils.roleStaff)))
                    event.getChannel().sendMessage(eb.setDescription(MessageConfUtils.discordNotACommand.replace("%newline%","\n")).build()).queue();
            } else if (ConfigUtils.moduleSayNotACommand.equals("owner")) {
                if (Objects.requireNonNull(event.getMessage().getMember()).isOwner())
                    event.getChannel().sendMessage(eb.setDescription(MessageConfUtils.discordNotACommand.replace("%newline%","\n")).build()).queue();
            }
        }
    }
}
