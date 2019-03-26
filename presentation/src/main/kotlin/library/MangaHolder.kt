/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.ui.library

import android.view.ViewGroup
import kotlinx.android.synthetic.main.manga_grid_item.*
import tachiyomi.domain.library.model.LibraryManga
import tachiyomi.ui.R
import tachiyomi.ui.adapter.BaseViewHolder
import tachiyomi.ui.glide.GlideRequests
import tachiyomi.ui.glide.MangaCover
import tachiyomi.ui.util.inflate

class MangaHolder(
  parent: ViewGroup,
  private val glideRequests: GlideRequests
) : BaseViewHolder(parent.inflate(R.layout.manga_grid_item)) {

  private var currentAdapter: LibraryCategoryAdapter? = null

  init {
    itemView.setOnClickListener { currentAdapter?.handleMangaClick(adapterPosition) }
    itemView.setOnLongClickListener {
      currentAdapter?.handleMangaLongClick(adapterPosition)
      true
    }
  }

  fun bind(manga: LibraryManga, isSelected: Boolean, adapter: LibraryCategoryAdapter) {
    currentAdapter = adapter
    catalog_title.text = manga.title

    glideRequests.load(MangaCover.from(manga))
      .centerCrop()
      .into(thumbnail)

    bindIsSelected(isSelected)
  }

  fun bindIsSelected(isSelected: Boolean) {
    itemView.isActivated = isSelected
  }

  override fun recycle() {
    currentAdapter = null
    glideRequests.clear(thumbnail)
  }

}
