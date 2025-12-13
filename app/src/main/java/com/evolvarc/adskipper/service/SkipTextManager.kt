package com.evolvarc.adskipper.service

object SkipTextManager {

    data class LanguageOption(
        val code: String,
        val displayName: String,
        val texts: List<String>
    )

    val languages = listOf(
        LanguageOption("en", "English", listOf("Skip Ad", "Skip ad", "SKIP AD", "Skip Ads", "Skip ads", "SKIP ADS", "Skip", "SKIP")),
        LanguageOption("es", "Spanish (Español)", listOf("Saltar anuncio", "Saltar", "SALTAR", "Omitir anuncio", "Omitir")),
        LanguageOption("hi", "Hindi (हिन्दी)", listOf("विज्ञापन छोड़ें", "छोड़ें")),
        LanguageOption("nl", "Dutch (Nederlands)", listOf("Advertentie overslaan", "Overslaan")),
        LanguageOption("pl", "Polish (Polski)", listOf("Pomiń reklamę", "Pomiń")),
        LanguageOption("fr", "French (Français)", listOf("Ignorer l'annonce", "Ignorer", "Passer", "Ignorer la publicité")),
        LanguageOption("de", "German (Deutsch)", listOf("Anzeige überspringen", "Überspringen", "Werbung überspringen")),
        LanguageOption("ru", "Russian (Русский)", listOf("Пропустить объявление", "Пропустить", "Пропустить рекламу")),
        LanguageOption("ja", "Japanese (日本語)", listOf("広告をスキップ", "スキップ")),
        LanguageOption("ko", "Korean (한국어)", listOf("광고 건너뛰기", "건너뛰기")),
        LanguageOption("ar", "Arabic (العربية)", listOf("تخطي الإعلان", "تخطي")),
        LanguageOption("th", "Thai (ไทย)", listOf("ข้ามโฆษณา", "ข้าม")),
        LanguageOption("vi", "Vietnamese (Tiếng Việt)", listOf("Bỏ qua quảng cáo", "Bỏ qua")),
        LanguageOption("hu", "Hungarian (Magyar)", listOf("Hirdetés kihagyása", "Kihagyás")),
        LanguageOption("ro", "Romanian (Română)", listOf("Omite anunțul", "Omite", "Omiteți anunțul")),
        LanguageOption("sv", "Swedish (Svenska)", listOf("Hoppa över annons", "Hoppa över")),
        LanguageOption("da", "Danish (Dansk)", listOf("Spring annonce over", "Spring over")),
        LanguageOption("fi", "Finnish (Suomi)", listOf("Ohita mainos", "Ohita")),
        LanguageOption("no", "Norwegian (Norsk)", listOf("Hopp over annonse", "Hopp over")),
        LanguageOption("uk", "Ukrainian (Українська)", listOf("Пропустити оголошення", "Пропустити", "Пропустити рекламу")),
        LanguageOption("fil", "Filipino (Tagalog)", listOf("Laktawan ang ad", "Laktawan", "Laktawan ang patalastas")),
        LanguageOption("bn", "Bengali (বাংলা)", listOf("বিজ্ঞাপন এড়িয়ে যান", "এড়িয়ে যান")),
        LanguageOption("ur", "Urdu (اردو)", listOf("اشتہار چھوڑیں", "چھوڑیں")),
        LanguageOption("pt", "Portuguese (Português)", listOf("Pular anúncio", "Pular")),
        LanguageOption("it", "Italian (Italiano)", listOf("Salta annuncio", "Salta", "Ignora"))
    )

    fun getSkipTexts(languageCode: String = "ALL"): Set<String> {
        return if (languageCode == "ALL") {
            languages.flatMap { it.texts }.toSet()
        } else {
            val selected = languages.find { it.code == languageCode }?.texts?.toSet() ?: emptySet()
            // Always include English as fallback/common denominator if needed, or maybe not? 
            // YouTube might default to English in some cases. Safe to include "Skip Ad" & "Skip".
            val english = languages.find { it.code == "en" }?.texts?.toSet() ?: emptySet()
            selected + english
        }
    }
}
