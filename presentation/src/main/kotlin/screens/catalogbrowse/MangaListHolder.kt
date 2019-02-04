/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.ui.screens.catalogbrowse

import android.view.View
import kotlinx.android.synthetic.main.manga_list_item.*
import tachiyomi.domain.manga.model.Manga

/**
 * Holder to use when displaying a [Manga] from a [CatalogBrowseAdapter].
 */
class MangaListHolder(
  private val view: View,
  private val adapter: CatalogBrowseAdapter
) : MangaHolder(view) {

  init {
    view.setOnClickListener {
      adapter.handleClick(adapterPosition)
    }
  }

  /**
   * Binds the given [manga] with this holder.
   */
  override fun bind(manga: Manga) {
    // Set manga title
    title.text = manga.title

    // Set alpha of thumbnail.
    thumbnail.alpha = if (manga.favorite) 0.3f else 1.0f

    bindImage(manga)
  }

  /**
   * Binds only the cover of the given [manga] with this holder.
   */
  override fun bindImage(manga: Manga) {
    // TODO
  }

  override fun recycle() {
    // TODO
  }

//  override fun setImage(manga: Manga) {
//    GlideApp.with(view.context).clear(thumbnail)
//    if (!manga.thumbnail_url.isNullOrEmpty()) {
//      GlideApp.with(view.context)
//        .load(manga)
//        .diskCacheStrategy(DiskCacheStrategy.DATA)
//        .centerCrop()
//        .skipMemoryCache(true)
//        .placeholder(android.R.color.transparent)
//        .into(StateImageViewTarget(thumbnail, progress))
//    }
//  }
}
