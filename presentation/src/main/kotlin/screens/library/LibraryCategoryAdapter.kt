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

  private var selectedManga = emptySet<Long>()
  private var nowSelectedManga = emptySet<Long>()

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MangaHolder {
    return MangaHolder(parent, glideRequests)
  }

  override fun onBindViewHolder(holder: MangaHolder, position: Int) {
    error("Unused")
  }

  override fun onBindViewHolder(holder: MangaHolder, position: Int, payloads: MutableList<Any>) {
    val item = getItem(position)
    val isSelected by lazy { item.mangaId in nowSelectedManga }

    if (payloads.isEmpty()) {
      holder.bind(item, isSelected, this)
    } else {
      val payload = payloads.first { it is Payload } as Payload
      if (payload.selectionChanged) {
        holder.bindIsSelected(isSelected)
      }
    }
  }

  override fun onViewRecycled(holder: MangaHolder) {
    holder.recycle()
  }

  fun submitManga(mangas: List<LibraryManga>, selectedManga: Set<Long>) {
    this.selectedManga = selectedManga
    submitList(mangas, forceSubmit = true)
  }

  override fun onListUpdated() {
    nowSelectedManga = selectedManga
  }

  override fun getDiffCallback(
    oldList: List<LibraryManga>,
    newList: List<LibraryManga>
  ): DiffUtil.Callback {
    return DiffCallback(oldList, newList, nowSelectedManga, selectedManga)
  }

  fun handleMangaClick(position: Int) {
    val manga = getItemOrNull(position) ?: return
    listener.onMangaClick(manga)
  }

  fun handleMangaLongClick(position: Int) {
    val manga = getItemOrNull(position) ?: return
    listener.onMangaLongClick(manga)
  }

  private class DiffCallback(
    oldList: List<LibraryManga>,
    newList: List<LibraryManga>,
    private val oldSelected: Set<Long>,
    private val newSelected: Set<Long>
  ) : ItemCallback<LibraryManga>(oldList, newList) {

    override fun areItemsTheSame(oldItem: LibraryManga, newItem: LibraryManga): Boolean {
      return oldItem.mangaId == newItem.mangaId
    }

    // TODO improve this
    override fun areContentsTheSame(oldItem: LibraryManga, newItem: LibraryManga): Boolean {
      return !selectionChanged(newItem)
    }

    // TODO improve this
    override fun getChangePayload(oldItem: LibraryManga, newItem: LibraryManga): Any? {
      return Payload(
        selectionChanged = selectionChanged(newItem)
      )
    }

    private fun selectionChanged(manga: LibraryManga): Boolean {
      return manga.mangaId in oldSelected != manga.mangaId in newSelected
    }

  }

  private data class Payload(
    val selectionChanged: Boolean
  )

}
