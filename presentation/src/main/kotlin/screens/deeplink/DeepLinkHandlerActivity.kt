/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.ui.screens.deeplink

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import tachiyomi.core.di.AppScope
import tachiyomi.domain.catalog.repository.CatalogRepository
import tachiyomi.domain.source.SourceManager
import tachiyomi.source.DeepLink
import tachiyomi.source.DeepLinkSource
import tachiyomi.source.Source
import tachiyomi.ui.screens.MainActivity
import timber.log.Timber
import javax.inject.Inject

class DeepLinkHandlerActivity : Activity() {

  // This is unused but still required to initialize the extensions
  @Suppress("unused")
  @Inject
  internal lateinit var sourceManager: SourceManager

  @Inject
  internal lateinit var catalogRepository: CatalogRepository

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    // Get caller extension
    val referrer = intent.getStringExtra(Intent.EXTRA_REFERRER)
    if (referrer == null || referrer.isEmpty()) {
      Timber.w("Received an intent from an extension without a receiver")
      finish()
      return
    }

    // Inject extensions manager
    AppScope.inject(this)

    // Find caller extension
    val catalog = catalogRepository.installedCatalogs.find { it.pkgName == referrer }
    if (catalog == null) {
      Timber.w("Extension not found: $referrer")
      finish()
      return
    }

    val urlToHandle = intent?.data?.toString()
    if (urlToHandle == null) {
      Timber.w("Url to handle not found in intent")
      finish()
      return
    }

    val source = catalog.source
    val sourceHandler = if (source is DeepLinkSource) {
      source.handleLink(urlToHandle)?.let { SourceHandler(source, it) }
    } else {
      null
    }

    if (sourceHandler == null) {
      Timber.w("No source could handle link $urlToHandle")
      finish()
      return
    }

    when (sourceHandler.link) {
      is DeepLink.Manga -> {
        val intent = Intent(this, MainActivity::class.java).apply {
          action = MainActivity.SHORTCUT_DEEPLINK_MANGA
          putExtra(MangaDeepLinkController.MANGA_KEY, sourceHandler.link.key)
          putExtra(MangaDeepLinkController.SOURCE_KEY, sourceHandler.source.id)
          addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }
        startActivity(intent)
      }
      is DeepLink.Chapter -> {
        val intent = Intent(this, MainActivity::class.java).apply {
          action = MainActivity.SHORTCUT_DEEPLINK_CHAPTER
          putExtra(ChapterDeepLinkController.CHAPTER_KEY, sourceHandler.link.key)
          putExtra(ChapterDeepLinkController.SOURCE_KEY, sourceHandler.source.id)
          addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }
        startActivity(intent)
      }
    }

    finish()
  }

  private data class SourceHandler(val source: Source, val link: DeepLink)

}
