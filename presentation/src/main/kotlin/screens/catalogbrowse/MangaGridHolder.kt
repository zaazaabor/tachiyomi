/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.ui.screens.catalogbrowse

import android.view.View
import com.bumptech.glide.load.engine.DiskCacheStrategy
import kotlinx.android.synthetic.main.manga_grid_item.*
import tachiyomi.domain.manga.model.Manga
import tachiyomi.ui.glide.GlideRequests
import tachiyomi.ui.widget.StateImageViewTarget

/**
 * Holder to use when displaying a [Manga] from a [CatalogBrowseAdapter].
 */
class MangaGridHolder(
  private val view: View,
  private val adapter: CatalogBrowseAdapter,
  private val glideRequests: GlideRequests
) : MangaHolder(view) {

  init {
    view.setOnClickListener {
      adapter.handleClick(adapterPosition)
    }
    view.setOnLongClickListener {
      adapter.handleLongClick(adapterPosition)
      true
    }
  }

  /**
   * Binds the given [manga] with this holder.
   */
  override fun bind(manga: Manga) {
    // Set manga title
    catalog_title.text = manga.title

    // Set alpha of thumbnail.
    bindFavorite(manga)
    bindImage(manga)
  }

  /**
   * Binds only the cover of the given [manga] with this holder.
   */
  override fun bindImage(manga: Manga) {
    if (!manga.cover.isEmpty()) {
      glideRequests.load(manga.cover)
        .diskCacheStrategy(DiskCacheStrategy.DATA)
        .centerCrop()
        .placeholder(android.R.color.transparent)
        .into(StateImageViewTarget(thumbnail, progress))
    }
  }

  override fun bindFavorite(manga: Manga) {
    // Set alpha of thumbnail.
    thumbnail.alpha = if (manga.favorite) 0.3f else 1.0f
  }

  override fun recycle() {
    glideRequests.clear(thumbnail)
  }

}
