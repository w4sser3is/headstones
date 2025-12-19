package tk.alex3025.headstones.utils;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import tk.alex3025.headstones.Headstones;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class HeadstoneTest {

    private MockedStatic<Headstones> mockedHeadstones;
    private MockedStatic<Bukkit> mockedBukkit;
    private MockedStatic<ExperienceManager> mockedExperienceManager;
    private Headstones plugin;
    private ConfigFile configFile;

    @BeforeEach
    void setUp() {
        mockedHeadstones = mockStatic(Headstones.class);
        mockedBukkit = mockStatic(Bukkit.class);
        mockedExperienceManager = mockStatic(ExperienceManager.class);

        plugin = mock(Headstones.class);
        configFile = mock(ConfigFile.class);

        mockedHeadstones.when(Headstones::getInstance).thenReturn(plugin);
        when(plugin.getDatabase()).thenReturn(configFile);
    }

    @AfterEach
    void tearDown() {
        mockedHeadstones.close();
        mockedBukkit.close();
        mockedExperienceManager.close();
    }

    @Test
    void testConstructorWithPlayer() {
        Player player = mock(Player.class);
        Location location = mock(Location.class);
        PlayerInventory inventory = mock(PlayerInventory.class);
        ItemStack[] contents = new ItemStack[0];

        when(player.getLocation()).thenReturn(location);
        when(player.getInventory()).thenReturn(inventory);
        when(inventory.getContents()).thenReturn(contents);
        when(player.getUniqueId()).thenReturn(UUID.randomUUID());

        mockedExperienceManager.when(() -> ExperienceManager.getExperience(player)).thenReturn(100);

        Headstone headstone = new Headstone(player);

        assertEquals(player, headstone.getOwner());
        assertEquals(location, headstone.getLocation());
        assertTrue(headstone.getTimestamp() > 0);
        assertTrue(headstone.isOwner(player));
    }

    @Test
    void testFromUUID() {
        String uuid = "test-uuid";
        String worldName = "world";
        String ownerUUID = UUID.randomUUID().toString();

        ConfigurationSection headstonesSection = mock(ConfigurationSection.class);
        ConfigurationSection hsSection = mock(ConfigurationSection.class);

        when(configFile.getConfigurationSection("headstones")).thenReturn(headstonesSection);
        when(headstonesSection.getConfigurationSection(uuid)).thenReturn(hsSection);

        when(hsSection.getString("world")).thenReturn(worldName);
        when(hsSection.getDouble("x")).thenReturn(10.0);
        when(hsSection.getDouble("y")).thenReturn(64.0);
        when(hsSection.getDouble("z")).thenReturn(10.0);
        when(hsSection.getString("owner")).thenReturn(ownerUUID);
        when(hsSection.getLong("timestamp")).thenReturn(123456789L);
        when(hsSection.getInt("experience", 0)).thenReturn(50);
        when(hsSection.getString("inventory")).thenReturn(null);

        World world = mock(World.class);
        mockedBukkit.when(() -> Bukkit.getWorld(worldName)).thenReturn(world);
        OfflinePlayer offlinePlayer = mock(OfflinePlayer.class);
        mockedBukkit.when(() -> Bukkit.getOfflinePlayer(UUID.fromString(ownerUUID))).thenReturn(offlinePlayer);

        Headstone headstone = Headstone.fromUUID(uuid);

        assertNotNull(headstone);
        assertEquals(offlinePlayer, headstone.getOwner());
        assertEquals(123456789L, headstone.getTimestamp());
        assertEquals(world, headstone.getLocation().getWorld());
    }

    @Test
    void testFromLocation() {
        String uuid = "test-uuid";
        Location location = new Location(mock(World.class), 10, 64, 10);

        ConfigurationSection headstonesSection = mock(ConfigurationSection.class);
        when(configFile.getConfigurationSection("headstones")).thenReturn(headstonesSection);

        Set<String> keys = new HashSet<>();
        keys.add(uuid);
        when(headstonesSection.getKeys(false)).thenReturn(keys);

        ConfigurationSection hsSection = mock(ConfigurationSection.class);
        when(headstonesSection.getConfigurationSection(uuid)).thenReturn(hsSection);

        when(hsSection.getString("world")).thenReturn("world");
        when(hsSection.getDouble("x")).thenReturn(10.0);
        when(hsSection.getDouble("y")).thenReturn(64.0);
        when(hsSection.getDouble("z")).thenReturn(10.0);
        when(hsSection.getString("owner")).thenReturn(UUID.randomUUID().toString());

        World worldMock = location.getWorld();
        // Location constructor does NOT check isLoaded if we pass a mock that mocks it?
        // Actually new Location(world, ...) calls world.getClass() etc?
        // The error "World unloaded" comes from Location.getWorld() -> Preconditions.checkArgument.
        // So we need to ensure mock(World.class) returns isLoaded() = true?
        // But isLoaded() is not in World interface in older versions? It seems it is effectively checking something.
        // Wait, the stacktrace showed Location.getWorld(Location.java:110).
        // If we look at Bukkit source, Location methods check validity.
        // Whatever, we try to mock it.

        // But wait, "Location" is a concrete class. "World" is an interface.
        // If I mock World, calling location.getWorld() returns the mock.
        // The check inside Location.getWorld() does:
        // if (world == null) return null;
        // if (!world.isLoaded()) throw ...
        // So yes, we need to mock isLoaded(). Or similar depending on version.
        // World interface usually doesn't have isLoaded()?
        // Actually, it seems implementation detail.
        // But let's try assuming there is no isLoaded method on interface and Location uses something else?
        // Check stack trace: `com.google.common.base.Preconditions.checkArgument`

        // If World is mocked, method calls return defaults (false/null/0).
        // So we need to stub whatever check it does.
        // If the method doesn't exist on interface, Mockito can't mock it unless we cast?
        // But World is interface.

        mockedBukkit.when(() -> Bukkit.getWorld("world")).thenReturn(location.getWorld());
        mockedBukkit.when(() -> Bukkit.getOfflinePlayer(any(UUID.class))).thenReturn(mock(OfflinePlayer.class));

        Headstone headstone = Headstone.fromLocation(location);

        assertNotNull(headstone);
        assertEquals(location, headstone.getLocation());
    }

    @Test
    void testOnPlayerDeath() {
        // Testing onPlayerDeath is difficult because it relies on createPlayerSkull -> checkForSafeBlock -> block.getType().isEmpty()
        // Material.isEmpty() triggers static Registry access which crashes in unit tests.
        // However, we can test that the method proceeds to create a safe block if we can find a way to make checkForSafeBlock return null or a block.
        // Since we cannot mock Material enum methods easily to return true/false without registry,
        // we will verify that at least the method calls getBlockAt.

        Player player = mock(Player.class);
        Location location = mock(Location.class);
        World world = mock(World.class);
        Block block = mock(Block.class);

        when(player.getLocation()).thenReturn(location);
        when(location.getWorld()).thenReturn(world);
        when(location.getBlockX()).thenReturn(10);
        when(location.getBlockY()).thenReturn(64);
        when(location.getBlockZ()).thenReturn(10);
        when(player.getUniqueId()).thenReturn(UUID.randomUUID());

        when(player.getInventory()).thenReturn(mock(PlayerInventory.class));
        when(player.getInventory().getContents()).thenReturn(new ItemStack[0]);

        // Mock World.getBlockAt to return a block
        when(world.getBlockAt(anyInt(), anyInt(), anyInt())).thenReturn(block);

        // Mock block.getType() to return a mock Material instead of a real Enum to avoid registry crash?
        // But getType return type is Material. We can mock the class Material.
        Material mockMaterial = mock(Material.class);
        when(block.getType()).thenReturn(mockMaterial);
        // And mock isEmpty() on it
        when(mockMaterial.isEmpty()).thenReturn(false);

        Headstone headstone = new Headstone(player);
        PlayerDeathEvent event = mock(PlayerDeathEvent.class);
        when(event.getDrops()).thenReturn(new java.util.ArrayList<>());

        headstone.onPlayerDeath(event, true, true);

        // Verify it tried to check for safe block
        verify(world, atLeastOnce()).getBlockAt(anyInt(), anyInt(), anyInt());
    }

    @Test
    void testRestorePlayerInventory() {
        // Setup Headstone with inventory
        Player player = mock(Player.class);
        Location location = mock(Location.class);
        PlayerInventory playerInventory = mock(PlayerInventory.class);

        when(player.getLocation()).thenReturn(location);
        when(player.getInventory()).thenReturn(playerInventory);

        ItemStack mockedItem = mock(ItemStack.class);
        when(playerInventory.getContents()).thenReturn(new ItemStack[]{mockedItem});
        when(player.getUniqueId()).thenReturn(UUID.randomUUID());

        mockedExperienceManager.when(() -> ExperienceManager.getExperience(player)).thenReturn(100);

        Headstone headstone = new Headstone(player);

        // Prepare restore
        // We mocked ExperienceManager, so setExperience will be mocked.

        // Case 1: Slot is empty
        when(playerInventory.getItem(0)).thenReturn(null);

        try {
            java.lang.reflect.Method method = Headstone.class.getDeclaredMethod("restorePlayerInventory", Player.class);
            method.setAccessible(true);
            method.invoke(headstone, player);

            // Verifications
            mockedExperienceManager.verify(() -> ExperienceManager.setExperience(player, 100));
            verify(playerInventory).setItem(eq(0), eq(mockedItem));

        } catch (Exception e) {
            fail(e);
        }

        // Case 2: Slot is occupied
        when(playerInventory.getItem(0)).thenReturn(mock(ItemStack.class));
        java.util.HashMap<Integer, ItemStack> drops = new java.util.HashMap<>();
        drops.put(0, mockedItem);
        when(playerInventory.addItem(any(ItemStack.class))).thenReturn(drops);
        when(player.getWorld()).thenReturn(mock(World.class));

        try {
            java.lang.reflect.Method method = Headstone.class.getDeclaredMethod("restorePlayerInventory", Player.class);
            method.setAccessible(true);
            method.invoke(headstone, player);

            verify(player.getWorld()).dropItem(eq(location), eq(mockedItem));

        } catch (Exception e) {
            fail(e);
        }
    }

    @Test
    void testFromBlock() {
        Block block = mock(Block.class);

        // Use Material.DIRT. Accessing it shouldn't crash unless static init block runs logic.
        when(block.getType()).thenReturn(Material.DIRT);

        Headstone result = Headstone.fromBlock(block);
        assertNull(result);
    }

    @Test
    void testSavePlayerData() {
        // Test private savePlayerData via reflection
        Player player = mock(Player.class);
        when(player.getInventory()).thenReturn(mock(PlayerInventory.class));
        when(player.getInventory().getContents()).thenReturn(new ItemStack[0]);
        when(player.getLocation()).thenReturn(mock(Location.class));
        UUID uuid = UUID.randomUUID();
        when(player.getUniqueId()).thenReturn(uuid);
        mockedExperienceManager.when(() -> ExperienceManager.getExperience(player)).thenReturn(100);

        Headstone headstone = new Headstone(player);

        Location skullLocation = mock(Location.class);
        when(skullLocation.getX()).thenReturn(10.0);
        when(skullLocation.getY()).thenReturn(64.0);
        when(skullLocation.getZ()).thenReturn(10.0);
        World world = mock(World.class);
        when(skullLocation.getWorld()).thenReturn(world);
        when(world.getName()).thenReturn("world");

        ConfigurationSection section = mock(ConfigurationSection.class);
        when(configFile.createSection(anyString())).thenReturn(section);

        try {
            java.lang.reflect.Method method = Headstone.class.getDeclaredMethod("savePlayerData", Location.class, boolean.class, boolean.class);
            method.setAccessible(true);
            method.invoke(headstone, skullLocation, true, true);

            // Access private uuid field to verify correctly
            java.lang.reflect.Field uuidField = Headstone.class.getDeclaredField("uuid");
            uuidField.setAccessible(true);
            String hsUuid = (String) uuidField.get(headstone);

            verify(configFile).createSection("headstones." + hsUuid);

            verify(section).set("owner", uuid.toString());
            verify(section).set("x", 10);
            verify(section).set("y", 64);
            verify(section).set("z", 10);
            verify(section).set("world", "world");
            verify(section).set("experience", 100);
            verify(section).set(eq("inventory"), anyString());

            verify(configFile).save();

        } catch (Exception e) {
            fail(e);
        }
    }

    @Test
    void testDeletePlayerData() {
        Player player = mock(Player.class);
        when(player.getInventory()).thenReturn(mock(PlayerInventory.class));
        when(player.getInventory().getContents()).thenReturn(new ItemStack[0]);
        when(player.getLocation()).thenReturn(mock(Location.class));
        when(player.getUniqueId()).thenReturn(UUID.randomUUID());
        mockedExperienceManager.when(() -> ExperienceManager.getExperience(player)).thenReturn(100);

        Headstone headstone = new Headstone(player);

        ConfigurationSection headstonesSection = mock(ConfigurationSection.class);
        when(configFile.getConfigurationSection("headstones")).thenReturn(headstonesSection);

        try {
            java.lang.reflect.Method method = Headstone.class.getDeclaredMethod("deletePlayerData");
            method.setAccessible(true);
            method.invoke(headstone);

            verify(headstonesSection).set(anyString(), eq(null));
            verify(configFile).save();

        } catch (Exception e) {
            fail(e);
        }
    }
}
