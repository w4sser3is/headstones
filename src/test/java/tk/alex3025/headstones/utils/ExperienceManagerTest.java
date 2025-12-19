package tk.alex3025.headstones.utils;

import org.bukkit.entity.Player;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class ExperienceManagerTest {

    @ParameterizedTest
    @CsvSource({
            "0, 0",
            "1, 7",
            "15, 315",
            "16, 352",
            "30, 1395",
            "31, 1507",
            "32, 1628"
    })
    void testGetExperienceByLevel(int level, int expectedXp) {
        assertEquals(expectedXp, ExperienceManager.getExperience(level));
    }

    @Test
    void testGetExperienceFromPlayer() {
        Player player = mock(Player.class);

        // Mock player state: level 1, 50% towards level 2.
        // Level 1 XP = 7 (from previous test)
        // Level 2 XP cost = 2*2 + 6*2 = 16? No.
        // XP to next level formula:
        // level 0-15: 2*level + 7
        // level 16-30: 5*level - 38
        // level 31+: 9*level - 158

        // Level 1 XP total = 7.
        // Level 1 to 2 needs: 2*1 + 7 = 9 xp.

        // If player is level 1 and has 0.5 exp progress.
        // Base XP for level 1 = 7.
        // Current progress XP = 0.5 * 9 = 4.5 -> round to 5?

        when(player.getLevel()).thenReturn(1);
        when(player.getExp()).thenReturn(0.5f);
        when(player.getExpToLevel()).thenReturn(9);

        // Expected: 7 (base) + 5 (partial) = 12
        int xp = ExperienceManager.getExperience(player);
        assertEquals(12, xp);
    }

    @Test
    void testSetExperience() {
        Player player = mock(Player.class);

        // Let's set XP to 7 (Level 1, 0 progress)
        ExperienceManager.setExperience(player, 7);

        verify(player).setLevel(1);
        verify(player).setExp(0);
        verify(player).giveExp(0);

         // Let's set XP to 12 (Level 1 + 5 XP)
        reset(player);
        ExperienceManager.setExperience(player, 12);

        verify(player).setLevel(1);
        verify(player).setExp(0);
        verify(player).giveExp(5);
    }
}
