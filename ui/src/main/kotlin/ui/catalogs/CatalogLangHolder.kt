/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.ui.catalogs

import android.content.Context
import android.content.res.ColorStateList
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.emoji.text.EmojiCompat
import com.google.android.material.chip.Chip
import com.jaredrummler.cyanea.Cyanea
import com.jaredrummler.cyanea.utils.ColorUtils
import tachiyomi.ui.R
import tachiyomi.ui.base.BaseViewHolder
import tachiyomi.util.getResourceColor
import tachiyomi.util.inflate

class CatalogLangHolder(
  parent: ViewGroup,
  theme: Theme,
  private val adapter: CatalogLangsAdapter
) : BaseViewHolder(parent.inflate(R.layout.catalogs_lang_item)) {

  private var currentText: String? = null

  private val chip = itemView as Chip

  init {
    chip.setOnClickListener {
      adapter.handleClick(adapterPosition)
    }
    chip.chipBackgroundColor = theme.chipBackgroundColor
    chip.setTextColor(theme.chipTextColor)
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
    private val cyanea: Cyanea get() = Cyanea.instance

    val chipBackgroundColor = ColorStateList(
      arrayOf(
        intArrayOf(android.R.attr.state_selected),
        intArrayOf()
      ),
      intArrayOf(
        cyanea.accent,
        if (cyanea.isDark) cyanea.backgroundColorLight else cyanea.backgroundColorDark
      )
    )

    val chipTextColor = ColorStateList(
      arrayOf(
        intArrayOf(android.R.attr.state_selected),
        intArrayOf()
      ),
      intArrayOf(
        ContextCompat.getColor(context, if (ColorUtils.isDarkColor(cyanea.accent)) {
          R.color.textColorPrimaryInverse
        } else {
          R.color.textColorPrimary
        }),
        context.getResourceColor(android.R.attr.textColorPrimary)
      )
    )
  }

}
