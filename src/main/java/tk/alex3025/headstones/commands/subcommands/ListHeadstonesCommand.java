package tk.alex3025.headstones.commands.subcommands;

import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import tk.alex3025.headstones.Headstones;
import tk.alex3025.headstones.utils.Message;

import java.util.UUID;

public class ListHeadstonesCommand extends SubcommandBase {

    public ListHeadstonesCommand() {
        super("list", "headstones.list", true);
    }

    @Override
    public boolean onCommand(CommandSender sender, String[] args) {
        Player player = (Player) sender;
        UUID playerUUID = player.getUniqueId();

        ConfigurationSection headstones = Headstones.getInstance().getDatabase().getConfigurationSection("headstones");
        boolean found = false;

        if (headstones != null) {
            for (String key : headstones.getKeys(false)) {
                ConfigurationSection hs = headstones.getConfigurationSection(key);
                if (hs != null) {
                    String ownerString = hs.getString("owner");
                    if (ownerString != null && ownerString.equals(playerUUID.toString())) {
                        if (!found) {
                             Message.sendMessage(sender, "&aYour Headstones:");
                             found = true;
                        }
                        String worldName = hs.getString("world");
                        int x = hs.getInt("x");
                        int y = hs.getInt("y");
                        int z = hs.getInt("z");

                        Message.sendMessage(sender, String.format("&7- &b%s&7: X: &f%d&7, Y: &f%d&7, Z: &f%d",
                                worldName, x, y, z));
                    }
                }
            }
        }

        if (!found) {
            Message.sendMessage(sender, "&cYou don't have any headstones.");
        }

        return true;
    }
}
