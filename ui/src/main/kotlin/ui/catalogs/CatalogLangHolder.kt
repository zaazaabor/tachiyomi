/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.ui.catalogs

import android.content.res.ColorStateList
import android.view.View
import androidx.core.graphics.ColorUtils
import androidx.emoji.text.EmojiCompat
import kotlinx.android.synthetic.main.catalogs_lang_item.*
import tachiyomi.ui.R
import tachiyomi.ui.base.BaseViewHolder
import tachiyomi.util.getResourceColor

class CatalogLangHolder(view: View, adapter: CatalogLangsAdapter) : BaseViewHolder(view) {

  private val unsetColor = catalogs_lang_chip.chipBackgroundColor

  private val selectedColor = ColorStateList.valueOf(
    ColorUtils.setAlphaComponent(view.context.getResourceColor(R.attr.colorPrimary), 64)
  )

  private var currentText: String? = null

  init {
    view.setOnClickListener {
      adapter.handleClick(adapterPosition)
    }
  }

  fun bind(choice: LanguageChoice, isSelected: Boolean) {
    val newText = when (choice) {
      LanguageChoice.All -> "All" // TODO string resource
      is LanguageChoice.One -> choice.language.toEmoji() ?: ""
      is LanguageChoice.Others -> "Others" // TODO string resource
    }
    if (currentText != newText) {
      currentText = newText
      catalogs_lang_chip.text = EmojiCompat.get().process(newText)
    }
    catalogs_lang_chip.isSelected = isSelected
    catalogs_lang_chip.chipBackgroundColor = if (isSelected) selectedColor else unsetColor
  }

}
