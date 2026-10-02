package com.example.ui.localization

/**
 * Supported application languages.
 * BN (বাংলা) is the default language as requested.
 */
enum class AppLanguage(
    val code: String,
    val nativeName: String,
    val englishName: String
) {
    BN("bn", "বাংলা", "Bengali"),
    EN("en", "English", "English");

    companion object {
        val DEFAULT = BN

        fun fromCode(code: String?): AppLanguage {
            return entries.find { it.code.equals(code, ignoreCase = true) } ?: DEFAULT
        }
    }
}
