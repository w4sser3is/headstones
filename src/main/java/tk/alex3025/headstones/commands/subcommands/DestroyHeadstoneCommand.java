package tk.alex3025.headstones.commands.subcommands;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import tk.alex3025.headstones.utils.Headstone;
import tk.alex3025.headstones.utils.HeadstoneUtils;
import tk.alex3025.headstones.utils.Message;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public class DestroyHeadstoneCommand extends SubcommandBase {

    public DestroyHeadstoneCommand() {
        super("destroy", "headstones.destroy", false);
    }

    @Override
    public boolean onCommand(CommandSender sender, String[] args) {
        if (args.length < 2) {
            new Message(sender).translation("usage-destroy").send();
            return true;
        }

        String targetPlayerName = args[0];
        OfflinePlayer targetPlayer = Bukkit.getOfflinePlayer(targetPlayerName);

        // check if player has played before or is online to avoid accidental wrong names
        if (!targetPlayer.hasPlayedBefore() && !targetPlayer.isOnline()) {
             new Message(sender).translation("player-not-found").send();
             return true;
        }

        int index;
        try {
            index = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            new Message(sender).translation("invalid-number").send();
            return true;
        }

        List<ConfigurationSection> playerHeadstones = HeadstoneUtils.getPlayerHeadstones(targetPlayer.getUniqueId());

        if (index < 1 || index > playerHeadstones.size()) {
            new Message(sender).translation("headstone-not-found").send();
            return true;
        }

        ConfigurationSection hsSection = playerHeadstones.get(index - 1);
        String uuid = hsSection.getName();
        Headstone headstone = Headstone.fromUUID(uuid);

        if (headstone == null) {
            new Message(sender).translation("headstone-not-found").send();
            return true;
        }

        // 1. Restore items to player if online, otherwise drop them at headstone location
        if (targetPlayer.isOnline() && targetPlayer.getPlayer() != null) {
            headstone.restorePlayerInventory(targetPlayer.getPlayer());
        } else {
            // Drop items at the headstone location
            if (headstone.getInventory() != null) {
                for (ItemStack item : headstone.getInventory()) {
                    if (item != null && item.getType() != Material.AIR) {
                        headstone.getLocation().getWorld().dropItemNaturally(headstone.getLocation(), item);
                    }
                }
            }
        }

        // 2. Remove the headstone block
        if (headstone.getLocation().getBlock().getType() == Material.PLAYER_HEAD) {
            headstone.getLocation().getBlock().setType(Material.AIR);
        }

        // 3. Delete from database
        headstone.deletePlayerData();

        new Message(sender, Map.of("username", targetPlayer.getName())).translation("headstone-destroyed").send();

        return true;
    }
}
