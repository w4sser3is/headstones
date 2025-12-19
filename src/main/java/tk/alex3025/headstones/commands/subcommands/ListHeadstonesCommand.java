package tk.alex3025.headstones.commands.subcommands;

import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import tk.alex3025.headstones.utils.HeadstoneUtils;
import tk.alex3025.headstones.utils.Message;

import java.util.List;

public class ListHeadstonesCommand extends SubcommandBase {

    public ListHeadstonesCommand() {
        super("list", "headstones.list", true);
    }

    @Override
    public boolean onCommand(CommandSender sender, String[] args) {
        Player player = (Player) sender;
        List<ConfigurationSection> playerHeadstones = HeadstoneUtils.getPlayerHeadstones(player);

        if (!playerHeadstones.isEmpty()) {
            Message.sendMessage(sender, "&aYour Headstones:");
            int index = 1;
            for (ConfigurationSection hs : playerHeadstones) {
                String worldName = hs.getString("world");
                int x = hs.getInt("x");
                int y = hs.getInt("y");
                int z = hs.getInt("z");

                Message.sendMessage(sender, String.format("&7%d. &b%s&7: X: &f%d&7, Y: &f%d&7, Z: &f%d",
                        index, worldName, x, y, z));
                index++;
            }
        } else {
            Message.sendMessage(sender, "&cYou don't have any headstones.");
        }

        return true;
    }
}
