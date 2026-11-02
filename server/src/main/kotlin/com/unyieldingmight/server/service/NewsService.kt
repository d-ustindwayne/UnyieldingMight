package com.unyieldingmight.server.service

import com.unyieldingmight.server.ApiException
import com.unyieldingmight.shared.model.NewsArticleDto
import com.unyieldingmight.shared.model.NewsTopic
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.get
import io.ktor.client.request.url
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.http.isSuccess
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.Serializable
import org.slf4j.LoggerFactory
import java.time.Duration
import java.time.Instant

/**
 * Health & fitness headlines from NewsAPI. The API key stays on the server and results are cached,
 * which keeps the app fast and well inside NewsAPI's free-tier request limit.
 */
class NewsService(
    private val http: HttpClient,
    private val apiKey: String?,
    private val cacheTtl: Duration = Duration.ofHours(1),
) {
    private val log = LoggerFactory.getLogger(NewsService::class.java)

    private val cache = mutableMapOf<String, Pair<Instant, List<NewsArticleDto>>>()
    private val mutex = Mutex()

    val enabled: Boolean get() = apiKey != null

    @Serializable
    private data class NewsApiResponse(val status: String, val articles: List<Article> = emptyList(), val message: String? = null)

    @Serializable
    private data class Article(
        val source: Source? = null,
        val title: String? = null,
        val description: String? = null,
        val url: String? = null,
        val urlToImage: String? = null,
        val publishedAt: String? = null,
    )

    @Serializable
    private data class Source(val name: String? = null)

    /**
     * Stories from the reader's [country] (an ISO code) first, then global ones to fill the feed.
     * Local fitness coverage is thin in most countries, so local results alone would often leave the tab nearly empty.
     */
    suspend fun headlines(topic: NewsTopic, country: String? = null): List<NewsArticleDto> = coroutineScope {
        val key = apiKey ?: throw ApiException.unavailable("News isn't configured on this server")
        val local = country?.let { code ->
            async {
                runCatching { cached("$code:$topic") { fetchLocal(topic, NewsLocales.forCountry(code), key) } }
                    .onFailure { log.warn("Local news for {} failed: {}", code, it.message) }
                    .getOrDefault(emptyList())
            }
        }
        val global = async {
            try {
                cached("GLOBAL:$topic") { fetchGlobal(topic, key) }
            } catch (e: Exception) {
                throw ApiException.unavailable("News is unavailable right now. Please try again later.")
            }
        }
        val localArticles = local?.await().orEmpty()
        val seen = localArticles.map { it.title }.toMutableSet()
        localArticles + global.await().filter { seen.add(it.title) }
    }

    /** Cached results for [key], refreshed after [cacheTtl]. Serves stale results rather than nothing when a refresh fails. */
    private suspend fun cached(key: String, fetch: suspend () -> List<NewsArticleDto>): List<NewsArticleDto> {
        val previous = mutex.withLock { cache[key] }
        if (previous != null && previous.first.plus(cacheTtl).isAfter(Instant.now())) return previous.second
        val articles = try {
            fetch()
        } catch (e: Exception) {
            log.warn("NewsAPI request failed: {}", e.message)
            previous?.second ?: throw e
        }
        mutex.withLock { cache[key] = Instant.now() to articles }
        return articles
    }

    /**
     * Up to three searches run together: headlines naming the country, the country's language (when NewsAPI
     * indexes it) and the country's main outlets (when known). Any one of them failing is fine.
     */
    private suspend fun fetchLocal(topic: NewsTopic, country: NewsLocales.CountryNews, key: String): List<NewsArticleDto> = coroutineScope {
        val searches = buildList<suspend () -> List<NewsArticleDto>> {
            add {
                everything(key, country.code) {
                    parameter("q", NewsLocales.countryQuery(country, topic))
                }
            }
            val language = country.language
            val native = language?.let { NewsLocales.nativeQuery(it, topic) }
            if (language != null && native != null) add {
                everything(key, country.code) {
                    parameter("q", native)
                    parameter("language", language)
                }
            }
            country.domains?.let { domains ->
                add {
                    everything(key, country.code) {
                        parameter("q", NewsLocales.outletQuery(topic))
                        parameter("domains", domains)
                    }
                }
            }
        }
        val results = searches.map { async { runCatching { it() } } }.awaitAll()
        if (results.all { it.isFailure }) throw results.first().exceptionOrNull()!!
        results.flatMap { it.getOrDefault(emptyList()) }
            .distinctBy { it.title }
            .sortedByDescending { it.publishedAt }
            .take(25)
    }

    private suspend fun everything(key: String, country: String, block: HttpRequestBuilder.() -> Unit) = request(key, country) {
        url("https://newsapi.org/v2/everything")
        block()
        parameter("searchIn", "title")
        parameter("sortBy", "publishedAt")
        parameter("pageSize", 15)
    }

    private suspend fun fetchGlobal(topic: NewsTopic, key: String): List<NewsArticleDto> = request(key, country = null) {
        when (topic) {
            NewsTopic.HEALTH -> {
                url("https://newsapi.org/v2/top-headlines")
                parameter("category", "health")
            }
            else -> {
                url("https://newsapi.org/v2/everything")
                parameter("q", query(topic))
                parameter("sortBy", "publishedAt")
                parameter("searchIn", "title")
            }
        }
        parameter("language", "en")
        parameter("pageSize", 40)
    }

    private suspend fun request(key: String, country: String?, block: HttpRequestBuilder.() -> Unit): List<NewsArticleDto> {
        val response = http.get {
            block()
            header("X-Api-Key", key)
        }
        val body = response.body<NewsApiResponse>()
        if (!response.status.isSuccess() || body.status != "ok") error(body.message ?: "HTTP ${response.status}")
        return body.articles.mapNotNull { it.toDto(country) }.distinctBy { it.title }
    }

    private fun query(topic: NewsTopic) = when (topic) {
        NewsTopic.FITNESS -> "fitness OR workout OR exercise"
        NewsTopic.NUTRITION -> "nutrition OR \"healthy eating\" OR protein OR diet"
        NewsTopic.GYM -> "\"strength training\" OR weightlifting OR powerlifting OR bodybuilding"
        NewsTopic.HEALTH -> "health"
    }

    private fun Article.toDto(country: String?): NewsArticleDto? {
        val title = title?.takeIf { it.isNotBlank() && it != "[Removed]" } ?: return null
        val url = url?.takeIf { it.startsWith("http") } ?: return null
        return NewsArticleDto(
            title = title,
            description = description?.takeIf { it.isNotBlank() && it != "[Removed]" },
            url = url,
            imageUrl = urlToImage?.takeIf { it.startsWith("https://") },
            source = source?.name ?: "",
            publishedAt = publishedAt?.let { runCatching { Instant.parse(it).toEpochMilli() }.getOrNull() } ?: 0L,
            country = country,
        )
    }
}
