package tk.alex3025.headstones.utils;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import tk.alex3025.headstones.Headstones;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MessageTest {

    private MockedStatic<Headstones> mockedHeadstones;
    private Headstones plugin;
    private ConfigFile messagesConfig;
    private MockedStatic<ChatColor> mockedChatColor;

    @BeforeEach
    void setUp() {
        mockedHeadstones = mockStatic(Headstones.class);
        plugin = mock(Headstones.class);
        messagesConfig = mock(ConfigFile.class);

        mockedHeadstones.when(Headstones::getInstance).thenReturn(plugin);
        when(plugin.getMessages()).thenReturn(messagesConfig);

        // Mock ChatColor.translateAlternateColorCodes to just return the string (or replace & with § if needed, but for test simplicity identity is fine usually, but method signature needs to be matched)
        mockedChatColor = mockStatic(ChatColor.class);
        mockedChatColor.when(() -> ChatColor.translateAlternateColorCodes(anyChar(), anyString()))
                .thenAnswer(invocation -> {
                    String s = invocation.getArgument(1);
                    return s.replace('&', '§');
                });
    }

    @AfterEach
    void tearDown() {
        mockedHeadstones.close();
        mockedChatColor.close();
    }

    @Test
    void testSendText() {
        CommandSender sender = mock(CommandSender.class);
        Message message = new Message(sender);

        message.text("Hello").prefixed(false).send();

        verify(sender).sendMessage("Hello");
    }

    @Test
    void testSendPrefixed() {
        CommandSender sender = mock(CommandSender.class);
        when(messagesConfig.getString("prefix")).thenReturn("&7[Headstones]");

        Message message = new Message(sender);
        message.text("Hello").prefixed(true).send();

        // ChatColor is mocked to replace & with §
        verify(sender).sendMessage("§7[Headstones] Hello");
    }

    @Test
    void testSendTranslation() {
        CommandSender sender = mock(CommandSender.class);
        when(messagesConfig.getString("test-key")).thenReturn("Translated Message");

        Message message = new Message(sender);
        message.translation("test-key").prefixed(false).send();

        verify(sender).sendMessage("Translated Message");
    }

    @Test
    void testPlaceholders() {
        CommandSender sender = mock(CommandSender.class);
        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("player", "Alex");

        Message message = new Message(sender, placeholders);
        message.text("Hello %player%").prefixed(false).send();

        verify(sender).sendMessage("Hello Alex");
    }

    @Test
    void testGetTranslation() {
        when(messagesConfig.getString("key")).thenReturn("value");
        assertEquals("value", Message.getTranslation("key"));
    }

    @Test
    void testStaticSendPrefixedMessage() {
        CommandSender sender = mock(CommandSender.class);
        when(messagesConfig.getString("prefix")).thenReturn("Prefix");

        Message.sendPrefixedMessage(sender, "Content");

        verify(sender).sendMessage("Prefix Content");
    }

    @Test
    void testSendEmpty() {
        CommandSender sender = mock(CommandSender.class);
        new Message(sender).send(); // rawMessage is null
        verify(sender, never()).sendMessage(anyString());

        new Message(sender).text("").send();
        verify(sender, never()).sendMessage(anyString());
    }
}
