package net.plasmere.streamline.commands.staff.spy;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.plasmere.streamline.config.MessageConfUtils;
import net.plasmere.streamline.objects.Player;
import net.plasmere.streamline.utils.MessagingUtils;
import net.plasmere.streamline.utils.PlayerUtils;

public class PSPYCommand extends Command {
    public PSPYCommand(String perm, String[] aliases){
        super("pspy", perm, aliases);
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (sender instanceof ProxiedPlayer) {
            Player player = PlayerUtils.getStat(sender);

            if (player == null) return;

            player.togglePSPY();

            MessagingUtils.sendBUserMessage(sender, MessageConfUtils.pspyToggle
                    .replace("%toggle%", player.pspy ? MessageConfUtils.pspyOn : MessageConfUtils.pspyOff)
            );
        } else {
            MessagingUtils.sendBUserMessage(sender, MessageConfUtils.onlyPlayers);
        }
    }
}
