/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.ui.screens.library

import android.annotation.SuppressLint
import android.content.Context
import android.os.Handler
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.ViewGroup
import com.google.android.material.chip.Chip
import tachiyomi.domain.category.Category
import tachiyomi.ui.R
import tachiyomi.ui.screens.category.getVisibleName
import tachiyomi.ui.theme.ChipTheme
import tachiyomi.ui.util.dpToPx
import tachiyomi.ui.util.visibleIf
import tachiyomi.ui.widget.CustomViewTabLayout
import tachiyomi.ui.widget.IconView

class LibraryTabs @JvmOverloads constructor(
  context: Context,
  attrs: AttributeSet? = null,
  defStyle: Int = 0
) : CustomViewTabLayout(context, attrs, defStyle) {

  private var settingsListener: (() -> Unit)? = null

  private val settingsTab = IconView(context).apply {
    layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
      ViewGroup.LayoutParams.MATCH_PARENT)
    setImageResource(R.drawable.ic_settings_black_24dp)
    setPadding(4.dpToPx, 0, 4.dpToPx, 0)

    setOnClickListener { settingsListener?.invoke() }
  }

  private val theme = ChipTheme.SelectedAccent(context)

  private var currentCategories: List<Category>? = null

  init {
    scrollContainer.addView(settingsTab)
  }

  fun setCategories(categories: List<Category>, selectedCategoryId: Long?) {
    if (currentCategories !== categories) {
      currentCategories = categories
      populateCategories(categories)
    }

    val categoryPosition = categories.indexOfFirst { it.id == selectedCategoryId }
    if (categoryPosition != -1) {
      getTabAt(categoryPosition)?.let {
        if (isLaidOut) {
          selectTab(it)
        } else {
          Handler().post { selectTab(it) }
        }
      }
    }
  }

  @SuppressLint("InflateParams")
  private fun populateCategories(categories: List<Category>) {
    val inflater by lazy { LayoutInflater.from(context) }

    removeAllTabs()

    for (category in categories) {
      val tab = newTab()
      if (tab.customView !is Chip) {
        val chip = inflater.inflate(R.layout.library_tab_chip, null) as Chip
        chip.chipBackgroundColor = theme.backgroundColor
        chip.setTextColor(theme.textColor)
        tab.customView = chip
      }
      val chip = tab.customView as Chip
      val name = category.getVisibleName(context)
      if (chip.text != name) {
        chip.text = name
      }
      addTab(tab, setSelected = false)
    }

    settingsTab.visibleIf { categories.isNotEmpty() }
  }

  fun setOnSettingsClickListener(listener: () -> Unit) {
    settingsListener = listener
  }

}
