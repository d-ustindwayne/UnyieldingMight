package com.unyieldingmight.server

import com.unyieldingmight.server.db.DevSeeder
import com.unyieldingmight.server.service.NewsService
import com.unyieldingmight.shared.model.GymClassDto
import com.unyieldingmight.shared.model.NewsTopic
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class NewsAndImagesTest {

    private fun article(title: String) =
        """{"source":{"name":"Src"},"title":"$title","url":"https://news.example/${title.hashCode()}","publishedAt":"2026-10-20T10:00:00Z"}"""

    private fun newsService(handler: (io.ktor.http.Parameters) -> List<String>?): NewsService {
        val engine = MockEngine { request ->
            val titles = handler(request.url.parameters)
            if (titles == null) respond("boom", HttpStatusCode.InternalServerError)
            else respond(
                """{"status":"ok","articles":[${titles.joinToString(",") { article(it) }}]}""",
                HttpStatusCode.OK, headersOf(HttpHeaders.ContentType, "application/json"),
            )
        }
        return NewsService(HttpClient(engine) { install(ContentNegotiation) { json(Services.json) } }, "key")
    }

    private val io.ktor.http.Parameters.isLocal get() = get("domains") != null || get("language") == "de" || get("q").orEmpty().contains("Germany") || get("q").orEmpty().contains("\"New Zealand\"")

    @Test
    fun `local stories come first, are labelled with the country, and duplicates are dropped`() = runTest {
        val news = newsService { if (it.isLocal) listOf("Auckland gym opens", "Shared story") else listOf("Shared story", "Global fitness trend") }
            .headlines(NewsTopic.FITNESS, "NZ")

        assertEquals(listOf("Auckland gym opens", "Shared story", "Global fitness trend"), news.map { it.title })
        assertEquals(listOf("NZ", "NZ", null), news.map { it.country })
    }

    @Test
    fun `every country gets a local search, in its own language when NewsAPI supports it`() = runTest {
        val searches = mutableListOf<io.ktor.http.Parameters>()
        newsService { searches += it; emptyList() }.headlines(NewsTopic.GYM, "DE")

        assertTrue(searches.any { it["q"].orEmpty().startsWith("(Germany OR German OR Berlin) AND (") })
        assertTrue(searches.any { it["language"] == "de" && it["q"].orEmpty().contains("Fitnessstudio") })
        assertTrue(searches.none { it["domains"] != null })
    }

    @Test
    fun `without a country only global news is fetched`() = runTest {
        val searches = mutableListOf<io.ktor.http.Parameters>()
        val news = newsService { searches += it; listOf("Global only") }.headlines(NewsTopic.HEALTH, null)

        assertEquals(1, searches.size)
        assertEquals(listOf<String?>(null), news.map { it.country })
    }

    @Test
    fun `global news still loads when the local requests fail`() = runTest {
        val news = newsService { if (it.isLocal) null else listOf("Global only") }.headlines(NewsTopic.GYM, "NZ")
        assertEquals(listOf("Global only"), news.map { it.title })
    }

    @Test
    fun `slugs match photo file names`() {
        assertEquals("spin-sweat", DevSeeder.slug("Spin & Sweat"))
        assertEquals("whey-protein-1kg", DevSeeder.slug("Whey Protein 1kg"))
        assertEquals("flexibility-mobility", DevSeeder.slug("Flexibility & Mobility"))
    }

    @Test
    fun `classes keep the photo an admin sets`() = apiTest { env, client ->
        val created = env.services.classes.create(
            com.unyieldingmight.shared.model.ClassUpsertRequest(
                "Photo Class", "desc", com.unyieldingmight.shared.model.ClassCategory.CARDIO, null,
                System.currentTimeMillis() + 86_400_000, System.currentTimeMillis() + 90_000_000, 10, 300, 0,
                imageUrl = "/images/classes/photo-class.jpg",
            )
        )
        val fetched = client.get("/api/classes/${created.id}").body<GymClassDto>()
        assertEquals("/images/classes/photo-class.jpg", fetched.imageUrl)
    }

    @Test
    fun `missing hosted images return 404 rather than an error page`() = apiTest { _, client ->
        assertEquals(HttpStatusCode.NotFound, client.get("/images/classes/does-not-exist.jpg").status)
        assertTrue(client.get("/api/health").status == HttpStatusCode.OK)
    }
}
