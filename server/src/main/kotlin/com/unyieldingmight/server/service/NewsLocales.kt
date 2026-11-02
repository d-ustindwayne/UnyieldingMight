package com.unyieldingmight.server.service

import com.unyieldingmight.shared.domain.Countries
import com.unyieldingmight.shared.model.NewsTopic

/**
 * How to find local stories for a reader's country with NewsAPI. Any ISO country works: its English name
 * (plus a few well-known aliases) is searched in headlines. Countries whose language NewsAPI indexes also get
 * a native-language search, and a few have a list of their main outlets.
 *
 * NewsAPI doesn't index Japanese, Chinese or Korean sources, so those countries rely on the English search.
 */
object NewsLocales {

    data class CountryNews(val code: String, val names: List<String>, val language: String?, val domains: String?)

    fun forCountry(code: String) = CountryNews(
        code = code,
        names = (listOf(Countries.nameOf(code).substringBefore(" (").trim()) + ALIASES[code].orEmpty()).distinct(),
        language = LANGUAGE[code],
        domains = DOMAINS[code],
    )

    /** Headlines mentioning the country and the topic, e.g. `("New Zealand" OR Kiwi) AND (fitness OR gym)`. */
    fun countryQuery(country: CountryNews, topic: NewsTopic): String =
        "(${country.names.joinToString(" OR ") { if (' ' in it) "\"$it\"" else it }}) AND (${topicTerms(topic)})"

    /** Narrower than the global wording: "exercise" mostly finds military exercises when paired with a country. */
    private fun topicTerms(topic: NewsTopic) = when (topic) {
        NewsTopic.FITNESS -> "fitness OR workout OR gym OR yoga OR marathon"
        NewsTopic.NUTRITION -> "nutrition OR diet OR protein OR \"healthy eating\""
        NewsTopic.GYM -> "gym OR \"strength training\" OR weightlifting OR powerlifting OR bodybuilding"
        NewsTopic.HEALTH -> "health OR wellbeing OR \"mental health\""
    }

    /** Broad wording for a country's own outlets, which publish few fitness stories. */
    fun outletQuery(topic: NewsTopic) = when (topic) {
        NewsTopic.FITNESS -> "fitness OR workout OR exercise OR gym"
        NewsTopic.NUTRITION -> "nutrition OR diet OR \"healthy eating\" OR protein OR food"
        NewsTopic.GYM -> "gym OR \"strength training\" OR weightlifting OR powerlifting OR bodybuilding"
        NewsTopic.HEALTH -> "health OR wellbeing OR \"mental health\" OR hospital"
    }

    fun nativeQuery(language: String, topic: NewsTopic): String? = NATIVE_TERMS[language]?.get(topic)

    private val ALIASES = mapOf(
        "NZ" to listOf("Kiwi", "Kiwis", "Auckland", "Wellington", "Christchurch"),
        "AU" to listOf("Australian", "Sydney", "Melbourne", "Brisbane", "Perth"),
        "US" to listOf("USA", "American", "Americans"),
        "GB" to listOf("UK", "British", "Britain", "England", "Scotland", "Wales"),
        "CA" to listOf("Canadian", "Toronto", "Vancouver"),
        "IE" to listOf("Irish", "Dublin"),
        "JP" to listOf("Japanese", "Tokyo"),
        "KR" to listOf("Korean", "Seoul"),
        "CN" to listOf("Chinese", "Beijing", "Shanghai"),
        "IN" to listOf("Indian", "Delhi", "Mumbai"),
        "PH" to listOf("Filipino", "Filipinos", "Manila"),
        "SG" to listOf("Singaporean"),
        "DE" to listOf("German", "Berlin"),
        "FR" to listOf("French", "Paris"),
        "ES" to listOf("Spanish", "Madrid"),
        "IT" to listOf("Italian", "Rome", "Milan"),
        "BR" to listOf("Brazilian"),
        "MX" to listOf("Mexican"),
    )

    /** Main outlets for a few countries, searched with [outletQuery]. */
    private val DOMAINS = mapOf(
        "NZ" to "nzherald.co.nz,stuff.co.nz,rnz.co.nz,1news.co.nz,newsroom.co.nz,thespinoff.co.nz,odt.co.nz",
        "AU" to "abc.net.au,smh.com.au,theage.com.au,news.com.au,9news.com.au,sbs.com.au",
    )

    /** The country's main language, for the languages NewsAPI can search. */
    private val LANGUAGE: Map<String, String> = buildMap {
        fun lang(language: String, vararg codes: String) = codes.forEach { put(it, language) }
        lang("de", "DE", "AT", "CH", "LI")
        lang("es", "ES", "MX", "AR", "CO", "CL", "PE", "VE", "EC", "GT", "CU", "BO", "DO", "HN", "PY", "SV", "NI", "CR", "PA", "UY")
        lang("fr", "FR", "BE", "LU", "MC", "SN", "CI")
        lang("it", "IT", "SM")
        lang("pt", "PT", "BR", "AO", "MZ")
        lang("nl", "NL")
        lang("no", "NO")
        lang("sv", "SE")
        lang("ru", "RU", "BY", "KZ")
        lang("ar", "SA", "AE", "EG", "MA", "DZ", "TN", "JO", "LB", "KW", "QA", "BH", "OM", "IQ")
        lang("he", "IL")
    }

    private val NATIVE_TERMS: Map<String, Map<NewsTopic, String>> = mapOf(
        "de" to topics(
            fitness = "Fitness OR Workout OR Krafttraining OR Fitnessstudio",
            nutrition = "Ernährung OR Diät OR Protein",
            gym = "Fitnessstudio OR Krafttraining OR Gewichtheben OR Bodybuilding",
            health = "Gesundheit",
        ),
        "es" to topics(
            fitness = "fitness OR gimnasio OR entrenamiento OR ejercicio",
            nutrition = "nutrición OR dieta OR alimentación OR proteína",
            gym = "gimnasio OR musculación OR halterofilia OR culturismo",
            health = "salud",
        ),
        "fr" to topics(
            fitness = "fitness OR musculation OR \"salle de sport\" OR entraînement",
            nutrition = "nutrition OR alimentation OR régime OR protéines",
            gym = "musculation OR \"salle de sport\" OR haltérophilie OR bodybuilding",
            health = "santé",
        ),
        "it" to topics(
            fitness = "fitness OR palestra OR allenamento",
            nutrition = "nutrizione OR dieta OR alimentazione OR proteine",
            gym = "palestra OR \"sollevamento pesi\" OR bodybuilding OR powerlifting",
            health = "salute",
        ),
        "pt" to topics(
            fitness = "fitness OR academia OR ginásio OR treino",
            nutrition = "nutrição OR dieta OR alimentação OR proteína",
            gym = "academia OR ginásio OR musculação OR fisiculturismo",
            health = "saúde",
        ),
        "nl" to topics(
            fitness = "fitness OR sportschool OR workout",
            nutrition = "voeding OR dieet OR eiwit",
            gym = "sportschool OR krachttraining OR gewichtheffen OR bodybuilding",
            health = "gezondheid",
        ),
        "no" to topics(
            fitness = "trening OR treningssenter OR fitness",
            nutrition = "ernæring OR kosthold OR protein",
            gym = "treningssenter OR styrketrening OR vektløfting",
            health = "helse",
        ),
        "sv" to topics(
            fitness = "träning OR gym OR fitness",
            nutrition = "kost OR näring OR protein",
            gym = "gym OR styrketräning OR tyngdlyftning",
            health = "hälsa",
        ),
        "ru" to topics(
            fitness = "фитнес OR тренировка OR спортзал",
            nutrition = "питание OR диета OR белок",
            gym = "спортзал OR \"силовые тренировки\" OR бодибилдинг",
            health = "здоровье",
        ),
        "ar" to topics(
            fitness = "لياقة OR تمارين OR \"صالة رياضية\"",
            nutrition = "تغذية OR حمية OR بروتين",
            gym = "\"صالة رياضية\" OR \"رفع الأثقال\" OR \"كمال الأجسام\"",
            health = "صحة",
        ),
        "he" to topics(
            fitness = "כושר OR אימון",
            nutrition = "תזונה OR דיאטה OR חלבון",
            gym = "\"חדר כושר\" OR \"הרמת משקולות\"",
            health = "בריאות",
        ),
    )

    private fun topics(fitness: String, nutrition: String, gym: String, health: String) = mapOf(
        NewsTopic.FITNESS to fitness,
        NewsTopic.NUTRITION to nutrition,
        NewsTopic.GYM to gym,
        NewsTopic.HEALTH to health,
    )
}
