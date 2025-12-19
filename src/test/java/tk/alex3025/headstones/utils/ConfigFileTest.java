package tk.alex3025.headstones.utils;

import org.bukkit.configuration.InvalidConfigurationException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mockito;
import tk.alex3025.headstones.Headstones;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ConfigFileTest {

    private Headstones plugin;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        plugin = mock(Headstones.class);
        when(plugin.getDataFolder()).thenReturn(tempDir.toFile());

        // Clear static CONFIGS list
        try {
            java.lang.reflect.Field configsField = ConfigFile.class.getDeclaredField("CONFIGS");
            configsField.setAccessible(true);
            java.util.List<?> configs = (java.util.List<?>) configsField.get(null);
            configs.clear();
        } catch (Exception e) {
            fail(e);
        }
    }

    @Test
    void testCreateOrLoadConfig() throws IOException, InvalidConfigurationException {
        // Setup a dummy file in temp dir to simulate saveResource
        File configFile = tempDir.resolve("config.yml").toFile();
        configFile.createNewFile();

        // Mock saveResource to do nothing (file already created)
        doNothing().when(plugin).saveResource(anyString(), anyBoolean());

        ConfigFile config = new ConfigFile(plugin, "config.yml");

        // Use reflection to access private file field
        try {
            java.lang.reflect.Field fileField = ConfigFile.class.getDeclaredField("file");
            fileField.setAccessible(true);
            File file = (File) fileField.get(config);
            assertTrue(file.exists());
            assertEquals("config.yml", file.getName());
        } catch (Exception e) {
            fail(e);
        }

        // Set a value and save
        config.set("key", "value");
        config.save();

        // Reload
        config.reload();
        assertEquals("value", config.getString("key"));
    }

    @Test
    void testReloadAll() throws IOException {
         File file1 = tempDir.resolve("test1.yml").toFile();
         file1.createNewFile();
         File file2 = tempDir.resolve("test2.yml").toFile();
         file2.createNewFile();

         ConfigFile config1 = new ConfigFile(plugin, "test1.yml");
         ConfigFile config2 = new ConfigFile(plugin, "test2.yml");

         ConfigFile.reloadAll();

         // If no exception, it passed.
    }
}
