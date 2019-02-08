/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.ui.screens.catalogbrowse

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import tachiyomi.domain.manga.model.Manga
import tachiyomi.ui.R
import tachiyomi.ui.adapter.BaseListAdapter
import tachiyomi.ui.glide.GlideRequests
import tachiyomi.ui.widget.AutofitRecyclerView

/**
 * Adapter for the list of manga from the catalogue. It can receive a [listener] as parameter to
 * handle clicks.
 */
class CatalogBrowseAdapter(
  private val listener: Listener? = null,
  private val glideRequests: GlideRequests
) : BaseListAdapter<Any, RecyclerView.ViewHolder>() {

  /**
   * Submits an update on the items. It receives a list of [mangas], whether [isLoading] more
   * results and [endReached].
   */
  fun submitList(mangas: List<Manga>, isLoading: Boolean, endReached: Boolean) {
    val items = mutableListOf<Any>()
    items.addAll(mangas)

    if (isLoading && mangas.isNotEmpty()) {
      items += LoadingMore
    } else if (endReached) {
      items += EndReached
    }

    submitList(items)
  }

  /**
   * Returns the view type for the item on this [position].
   */
  override fun getItemViewType(position: Int): Int {
    return when (getItem(position)) {
      is Manga -> MANGA_VIEWTYPE
      else -> FOOTER_VIEWTYPE
    }
  }

  /**
   * Creates a new view holder for the given [viewType].
   */
  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
    val inflater = LayoutInflater.from(parent.context)
    return when (viewType) {
      MANGA_VIEWTYPE -> {
        val isGridMode = (parent as? AutofitRecyclerView)?.layoutManager is GridLayoutManager
        if (isGridMode) {
          val view = inflater.inflate(R.layout.manga_grid_item, parent, false)
          MangaGridHolder(view, this, glideRequests)
        } else {
          val view = inflater.inflate(R.layout.manga_list_item, parent, false)
          MangaListHolder(view, this, glideRequests)
        }
      }
      FOOTER_VIEWTYPE -> {
        val view = inflater.inflate(R.layout.catalogbrowse_footer_item, parent, false)
        FooterHolder(view)
      }
      else -> error("$viewType is not a valid viewtype")
    }

  }

  /**
   * Binds this [holder] with the item on this [position].
   */
  override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
    // Unused
  }

  /**
   * Binds this [holder] with the item on this [position], supporting partial updates with
   * [payloads].
   */
  override fun onBindViewHolder(
    holder: RecyclerView.ViewHolder,
    position: Int,
    payloads: List<Any>
  ) {
    when (holder) {
      is MangaHolder -> {
        val manga = getItem(position) as Manga
        if (payloads.isEmpty()) {
          holder.bind(manga)
        } else {
          val payload = payloads.first { it is MangaPayload } as MangaPayload
          if (payload.coverChange) {
            holder.bindImage(manga)
          }
          if (payload.favoriteChange) {
            holder.bindFavorite(manga)
          }
        }
      }
      is FooterHolder -> {
        val item = getItem(position)
        holder.bind(item === LoadingMore, item === EndReached)
      }
    }
  }

  override fun onViewRecycled(holder: RecyclerView.ViewHolder) {
    when (holder) {
      is MangaHolder -> holder.recycle()
    }
  }

  /**
   * Handles a user click on the element at the given [position]. The click is delegated to the
   * [listener] of this adapter.
   */
  fun handleClick(position: Int) {
    val manga = getItemOrNull(position) as? Manga ?: return
    listener?.onMangaClick(manga)
  }

  /**
   * Handles a user long click on the element at the given [position]. The click is delegated to
   * the listener of this adapter.
   */
  fun handleLongClick(position: Int) {
    val manga = getItemOrNull(position) as? Manga ?: return
    listener?.onMangaLongClick(manga)
  }

  /**
   * Returns the span size for the item at the given [position]. Only used when in grid mode.
   */
  fun getSpanSize(position: Int): Int? {
    return when (getItemViewType(position)) {
      MANGA_VIEWTYPE -> 1
      else -> null
    }
  }

  /**
   * Listener used to delegate clicks on this adapter.
   */
  interface Listener {

    /**
     * Called when this [manga] is clicked.
     */
    fun onMangaClick(manga: Manga)

    /**
     * Called when this [manga] is long clicked.
     */
    fun onMangaLongClick(manga: Manga)

  }

  /**
   * Diff implementation for all the items on this adapter.
   */
  override val itemCallback = object : DiffUtil.ItemCallback<Any>() {

    /**
     * Returns whether [oldItem] and [newItem] are the same.
     */
    override fun areItemsTheSame(oldItem: Any, newItem: Any): Boolean {
      return when {
        oldItem === newItem -> true
        oldItem is Manga && newItem is Manga -> oldItem.id == newItem.id
        else -> false
      }
    }

    /**
     * Returns whether the contents of [oldItem] and [newItem] are the same.
     */
    override fun areContentsTheSame(oldItem: Any, newItem: Any): Boolean {
      return oldItem == newItem
    }

    /**
     * Returns an optional payload to describe partial updates.
     */
    override fun getChangePayload(oldItem: Any, newItem: Any) = when (newItem) {
      is Manga -> {
        oldItem as Manga
        MangaPayload(
          coverChange = oldItem.cover != newItem.cover,
          favoriteChange = oldItem.favorite != newItem.favorite
        )
      }
      else -> null
    }
  }

  /**
   * The item used in the adapter to show a progress bar.
   */
  private object LoadingMore

  /**
   * The item used in the adapter to display a message when there are no more results.
   */
  private object EndReached

  /**
   * A payload used to notify the adapter of the manga data that changed.
   */
  private data class MangaPayload(
    val coverChange: Boolean,
    val favoriteChange: Boolean
  )

  private companion object {
    /**
     * View type for a [Manga].
     */
    const val MANGA_VIEWTYPE = 1

    /**
     * View type for a [LoadingMore] or [EndReached] item.
     */
    const val FOOTER_VIEWTYPE = 2
  }

}
