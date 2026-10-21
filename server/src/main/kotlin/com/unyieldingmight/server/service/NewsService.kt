package com.unyieldingmight.server.service

import com.unyieldingmight.server.ApiException
import com.unyieldingmight.shared.model.NewsArticleDto
import com.unyieldingmight.shared.model.NewsTopic
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.http.isSuccess
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
    private val cacheTtl: Duration = Duration.ofMinutes(30),
) {
    private val log = LoggerFactory.getLogger(NewsService::class.java)
    private val cache = mutableMapOf<NewsTopic, Pair<Instant, List<NewsArticleDto>>>()
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

    suspend fun headlines(topic: NewsTopic): List<NewsArticleDto> {
        val key = apiKey ?: throw ApiException.unavailable("News isn't configured on this server")
        mutex.withLock {
            cache[topic]?.let { (fetchedAt, articles) ->
                if (fetchedAt.plus(cacheTtl).isAfter(Instant.now())) return articles
            }
        }
        val articles = try {
            fetch(topic, key)
        } catch (e: Exception) {
            log.warn("NewsAPI request failed: {}", e.message)
            // Serve stale headlines rather than nothing.
            mutex.withLock { cache[topic]?.second } ?: throw ApiException.unavailable("News is unavailable right now. Please try again later.")
        }
        mutex.withLock { cache[topic] = Instant.now() to articles }
        return articles
    }

    private suspend fun fetch(topic: NewsTopic, key: String): List<NewsArticleDto> {
        val response = when (topic) {
            NewsTopic.HEALTH -> http.get("https://newsapi.org/v2/top-headlines") {
                parameter("category", "health")
                parameter("language", "en")
                parameter("pageSize", 40)
                header("X-Api-Key", key)
            }
            else -> http.get("https://newsapi.org/v2/everything") {
                parameter("q", query(topic))
                parameter("language", "en")
                parameter("sortBy", "publishedAt")
                parameter("searchIn", "title")
                parameter("pageSize", 40)
                header("X-Api-Key", key)
            }
        }
        val body = response.body<NewsApiResponse>()
        if (!response.status.isSuccess() || body.status != "ok") error(body.message ?: "HTTP ${response.status}")
        return body.articles.mapNotNull { it.toDto() }.distinctBy { it.title }
    }

    private fun query(topic: NewsTopic) = when (topic) {
        NewsTopic.FITNESS -> "fitness OR workout OR exercise"
        NewsTopic.NUTRITION -> "nutrition OR \"healthy eating\" OR protein OR diet"
        NewsTopic.GYM -> "\"strength training\" OR weightlifting OR powerlifting OR bodybuilding"
        NewsTopic.HEALTH -> "health"
    }

    private fun Article.toDto(): NewsArticleDto? {
        val title = title?.takeIf { it.isNotBlank() && it != "[Removed]" } ?: return null
        val url = url?.takeIf { it.startsWith("http") } ?: return null
        return NewsArticleDto(
            title = title,
            description = description?.takeIf { it.isNotBlank() && it != "[Removed]" },
            url = url,
            imageUrl = urlToImage?.takeIf { it.startsWith("https://") },
            source = source?.name ?: "",
            publishedAt = publishedAt?.let { runCatching { Instant.parse(it).toEpochMilli() }.getOrNull() } ?: 0L,
        )
    }
}
