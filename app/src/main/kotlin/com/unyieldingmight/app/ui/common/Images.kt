package com.unyieldingmight.app.ui.common

import com.unyieldingmight.app.BuildConfig

/** Photos are either absolute https URLs or paths hosted by the API (e.g. "/images/classes/hiit-blast.jpg"). */
fun resolveImageUrl(url: String?): String? = when {
    url.isNullOrBlank() -> null
    url.startsWith("/") -> BuildConfig.API_BASE_URL.trimEnd('/') + url
    else -> url
}
