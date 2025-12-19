package tk.alex3025.headstones.commands.subcommands;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;
import tk.alex3025.headstones.Headstones;
import tk.alex3025.headstones.utils.ConfigFile;
import tk.alex3025.headstones.utils.Message;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class TeleportHeadstoneCommandTest {

    private MockedStatic<Headstones> mockedHeadstones;
    private MockedStatic<Bukkit> mockedBukkit;
    private Headstones plugin;
    private ConfigFile database;

    @BeforeEach
    void setUp() {
        mockedHeadstones = mockStatic(Headstones.class);
        mockedBukkit = mockStatic(Bukkit.class);
        plugin = mock(Headstones.class);
        database = mock(ConfigFile.class);

        mockedHeadstones.when(Headstones::getInstance).thenReturn(plugin);
        when(plugin.getDatabase()).thenReturn(database);
    }

    @AfterEach
    void tearDown() {
        mockedHeadstones.close();
        mockedBukkit.close();
    }

    @Test
    void testTeleportSuccess() {
        // Setup player
        Player player = mock(Player.class);
        UUID playerUUID = UUID.randomUUID();
        when(player.getUniqueId()).thenReturn(playerUUID);

        // Setup database
        ConfigurationSection headstones = mock(ConfigurationSection.class);
        when(database.getConfigurationSection("headstones")).thenReturn(headstones);

        Set<String> keys = new HashSet<>();
        keys.add("hs1");
        when(headstones.getKeys(false)).thenReturn(keys);

        ConfigurationSection hs1 = mock(ConfigurationSection.class);
        when(headstones.getConfigurationSection("hs1")).thenReturn(hs1);
        when(hs1.getString("owner")).thenReturn(playerUUID.toString());
        when(hs1.getString("world")).thenReturn("world");
        when(hs1.getDouble("x")).thenReturn(10.0);
        when(hs1.getDouble("y")).thenReturn(60.0);
        when(hs1.getDouble("z")).thenReturn(10.0);

        // Setup world
        World world = mock(World.class);
        mockedBukkit.when(() -> Bukkit.getWorld("world")).thenReturn(world);

        // Execute
        TeleportHeadstoneCommand command = new TeleportHeadstoneCommand();
        command.onCommand(player, new String[]{"1"});

        // Verify
        ArgumentCaptor<Location> locationCaptor = ArgumentCaptor.forClass(Location.class);
        verify(player).teleport(locationCaptor.capture());

        Location loc = locationCaptor.getValue();
        assertEquals(world, loc.getWorld());
        assertEquals(10.5, loc.getX());
        assertEquals(61.0, loc.getY());
    }

    @Test
    void testTeleportInvalidIndex() {
        Player player = mock(Player.class);
        UUID playerUUID = UUID.randomUUID();
        when(player.getUniqueId()).thenReturn(playerUUID);
        when(database.getConfigurationSection("headstones")).thenReturn(null);

        TeleportHeadstoneCommand command = new TeleportHeadstoneCommand();
        command.onCommand(player, new String[]{"1"});

        // Should send message about index invalid or no headstones
        // Since we are mocking everything, we can't easily verify static Message calls unless we mock Message class or check side effects.
        // But Message.sendMessage sends to sender.
        verify(player, atLeastOnce()).sendMessage(anyString());
    }
}
