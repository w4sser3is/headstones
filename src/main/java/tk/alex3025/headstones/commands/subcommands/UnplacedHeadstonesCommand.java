package tk.alex3025.headstones.commands.subcommands;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import tk.alex3025.headstones.Headstones;
import tk.alex3025.headstones.utils.ExperienceManager;
import tk.alex3025.headstones.utils.InventorySerializer;
import tk.alex3025.headstones.utils.Message;

import java.io.IOException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;

public class UnplacedHeadstonesCommand extends SubcommandBase {

    public UnplacedHeadstonesCommand() {
        super("unplaced", "headstones.unplaced", false);
    }

    @Override
    public boolean onCommand(CommandSender sender, String[] args) {
        if (args.length == 0) {
            new Message(sender).translation("usage-unplaced").send();
            return true;
        }

        String action = args[0].toLowerCase();

        if (action.equals("list")) {
            return handleList(sender);
        } else if (action.equals("restore")) {
            if (args.length < 2) {
                new Message(sender).translation("usage-unplaced-restore").send();
                return true;
            }
            return handleRestore(sender, args[1]);
        } else {
            new Message(sender).translation("usage-unplaced").send();
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args) {
        List<String> matches = new ArrayList<>();
        if (args.length == 1) {
            if ("list".startsWith(args[0].toLowerCase())) matches.add("list");
            if ("restore".startsWith(args[0].toLowerCase())) matches.add("restore");
        } else if (args.length == 2 && args[0].equalsIgnoreCase("restore")) {
            ConfigurationSection unplaced = Headstones.getInstance().getDatabase().getConfigurationSection("unplaced_headstones");
            if (unplaced != null) {
                for (String key : unplaced.getKeys(false)) {
                    if (key.toLowerCase().startsWith(args[1].toLowerCase())) {
                        matches.add(key);
                    }
                }
            }
        }
        return matches;
    }

    private boolean handleList(CommandSender sender) {
        ConfigurationSection unplaced = Headstones.getInstance().getDatabase().getConfigurationSection("unplaced_headstones");

        if (unplaced == null || unplaced.getKeys(false).isEmpty()) {
            new Message(sender).translation("no-unplaced-headstones").send();
            return true;
        }

        sender.sendMessage(Message.getTranslation("prefix") + " " + Message.getTranslation("unplaced-list-header"));
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneId.systemDefault());

        for (String id : unplaced.getKeys(false)) {
            ConfigurationSection hs = unplaced.getConfigurationSection(id);
            if (hs == null) continue;

            String ownerId = hs.getString("owner");
            long timestamp = hs.getLong("timestamp", 0);

            String ownerName = "Unknown";
            if (ownerId != null) {
                try {
                    OfflinePlayer owner = Bukkit.getOfflinePlayer(UUID.fromString(ownerId));
                    if (owner.getName() != null) ownerName = owner.getName();
                } catch (IllegalArgumentException ignored) {}
            }

            String date = formatter.format(Instant.ofEpochMilli(timestamp));

            String messageStr = Message.getTranslation("unplaced-list-item")
                .replace("%id%", id)
                .replace("%player%", ownerName)
                .replace("%date%", date);

            Message.sendMessage(sender, messageStr);
        }

        return true;
    }

    private boolean handleRestore(CommandSender sender, String id) {
        ConfigurationSection unplaced = Headstones.getInstance().getDatabase().getConfigurationSection("unplaced_headstones");

        if (unplaced == null || !unplaced.contains(id)) {
            new Message(sender).translation("unplaced-not-found").send();
            return true;
        }

        ConfigurationSection hs = unplaced.getConfigurationSection(id);
        if (hs == null) {
            new Message(sender).translation("unplaced-not-found").send();
            return true;
        }

        String ownerIdStr = hs.getString("owner");
        if (ownerIdStr == null) {
            new Message(sender).translation("unplaced-invalid-data").send();
            return true;
        }

        UUID ownerId;
        try {
            ownerId = UUID.fromString(ownerIdStr);
        } catch (IllegalArgumentException e) {
            new Message(sender).translation("unplaced-invalid-data").send();
            return true;
        }

        Player player = Bukkit.getPlayer(ownerId);
        if (player == null || !player.isOnline()) {
            new Message(sender).translation("player-not-online").send();
            return true;
        }

        // Restore experience
        int experience = hs.getInt("experience", 0);
        if (experience > 0) {
            int currentExp = ExperienceManager.getExperience(player);
            ExperienceManager.setExperience(player, currentExp + experience);
        }

        // Restore inventory
        String inventoryData = hs.getString("inventory");
        boolean droppedItems = false;
        if (inventoryData != null && !inventoryData.isEmpty()) {
            try {
                ItemStack[] items = InventorySerializer.deserialize(inventoryData);
                for (ItemStack item : items) {
                    if (item != null) {
                        HashMap<Integer, ItemStack> remaining = player.getInventory().addItem(item);
                        if (!remaining.isEmpty()) {
                            droppedItems = true;
                            for (ItemStack drop : remaining.values()) {
                                player.getWorld().dropItem(player.getLocation(), drop);
                            }
                        }
                    }
                }
            } catch (IOException e) {
                Headstones.getInstance().getLogger().log(Level.SEVERE, "Failed to deserialize unplaced headstone inventory", e);
                new Message(sender).translation("unplaced-restore-error").send();
                return true;
            }
        }

        // Cleanup
        unplaced.set(id, null);
        Headstones.getInstance().getDatabase().save();

        if (droppedItems) {
            new Message(player).translation("some-items-dropped").send();
        }

        String restoredToAdminMsg = Message.getTranslation("unplaced-restored-admin")
            .replace("%player%", player.getName());
        Message.sendMessage(sender, restoredToAdminMsg);

        new Message(player).translation("unplaced-restored-player").send();

        return true;
    }
}
