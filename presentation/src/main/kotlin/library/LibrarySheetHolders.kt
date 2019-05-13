/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.ui.library

import android.view.ViewGroup
import com.google.android.material.chip.Chip
import kotlinx.android.synthetic.main.library_sheet_categories_header.*
import tachiyomi.ui.R
import tachiyomi.ui.adapter.BaseViewHolder
import tachiyomi.ui.category.getVisibleName
import tachiyomi.ui.theme.ChipTheme
import tachiyomi.ui.util.inflate

class CategoriesHeaderHolder(
  parent: ViewGroup,
  adapter: LibrarySheetAdapter
) : BaseViewHolder(parent.inflate(R.layout.library_sheet_categories_header)) {

  init {
    library_sheet_categories_header_icon.setOnClickListener {
      adapter.handleCategorySettingsClick()
    }
  }

}

class CategoryHolder(
  parent: ViewGroup,
  adapter: LibrarySheetAdapter
) : BaseViewHolder(parent.inflate(R.layout.library_sheet_category_item)) {

  private val theme = ChipTheme.SelectedAccent(parent.context)

  private val chip = itemView as Chip

  init {
    chip.chipBackgroundColor = theme.backgroundColor
    chip.setTextColor(theme.textColor)
    chip.setOnClickListener { adapter.handleCategoryClick(adapterPosition) }
  }

  fun bind(item: CategoryItem) {
    chip.text = item.category.getVisibleName(itemView.context)
    chip.isSelected = item.isSelected
  }

}

class FiltersHeaderHolder(
  parent: ViewGroup
) : BaseViewHolder(parent.inflate(R.layout.library_sheet_filters_header)) {

}

class FilterHolder(
  parent: ViewGroup,
  adapter: LibrarySheetAdapter
) : BaseViewHolder(parent.inflate(R.layout.library_sheet_filter_item)) {

  private val theme = ChipTheme.SelectedAccent(parent.context)

  private val chip = itemView as Chip

  init {
    chip.chipBackgroundColor = theme.backgroundColor
    chip.setTextColor(theme.textColor)
  }

  fun bind(item: FilterItem) {
    chip.text = item.filter.name // TODO string resources
    chip.isSelected = item.isEnabled
  }

}

class SortHeaderHolder(
  parent: ViewGroup
) : BaseViewHolder(parent.inflate(R.layout.library_sheet_sort_header)) {

}

class SortHolder(
  parent: ViewGroup,
  adapter: LibrarySheetAdapter
) : BaseViewHolder(parent.inflate(R.layout.library_sheet_sort_item)) {

  private val theme = ChipTheme.SelectedAccent(parent.context)

  private val chip = itemView as Chip

  init {
    chip.chipBackgroundColor = theme.backgroundColor
    chip.setTextColor(theme.textColor)
    chip.setOnClickListener { adapter.handleSortClick(adapterPosition) }
  }

  fun bind(item: SortItem) {
    chip.text = item.sort.getName(itemView.context)
    chip.isSelected = item.isSelected
  }

}
