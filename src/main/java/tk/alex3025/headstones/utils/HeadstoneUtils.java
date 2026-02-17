package tk.alex3025.headstones.utils;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import tk.alex3025.headstones.Headstones;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class HeadstoneUtils {

    public static List<ConfigurationSection> getPlayerHeadstones(Player player) {
        return getPlayerHeadstones(player.getUniqueId());
    }

    public static List<ConfigurationSection> getPlayerHeadstones(UUID playerUUID) {
        ConfigurationSection headstones = Headstones.getInstance().getDatabase().getConfigurationSection("headstones");

        if (headstones == null) {
            return List.of();
        }

        return headstones.getKeys(false).stream()
            .map(headstones::getConfigurationSection)
            .filter(Objects::nonNull)
            .filter(hs -> playerUUID.toString().equals(hs.getString("owner")))
            .toList();
    }
}
