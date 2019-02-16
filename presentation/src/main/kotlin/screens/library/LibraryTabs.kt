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
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.ViewGroup
import com.google.android.material.chip.Chip
import tachiyomi.domain.library.model.Library
import tachiyomi.ui.R
import tachiyomi.ui.theme.ChipTheme
import tachiyomi.ui.util.dpToPx
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

  init {
    scrollContainer.addView(settingsTab)
  }

  @SuppressLint("InflateParams")
  fun submitList(library: Library) {
    val inflater by lazy { LayoutInflater.from(context) }

    library.forEachIndexed { i, (category, _) ->
      val tab = getTabAt(i)!!
      if (tab.customView !is Chip) {
        val chip = inflater.inflate(R.layout.library_tab_chip, null) as Chip
        chip.chipBackgroundColor = theme.backgroundColor
        chip.setTextColor(theme.textColor)
        tab.customView = chip
      }
      val chip = tab.customView as Chip
      if (chip.text != category.name) {
        chip.text = category.name
      }
    }
  }

  fun setOnSettingsClickListener(listener: () -> Unit) {
    settingsListener = listener
  }

}
