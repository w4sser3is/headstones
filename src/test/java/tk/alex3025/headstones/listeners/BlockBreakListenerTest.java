package tk.alex3025.headstones.listeners;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import tk.alex3025.headstones.Headstones;
import tk.alex3025.headstones.utils.ConfigFile;
import tk.alex3025.headstones.utils.Headstone;
import tk.alex3025.headstones.utils.Message;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class BlockBreakListenerTest {

    private BlockBreakListener listener;
    private MockedStatic<Headstone> mockedHeadstone;
    private MockedStatic<Headstones> mockedHeadstonesPlugin;
    private MockedConstruction<Message> mockedMessage;

    @BeforeEach
    void setUp() {
        listener = new BlockBreakListener();
        mockedHeadstone = mockStatic(Headstone.class);
        mockedHeadstonesPlugin = mockStatic(Headstones.class);

        // Mock Message construction to avoid it calling Headstones.getInstance() internally if possible,
        // or just mock Headstones.getInstance() to return a mock that handles it.
        // However, Message constructor is simple, but translation() calls Headstones.getInstance().
        // So we can mock construction of Message to return a mock.
        mockedMessage = mockConstruction(Message.class, (mock, context) -> {
            when(mock.translation(anyString())).thenReturn(mock);
            when(mock.prefixed(anyBoolean())).thenReturn(mock);
        });

        Headstones plugin = mock(Headstones.class);
        ConfigFile messages = mock(ConfigFile.class);
        when(plugin.getMessages()).thenReturn(messages);
        when(messages.getString(anyString())).thenReturn("mocked message");
        mockedHeadstonesPlugin.when(Headstones::getInstance).thenReturn(plugin);
    }

    @AfterEach
    void tearDown() {
        mockedHeadstone.close();
        mockedHeadstonesPlugin.close();
        mockedMessage.close();
    }

    @Test
    void testBreakByOpponentDefaultDenied() {
        // Arrange
        BlockBreakEvent event = mock(BlockBreakEvent.class);
        Block block = mock(Block.class);
        Player opponent = mock(Player.class);
        Player owner = mock(Player.class);
        Headstone headstone = mock(Headstone.class);

        when(event.getBlock()).thenReturn(block);
        when(event.getPlayer()).thenReturn(opponent);
        when(Headstone.fromBlock(block)).thenReturn(headstone);

        // Setup owner and opponent
        UUID ownerId = UUID.randomUUID();
        UUID opponentId = UUID.randomUUID();
        when(owner.getUniqueId()).thenReturn(ownerId);
        when(opponent.getUniqueId()).thenReturn(opponentId);

        // Headstone logic
        when(headstone.isOwner(opponent)).thenReturn(false);
        when(headstone.getOwner()).thenReturn(owner);
        when(owner.isOnline()).thenReturn(true);
        when(owner.getPlayer()).thenReturn(owner);

        // Default: owner does not have permission
        when(owner.hasPermission("headstones.allow-opponents")).thenReturn(false);

        // Act
        listener.onBlockBreak(event);

        // Assert
        verify(event).setCancelled(true);
        verify(headstone, never()).onBreak(event);
    }

    @Test
    void testBreakByOpponentAllowedWhenPermissionSet() {
        // Arrange
        BlockBreakEvent event = mock(BlockBreakEvent.class);
        Block block = mock(Block.class);
        Player opponent = mock(Player.class);
        Player owner = mock(Player.class);
        Headstone headstone = mock(Headstone.class);

        when(event.getBlock()).thenReturn(block);
        when(event.getPlayer()).thenReturn(opponent);
        when(Headstone.fromBlock(block)).thenReturn(headstone);

        // Setup owner and opponent
        UUID ownerId = UUID.randomUUID();
        UUID opponentId = UUID.randomUUID();
        when(owner.getUniqueId()).thenReturn(ownerId);
        when(opponent.getUniqueId()).thenReturn(opponentId);

        // Headstone logic
        when(headstone.isOwner(opponent)).thenReturn(false);
        when(headstone.getOwner()).thenReturn(owner);
        when(owner.isOnline()).thenReturn(true);
        when(owner.getPlayer()).thenReturn(owner);

        // Owner HAS permission
        when(owner.hasPermission("headstones.allow-opponents")).thenReturn(true);

        // Act
        listener.onBlockBreak(event);

        // Assert
        verify(event, never()).setCancelled(true);
        verify(headstone).onBreak(event);
    }

    @Test
    void testBreakByOpponentDeniedWhenOwnerOffline() {
        // Arrange
        BlockBreakEvent event = mock(BlockBreakEvent.class);
        Block block = mock(Block.class);
        Player opponent = mock(Player.class);
        Player owner = mock(Player.class); // This acts as OfflinePlayer too
        Headstone headstone = mock(Headstone.class);

        when(event.getBlock()).thenReturn(block);
        when(event.getPlayer()).thenReturn(opponent);
        when(Headstone.fromBlock(block)).thenReturn(headstone);

        when(headstone.isOwner(opponent)).thenReturn(false);
        when(headstone.getOwner()).thenReturn(owner); // getOwner returns OfflinePlayer
        when(owner.isOnline()).thenReturn(false);

        // Act
        listener.onBlockBreak(event);

        // Assert
        verify(event).setCancelled(true);
        verify(headstone, never()).onBreak(event);
    }
}
