package tk.alex3025.headstones.commands.subcommands;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import tk.alex3025.headstones.utils.HeadstoneUtils;
import tk.alex3025.headstones.utils.Message;

import java.util.List;

public class TeleportHeadstoneCommand extends SubcommandBase {

    public TeleportHeadstoneCommand() {
        super("tp", "headstones.tp", true);
    }

    @Override
    public boolean onCommand(CommandSender sender, String[] args) {
        if (args.length < 1) {
            Message.sendMessage(sender, "&cUsage: /hs tp <number>");
            return true;
        }

        int index;
        try {
            index = Integer.parseInt(args[0]);
        } catch (NumberFormatException e) {
            Message.sendMessage(sender, "&cInvalid number.");
            return true;
        }

        Player player = (Player) sender;
        List<ConfigurationSection> playerHeadstones = HeadstoneUtils.getPlayerHeadstones(player);

        if (index < 1 || index > playerHeadstones.size()) {
            Message.sendMessage(sender, "&cHeadstone not found. Use /hs list to see your headstones.");
            return true;
        }

        // Adjust for 0-based index
        ConfigurationSection hs = playerHeadstones.get(index - 1);
        String worldName = hs.getString("world");
        double x = hs.getDouble("x");
        double y = hs.getDouble("y");
        double z = hs.getDouble("z");
        float yaw = 0; // Default yaw
        float pitch = 0; // Default pitch

        World world = Bukkit.getWorld(worldName);
        if (world == null) {
            Message.sendMessage(sender, "&cThe world '" + worldName + "' is not loaded.");
            return true;
        }

        Location location = new Location(world, x, y + 1, z, yaw, pitch); // Teleport slightly above to avoid sticking in ground?
        // Actually Headstone saves block location. Usually users want to stand on top or near it.
        // If the headstone is at y=60, the block occupies y=60. Standing at y=60 would be inside it.
        // Usually teleport to center of block + 1 up?
        // Let's check how headstones are saved.
        // HeadstoneTest says: location.getBlockX()...
        // Headstone saves: x, y, z.
        // If I tp to integer coordinates, I am at the corner of the block.
        // Better to tp to center: x+0.5, y, z+0.5.
        // And if the block is solid (Headstone is skull/fence?), y might need adjustment.
        // Assuming we want to tp on top of it or at the location where the player died.
        // The saved location is `player.getLocation().getBlockX()`. So it is the block coordinate.
        // So the headstone is AT that block.
        // Ideally we tp to `x+0.5, y+1, z+0.5`.

        location.add(0.5, 0, 0.5);
        player.teleport(location);
        Message.sendMessage(sender, "&aTeleported to headstone #" + index + ".");

        return true;
    }
}
