package tk.alex3025.headstones.commands.subcommands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import tk.alex3025.headstones.Headstones;
import tk.alex3025.headstones.utils.ConfigFile;
import tk.alex3025.headstones.utils.ExperienceManager;
import tk.alex3025.headstones.utils.Headstone;
import tk.alex3025.headstones.utils.HeadstoneUtils;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class DestroyHeadstoneCommandTest {

    private MockedStatic<Headstones> mockedHeadstones;
    private MockedStatic<Bukkit> mockedBukkit;
    private MockedStatic<HeadstoneUtils> mockedHeadstoneUtils;
    private MockedStatic<Headstone> mockedHeadstone;
    private MockedStatic<ExperienceManager> mockedExperienceManager;
    private MockedStatic<ChatColor> mockedChatColor;

    private Headstones plugin;
    private ConfigFile database;
    private ConfigFile messages;

    @BeforeEach
    void setUp() {
        mockedHeadstones = mockStatic(Headstones.class);
        mockedBukkit = mockStatic(Bukkit.class);
        mockedHeadstoneUtils = mockStatic(HeadstoneUtils.class);
        mockedHeadstone = mockStatic(Headstone.class);
        mockedExperienceManager = mockStatic(ExperienceManager.class);
        mockedChatColor = mockStatic(ChatColor.class);

        plugin = mock(Headstones.class);
        database = mock(ConfigFile.class);
        messages = mock(ConfigFile.class);

        mockedHeadstones.when(Headstones::getInstance).thenReturn(plugin);
        when(plugin.getDatabase()).thenReturn(database);
        when(plugin.getMessages()).thenReturn(messages);

        mockedChatColor.when(() -> ChatColor.translateAlternateColorCodes(anyChar(), anyString()))
                .thenAnswer(invocation -> invocation.getArgument(1));
    }

    @AfterEach
    void tearDown() {
        mockedHeadstones.close();
        mockedBukkit.close();
        mockedHeadstoneUtils.close();
        mockedHeadstone.close();
        mockedExperienceManager.close();
        mockedChatColor.close();
    }

    @Test
    void testOfflinePlayerAborts() {
        CommandSender sender = mock(CommandSender.class);
        OfflinePlayer target = mock(OfflinePlayer.class);
        UUID targetUUID = UUID.randomUUID();
        when(target.getUniqueId()).thenReturn(targetUUID);
        when(target.hasPlayedBefore()).thenReturn(true);
        when(target.isOnline()).thenReturn(false);

        mockedBukkit.when(() -> Bukkit.getOfflinePlayer("Steve")).thenReturn(target);

        DestroyHeadstoneCommand command = new DestroyHeadstoneCommand();
        command.onCommand(sender, new String[]{"Steve"});

        mockedHeadstoneUtils.verify(() -> HeadstoneUtils.getPlayerHeadstones(any(UUID.class)), never());
    }

    @Test
    void testAdditiveExperienceRestore() {
        CommandSender sender = mock(CommandSender.class);
        Player onlinePlayer = mock(Player.class);
        OfflinePlayer target = mock(OfflinePlayer.class);
        UUID targetUUID = UUID.randomUUID();

        when(target.getUniqueId()).thenReturn(targetUUID);
        when(target.hasPlayedBefore()).thenReturn(true);
        when(target.isOnline()).thenReturn(true);
        when(target.getPlayer()).thenReturn(onlinePlayer);
        when(target.getName()).thenReturn("Steve");

        mockedBukkit.when(() -> Bukkit.getOfflinePlayer("Steve")).thenReturn(target);

        ConfigurationSection hsSection = mock(ConfigurationSection.class);
        when(hsSection.getName()).thenReturn("uuid-1");
        mockedHeadstoneUtils.when(() -> HeadstoneUtils.getPlayerHeadstones(targetUUID)).thenReturn(List.of(hsSection));

        Headstone headstone = mock(Headstone.class);
        when(headstone.getExperience()).thenReturn(50);
        when(headstone.getInventory()).thenReturn(null);

        Location location = mock(Location.class);
        Block block = mock(Block.class);
        when(headstone.getLocation()).thenReturn(location);
        when(location.getBlock()).thenReturn(block);

        mockedHeadstone.when(() -> Headstone.fromUUID("uuid-1")).thenReturn(headstone);

        mockedExperienceManager.when(() -> ExperienceManager.getExperience(onlinePlayer)).thenReturn(200);

        DestroyHeadstoneCommand command = new DestroyHeadstoneCommand();
        command.onCommand(sender, new String[]{"Steve"});

        mockedExperienceManager.verify(() -> ExperienceManager.setExperience(onlinePlayer, 250));
        verify(headstone).deletePlayerData();
    }
}
