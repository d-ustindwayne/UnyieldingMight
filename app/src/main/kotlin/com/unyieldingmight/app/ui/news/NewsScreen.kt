package com.unyieldingmight.app.ui.news

import android.content.Context
import android.net.Uri
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Newspaper
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.unyieldingmight.app.AppContainer
import com.unyieldingmight.app.ui.common.EmptyState
import com.unyieldingmight.app.ui.common.Format
import com.unyieldingmight.app.ui.common.LoadContent
import com.unyieldingmight.app.ui.common.LoadViewModel
import com.unyieldingmight.app.ui.common.containerViewModel
import com.unyieldingmight.app.ui.shell.ScreenScaffold
import com.unyieldingmight.app.ui.theme.Overline
import com.unyieldingmight.shared.model.NewsArticleDto
import com.unyieldingmight.shared.model.NewsTopic
import kotlinx.coroutines.flow.MutableStateFlow

class NewsViewModel(container: AppContainer) : LoadViewModel<List<NewsArticleDto>>(container) {
    val topic = MutableStateFlow(NewsTopic.FITNESS)

    override suspend fun load() = container.api.news(topic.value)

    fun select(newTopic: NewsTopic) {
        if (newTopic == topic.value) return
        topic.value = newTopic
        refresh()
    }

    init { refresh() }
}

@Composable
fun NewsScreen() {
    val vm = containerViewModel { NewsViewModel(it) }
    val state by vm.state.collectAsState()
    val topic by vm.topic.collectAsState()
    val context = LocalContext.current

    ScreenScaffold("News", topLevel = true) { padding ->
        Column(Modifier.padding(padding)) {
            LazyRow(contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(NewsTopic.entries) { t -> FilterChip(t == topic, onClick = { vm.select(t) }, label = { Text(t.label) }) }
            }
            LoadContent(state, vm::refresh) { articles ->
                LazyVerticalStaggeredGrid(
                    columns = StaggeredGridCells.Adaptive(300.dp),
                    contentPadding = PaddingValues(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalItemSpacing = 12.dp,
                    modifier = Modifier.fillMaxSize(),
                ) {
                    if (articles.isEmpty()) {
                        item(span = StaggeredGridItemSpan.FullLine) {
                            EmptyState(Icons.Rounded.Newspaper, "No headlines", "Nothing new on this topic right now. Try another.")
                        }
                    }
                    items(articles, key = { it.url }) { article -> ArticleCard(article) { openArticle(context, article.url) } }
                    item(span = StaggeredGridItemSpan.FullLine) {
                        Text(
                            "Headlines provided by NewsAPI.org",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(8.dp),
                        )
                    }
                }
            }
        }
    }
}

private fun openArticle(context: Context, url: String) {
    CustomTabsIntent.Builder().setShowTitle(true).build().launchUrl(context, Uri.parse(url))
}

@Composable
private fun ArticleCard(article: NewsArticleDto, onClick: () -> Unit) {
    Surface(onClick = onClick, shape = MaterialTheme.shapes.large, color = MaterialTheme.colorScheme.surfaceContainer) {
        Column {
            article.imageUrl?.let {
                Box(Modifier.fillMaxWidth().aspectRatio(16f / 9f)) {
                    AsyncImage(model = it, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                }
            }
            Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    listOf(article.source, Format.ago(article.publishedAt)).filter { it.isNotBlank() }.joinToString(" · ").uppercase(),
                    style = Overline,
                    color = MaterialTheme.colorScheme.primary,
                    maxLines = 1,
                )
                Text(article.title, style = MaterialTheme.typography.titleMedium, maxLines = 3, overflow = TextOverflow.Ellipsis)
                article.description?.let {
                    Text(it, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 3, overflow = TextOverflow.Ellipsis)
                }
            }
        }
    }
}
