/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.ui.screens.library

import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import tachiyomi.domain.library.model.LibraryManga
import tachiyomi.ui.adapter.BaseListAdapter
import tachiyomi.ui.adapter.ItemCallback
import tachiyomi.ui.glide.GlideRequests

class LibraryCategoryAdapter(
  private val glideRequests: GlideRequests,
  private val listener: LibraryAdapter.Listener
) : BaseListAdapter<LibraryManga, MangaHolder>() {

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MangaHolder {
    return MangaHolder(parent, glideRequests)
  }

  override fun onBindViewHolder(holder: MangaHolder, position: Int) {
    holder.bind(getItem(position), this)
  }

  override fun onViewRecycled(holder: MangaHolder) {
    holder.recycle()
  }

  // TODO
  override fun getDiffCallback(
    oldList: List<LibraryManga>,
    newList: List<LibraryManga>
  ): DiffUtil.Callback {
    return object : ItemCallback<LibraryManga>(oldList, newList) {

      override fun areItemsTheSame(oldItem: LibraryManga, newItem: LibraryManga): Boolean {
        return oldItem == newItem
      }

      override fun areContentsTheSame(oldItem: LibraryManga, newItem: LibraryManga): Boolean {
        return true
      }

    }
  }

  fun handleMangaClick(position: Int) {
    val manga = getItemOrNull(position) ?: return
    listener.onMangaClick(manga)
  }

}
