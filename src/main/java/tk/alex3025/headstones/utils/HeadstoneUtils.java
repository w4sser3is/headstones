package tk.alex3025.headstones.utils;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import tk.alex3025.headstones.Headstones;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class HeadstoneUtils {

    public static List<ConfigurationSection> getPlayerHeadstones(Player player) {
        return getPlayerHeadstones(player.getUniqueId());
    }

    public static List<ConfigurationSection> getPlayerHeadstones(UUID playerUUID) {
        List<ConfigurationSection> playerHeadstones = new ArrayList<>();
        ConfigurationSection headstones = Headstones.getInstance().getDatabase().getConfigurationSection("headstones");

        if (headstones != null) {
            for (String key : headstones.getKeys(false)) {
                ConfigurationSection hs = headstones.getConfigurationSection(key);
                if (hs != null) {
                    String ownerString = hs.getString("owner");
                    if (ownerString != null && ownerString.equals(playerUUID.toString())) {
                        playerHeadstones.add(hs);
                    }
                }
            }
        }
        return playerHeadstones;
    }
}
