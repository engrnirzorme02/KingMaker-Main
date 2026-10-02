package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.ui.localization.AppLanguage
import com.example.ui.localization.BengaliStrings
import com.example.ui.localization.EnglishStrings
import com.example.ui.localization.LocalizationManager
import com.example.ui.localization.getAppStrings
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class LocalizationTest {

    private lateinit var context: Context
    private lateinit var localizationManager: LocalizationManager

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        // Clear prefs before test
        val prefs = context.getSharedPreferences("kingmaker_localization_prefs", Context.MODE_PRIVATE)
        prefs.edit().clear().commit()
        localizationManager = LocalizationManager(context)
    }

    @Test
    fun testDefaultLanguageIsBengali() {
        // Bengali must be the default language
        assertEquals(AppLanguage.BN, AppLanguage.DEFAULT)
        assertEquals(AppLanguage.BN, localizationManager.currentLanguage.value)
        assertEquals("bn", localizationManager.currentLanguage.value.code)
        assertEquals("বাংলা", localizationManager.currentLanguage.value.nativeName)
    }

    @Test
    fun testLanguageSwitchingAndPersistence() {
        // Switch to English
        localizationManager.setLanguage(AppLanguage.EN)
        assertEquals(AppLanguage.EN, localizationManager.currentLanguage.value)

        // Create new manager instance and verify it restores English from SharedPreferences
        val restoredManager = LocalizationManager(context)
        assertEquals(AppLanguage.EN, restoredManager.currentLanguage.value)

        // Switch back to Bengali
        restoredManager.setLanguage(AppLanguage.BN)
        assertEquals(AppLanguage.BN, restoredManager.currentLanguage.value)

        val reloadedManager = LocalizationManager(context)
        assertEquals(AppLanguage.BN, reloadedManager.currentLanguage.value)
    }

    @Test
    fun testStringsMapping() {
        val bnStrings = getAppStrings(AppLanguage.BN)
        val enStrings = getAppStrings(AppLanguage.EN)

        // Assert Bengali Strings
        assertEquals(BengaliStrings, bnStrings)
        assertEquals("NIRZOR KINGMAKER", bnStrings.appName)
        assertEquals("বাংলা (ডিফল্ট)", bnStrings.languageBengali)
        assertTrue(bnStrings.languagePill.contains("বাংলা"))
        assertNotNull(bnStrings.intakeHeader)
        assertNotNull(bnStrings.warRoomHeader)
        assertNotNull(bnStrings.blueprintHeader)

        // Assert English Strings
        assertEquals(EnglishStrings, enStrings)
        assertEquals("NIRZOR KINGMAKER", enStrings.appName)
        assertEquals("English", enStrings.languageEnglish)
        assertTrue(enStrings.languagePill.contains("English"))
        assertNotNull(enStrings.intakeHeader)
        assertNotNull(enStrings.warRoomHeader)
        assertNotNull(enStrings.blueprintHeader)
    }

    @Test
    fun testFromCodeFallback() {
        assertEquals(AppLanguage.BN, AppLanguage.fromCode("bn"))
        assertEquals(AppLanguage.EN, AppLanguage.fromCode("en"))
        assertEquals(AppLanguage.BN, AppLanguage.fromCode("unknown_code"))
        assertEquals(AppLanguage.BN, AppLanguage.fromCode(null))
    }
}
