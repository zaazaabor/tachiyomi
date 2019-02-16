/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.ui.screens.catalog

import android.content.Context
import android.view.ViewGroup
import androidx.emoji.text.EmojiCompat
import com.google.android.material.chip.Chip
import tachiyomi.ui.R
import tachiyomi.ui.adapter.BaseViewHolder
import tachiyomi.ui.theme.ChipTheme
import tachiyomi.ui.util.inflate

class CatalogLangHolder(
  parent: ViewGroup,
  theme: Theme,
  adapter: CatalogLangsAdapter
) : BaseViewHolder(parent.inflate(R.layout.catalogs_lang_item)) {

  private var currentText: String? = null

  private val chip = itemView as Chip

  init {
    chip.setOnClickListener { adapter.handleClick(adapterPosition) }
    chip.chipBackgroundColor = theme.chip.backgroundColor
    chip.setTextColor(theme.chip.textColor)
  }

  fun bind(choice: LanguageChoice, isSelected: Boolean) {
    val newText = when (choice) {
      LanguageChoice.All -> chip.context.getString(R.string.lang_all)
      is LanguageChoice.One -> choice.language.toEmoji() ?: ""
      is LanguageChoice.Others -> chip.context.getString(R.string.lang_others)
    }
    if (currentText != newText) {
      currentText = newText
      chip.text = EmojiCompat.get().process(newText)
    }
    chip.isSelected = isSelected
  }

  class Theme(context: Context) {
    val chip = ChipTheme.SelectedAccent(context)
  }

}
