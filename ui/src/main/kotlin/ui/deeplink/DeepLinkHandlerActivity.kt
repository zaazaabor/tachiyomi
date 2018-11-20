/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.ui.deeplink

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import tachiyomi.core.di.AppScope
import tachiyomi.data.extension.ExtensionManager
import tachiyomi.domain.source.SourceManager
import tachiyomi.source.DeepLink
import tachiyomi.source.DeepLinkSource
import tachiyomi.source.Source
import tachiyomi.ui.MainActivity
import timber.log.Timber
import javax.inject.Inject

class DeepLinkHandlerActivity : Activity() {

  // This is unused but still required to initialize the extensions
  @Suppress("unused")
  @Inject
  internal lateinit var sourceManager: SourceManager

  @Inject
  internal lateinit var extensionManager: ExtensionManager

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
    val extension = extensionManager.installedExtensions.find { it.pkgName == referrer }
    if (extension == null) {
      Timber.w("Extension not found: $referrer")
      finish()
      return
    }

    Timber.w("Found $extension")

    val sourceUrl = intent.data.toString()

    val handlingSources = extension.sources
      .mapNotNull { source ->
        if (source is DeepLinkSource) {
          source.handlesLink(sourceUrl)?.let {
            SourceHandler(source, it)
          }
        } else {
          null
        }
      }

    if (handlingSources.isEmpty()) {
      Timber.w("No source could handle link $sourceUrl")
      finish()
      return
    }

    Timber.w("Sources handling: $handlingSources")

    val receiver = handlingSources.first() // TODO intermediary UI to select a receiver?

    when (receiver.link) {
      is DeepLink.Manga -> {
        val intent = Intent(this, MainActivity::class.java).apply {
          action = MainActivity.SHORTCUT_DEEPLINK_MANGA
          putExtra(MangaDeepLinkController.MANGA_KEY, receiver.link.key)
          putExtra(MangaDeepLinkController.SOURCE_KEY, receiver.source.id)
          addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }
        startActivity(intent)
      }
      is DeepLink.Chapter -> {
        val intent = Intent(this, MainActivity::class.java).apply {
          action = MainActivity.SHORTCUT_DEEPLINK_CHAPTER
          putExtra(ChapterDeepLinkController.CHAPTER_KEY, receiver.link.key)
          putExtra(ChapterDeepLinkController.SOURCE_KEY, receiver.source.id)
          addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }
        startActivity(intent)
      }
    }

    finish()
  }

  private data class SourceHandler(val source: Source, val link: DeepLink)

}
