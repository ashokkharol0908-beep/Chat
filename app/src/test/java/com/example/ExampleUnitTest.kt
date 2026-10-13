package com.example

import com.example.data.model.MoodType
import com.example.data.model.VibeDataBank
import com.example.data.model.VibeType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    @Test
    fun vibeDataBank_hasSufficientPrompts() {
        assertTrue(VibeDataBank.DARES.size >= 5)
        assertTrue(VibeDataBank.TRUTHS.size >= 5)
        assertTrue(VibeDataBank.ROASTS.size >= 5)
        assertTrue(VibeDataBank.COMPLIMENTS.size >= 5)
        assertTrue(VibeDataBank.CHAOS_CHALLENGES.size >= 4)
        assertTrue(VibeDataBank.STICKERS.size >= 10)
    }

    @Test
    fun moodType_hasValidMappings() {
        assertEquals("😎", MoodType.HAPPY.emoji)
        assertEquals("Vibing", MoodType.HAPPY.title)
        assertEquals("🧠", MoodType.LOCKED_IN.emoji)
        assertEquals("Menace", MoodType.CHAOS.title)
    }

    @Test
    fun vibeType_hasValidIcons() {
        assertEquals("🔥", VibeType.DARE.icon)
        assertEquals("🔮", VibeType.TRUTH.icon)
        assertEquals("💀", VibeType.ROAST.icon)
        assertEquals("✨", VibeType.COMPLIMENT.icon)
    }

    @Test
    fun appThemes_areProperlyConfigured() {
        assertEquals(5, com.example.ui.theme.AppTheme.entries.size)
        assertTrue(com.example.ui.theme.AppTheme.entries.any { it.id == "matrix_lime" })
        assertTrue(com.example.ui.theme.AppTheme.entries.any { it.id == "tokyo_sunset" })
        assertTrue(com.example.ui.theme.AppTheme.entries.any { it.id == "ocean_abyss" })
        assertTrue(com.example.ui.theme.AppTheme.entries.any { it.id == "crimson_viper" })
    }
}
