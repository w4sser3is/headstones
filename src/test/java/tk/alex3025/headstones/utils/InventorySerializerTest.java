package tk.alex3025.headstones.utils;

import org.bukkit.inventory.ItemStack;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class InventorySerializerTest {

    @Test
    void testSerialize() {
        // This is tricky because BukkitObjectOutputStream is instantiated inside the method.
        // And it's a real class in the API.
        // But since we are running unit tests without a server, we don't know if it works.

        // Let's assume for a moment we can mock the static method serialize of InventorySerializer?
        // No, we want to test the method itself.

        // The method does:
        // 1. new BukkitObjectOutputStream
        // 2. dataOutput.writeInt
        // 3. item.serializeAsBytes()
        // 4. Base64Coder.encodeLines

        // If we cannot mock BukkitObjectOutputStream constructor, we might rely on it being "simple enough".
        // BUT, BukkitObjectOutputStream might check for YAML configuration or Server instance.

        // Let's try to mock ItemStack first.
        ItemStack item = mock(ItemStack.class);
        byte[] bytes = new byte[]{1, 2, 3};
        when(item.serializeAsBytes()).thenReturn(bytes);

        ItemStack[] inventory = new ItemStack[]{item, null};

        // We need to handle BukkitObjectOutputStream.
        // Since we can't easily injection mock the constructor without PowerMock or similar (and we have mockito-inline which supports static mocks but constructor mocks are also supported in newer Mockito).

        try (MockedConstruction<BukkitObjectOutputStream> mockedBoos = mockConstruction(BukkitObjectOutputStream.class,
                (mock, context) -> {
                    // This block is executed when a new instance is created
                })) {

            // Wait, if we mock the construction, the method uses the mock.
            // But the method calls `dataOutput.writeInt(inventoryContents.length);`
            // and `dataOutput.writeObject(...)`.

            // We need to capture what is written to the underlying stream?
            // The method creates `ByteArrayOutputStream` internally too.

            // Actually `BukkitObjectOutputStream` takes the stream in constructor.
            // context.arguments().get(0) should be the stream.

            String result = InventorySerializer.serialize(inventory);

            // Verify what happened on the mock
            BukkitObjectOutputStream mockBoos = mockedBoos.constructed().get(0);
            verify(mockBoos).writeInt(2); // length
            verify(mockBoos).writeObject(bytes); // item 1
            verify(mockBoos).writeObject(null); // item 2

            // The result comes from `Base64Coder.encodeLines(outputStream.toByteArray())`.
            // Since `outputStream` was passed to the REAL constructor of `BukkitObjectOutputStream` (which we mocked), nothing was written to `outputStream` because we mocked the writer!
            // So `outputStream.toByteArray()` will be empty.

            // So we need our mock to actually write to the stream provided in the constructor?
            // Or we just check that the method didn't crash and called the right things?

            // The method returns a String. If the stream is empty, it returns empty string (encoded).
            assertNotNull(result);

        } catch (Exception e) {
            fail(e);
        }
    }

    @Test
    void testDeserialize() {
        String data = "somebase64data";

        // deserialize calls Base64Coder.decodeLines -> ByteArrayInputStream -> BukkitObjectInputStream -> readInt -> readObject -> ItemStack.deserializeBytes

        try (MockedStatic<org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder> mockedBase64 = mockStatic(org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder.class);
             MockedConstruction<BukkitObjectInputStream> mockedBois = mockConstruction(BukkitObjectInputStream.class,
                     (mock, context) -> {
                         when(mock.readInt()).thenReturn(2);
                         when(mock.readObject()).thenReturn(new byte[]{1,2,3}).thenReturn(null);
                     });
             MockedStatic<ItemStack> mockedItemStack = mockStatic(ItemStack.class)) {

            mockedBase64.when(() -> org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder.decodeLines(anyString()))
                    .thenReturn(new byte[]{0});

            ItemStack deserializedItem = mock(ItemStack.class);
            mockedItemStack.when(() -> ItemStack.deserializeBytes(any(byte[].class))).thenReturn(deserializedItem);

            ItemStack[] result = InventorySerializer.deserialize(data);

            assertEquals(2, result.length);
            assertEquals(deserializedItem, result[0]);
            assertNull(result[1]);

        } catch (Exception e) {
            fail(e);
        }
    }
}
