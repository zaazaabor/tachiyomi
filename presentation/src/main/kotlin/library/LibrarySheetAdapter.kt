/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.ui.library

import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import tachiyomi.domain.library.model.Category
import tachiyomi.domain.library.model.LibraryFilter
import tachiyomi.domain.library.model.LibrarySort
import tachiyomi.domain.library.model.LibrarySorting
import tachiyomi.ui.adapter.BaseListAdapter
import tachiyomi.ui.adapter.BaseViewHolder
import tachiyomi.ui.adapter.ItemCallback

class LibrarySheetAdapter(
  private val listener: Listener
) : BaseListAdapter<LibrarySheetItem, BaseViewHolder>() {

  override fun getItemViewType(position: Int): Int {
    return when (getItem(position)) {
      CategoriesHeader -> VIEW_TYPE_CATEGORY_HEADER
      is CategoryItem -> VIEW_TYPE_CATEGORY
      FiltersHeader -> VIEW_TYPE_FILTERS_HEADER
      is FilterItem -> VIEW_TYPE_FILTER
      SortHeader -> VIEW_TYPE_SORT_HEADER
      is SortItem -> VIEW_TYPE_SORT
    }
  }

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
    return when (viewType) {
      VIEW_TYPE_CATEGORY_HEADER -> CategoriesHeaderHolder(parent, this)
      VIEW_TYPE_CATEGORY -> CategoryHolder(parent, this)
      VIEW_TYPE_FILTERS_HEADER -> FiltersHeaderHolder(parent)
      VIEW_TYPE_FILTER -> FilterHolder(parent, this)
      VIEW_TYPE_SORT_HEADER -> SortHeaderHolder(parent)
      VIEW_TYPE_SORT -> SortHolder(parent, this)
      else -> error("Unknown view type $viewType")
    }
  }

  override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
    when (val item = getItem(position)) {
      is CategoryItem -> (holder as CategoryHolder).bind(item)
      is FilterItem -> (holder as FilterHolder).bind(item)
      is SortItem -> (holder as SortHolder).bind(item)
    }
  }

  fun render(
    categories: List<Category>,
    selectedCategory: Category?,
    selectedFilters: List<LibraryFilter>,
    selectedSort: LibrarySorting
  ) {
    val list = mutableListOf<LibrarySheetItem>()
    list += CategoriesHeader
    val selectedId = selectedCategory?.id
    list.addAll(categories.map { CategoryItem(it, it.id == selectedId) })
    list += FiltersHeader
    list += listOf(LibraryFilter.Downloaded, LibraryFilter.Unread, LibraryFilter.Completed)
      .map { FilterItem(it, false) }
    list += SortHeader
    list += listOf(LibrarySort.Title, LibrarySort.LastRead, LibrarySort.LastUpdated,
      LibrarySort.Unread, LibrarySort.TotalChapters, LibrarySort.Source)
      .map { sort -> SortItem(sort, selectedSort.type == sort) }

    submitList(list)
  }

  /**
   * Returns the callback to be used for a diff between [oldList] and [newList].
   */
  override fun getDiffCallback(
    oldList: List<LibrarySheetItem>,
    newList: List<LibrarySheetItem>
  ): DiffUtil.Callback {
    return Callback(oldList, newList)
  }

  fun handleCategoryClick(position: Int) {
    val item = getItemOrNull(position) as? CategoryItem ?: return
    listener.onCategoryClick(item.category)
  }

  fun handleCategorySettingsClick() {
    listener.onCategorySettingsClick()
  }

  fun handleSortClick(position: Int) {
    val item = getItemOrNull(position) as? SortItem ?: return
    listener.onSortClick(item.sort)
  }

  class Callback(
    oldList: List<LibrarySheetItem>,
    newList: List<LibrarySheetItem>
  ) : ItemCallback<LibrarySheetItem>(oldList, newList) {

    override fun areItemsTheSame(oldItem: LibrarySheetItem, newItem: LibrarySheetItem): Boolean {
      return oldItem == newItem
    }

    override fun areContentsTheSame(oldItem: LibrarySheetItem, newItem: LibrarySheetItem): Boolean {
      return true
    }
  }

  interface Listener {
    fun onCategoryClick(category: Category)
    fun onCategorySettingsClick()
    fun onSortClick(sort: LibrarySort)
  }

  private companion object {
    const val VIEW_TYPE_CATEGORY = 1
    const val VIEW_TYPE_CATEGORY_HEADER = 2
    const val VIEW_TYPE_FILTERS_HEADER = 3
    const val VIEW_TYPE_FILTER = 4
    const val VIEW_TYPE_SORT_HEADER = 5
    const val VIEW_TYPE_SORT = 6
  }

}
