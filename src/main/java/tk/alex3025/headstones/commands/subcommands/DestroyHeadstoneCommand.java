package tk.alex3025.headstones.commands.subcommands;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import tk.alex3025.headstones.utils.ExperienceManager;
import tk.alex3025.headstones.utils.Headstone;
import tk.alex3025.headstones.utils.HeadstoneUtils;
import tk.alex3025.headstones.utils.Message;

import java.util.ArrayList;
import java.util.List;

public class DestroyHeadstoneCommand extends SubcommandBase {

    public DestroyHeadstoneCommand() {
        super("destroy", "headstones.destroy", false);
    }

    @Override
    public boolean onCommand(CommandSender sender, String[] args) {
        if (args.length < 1) {
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

        // do not destroy headstones of offline players, otherwise their items and experience would be lost
        if (!targetPlayer.isOnline() || targetPlayer.getPlayer() == null) {
            new Message(sender).translation("player-not-online").send();
            return true;
        }

        List<ConfigurationSection> playerHeadstones = HeadstoneUtils.getPlayerHeadstones(targetPlayer.getUniqueId());

        if (playerHeadstones.isEmpty()) {
            new Message(sender).translation("headstone-not-found").send();
            return true;
        }

        int totalExperience = 0;
        List<ItemStack> allItems = new ArrayList<>();
        int destroyedCount = 0;

        // Process all headstones
        for (ConfigurationSection hsSection : playerHeadstones) {
            String uuid = hsSection.getName();
            Headstone headstone = Headstone.fromUUID(uuid);

            if (headstone == null) {
                continue;
            }

            // Collect experience
            totalExperience += headstone.getExperience();

            // Collect items
            if (headstone.getInventory() != null) {
                for (ItemStack item : headstone.getInventory()) {
                    if (item != null && item.getType() != Material.AIR) {
                        allItems.add(item);
                    }
                }
            }

            // Remove the headstone block
            if (headstone.getLocation().getBlock().getType() == Material.PLAYER_HEAD) {
                headstone.getLocation().getBlock().setType(Material.AIR);
            }

            // Delete from database
            headstone.deletePlayerData();
            destroyedCount++;
        }

        // Restore items and experience to player if online
        if (targetPlayer.isOnline() && targetPlayer.getPlayer() != null) {
            Player onlinePlayer = targetPlayer.getPlayer();

            // Restore experience
            if (totalExperience > 0) {
                int currentExp = ExperienceManager.getExperience(onlinePlayer);
                ExperienceManager.setExperience(onlinePlayer, currentExp + totalExperience);
            }

            // Restore items - try to add to inventory, drop excess at player's location
            for (ItemStack item : allItems) {
                var remaining = onlinePlayer.getInventory().addItem(item);
                for (ItemStack droppedItem : remaining.values()) {
                    // Drop items in front of the player
                    onlinePlayer.getWorld().dropItemNaturally(onlinePlayer.getLocation().add(0, 0, 1), droppedItem);
                }
            }

            // Notify the player
            new Message(onlinePlayer).translation("headstones-restored")
                    .replace("count", destroyedCount)
                    .replace("experience", totalExperience)
                    .send();
        }

        new Message(sender)
                .translation("all-headstones-destroyed")
                .replace("username", targetPlayer.getName())
                .replace("count", destroyedCount)
                .send();

        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull String[] args) {
        if (args.length == 1) {
            List<String> completions = new ArrayList<>();
            for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                completions.add(onlinePlayer.getName());
            }
            return completions;
        }
        return List.of();
    }
}
