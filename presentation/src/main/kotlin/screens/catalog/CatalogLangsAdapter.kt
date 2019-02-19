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
import androidx.recyclerview.widget.DiffUtil
import tachiyomi.ui.adapter.BaseListAdapter
import tachiyomi.ui.adapter.ItemCallback

class CatalogLangsAdapter(
  context: Context,
  private val listener: CatalogAdapter.Listener
) : BaseListAdapter<LanguageChoice, CatalogLangHolder>() {

  private var selectedChoice: LanguageChoice? = null

  private val holderTheme = CatalogLangHolder.Theme(context)

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CatalogLangHolder {
    return CatalogLangHolder(parent, holderTheme, this)
  }

  override fun onBindViewHolder(holder: CatalogLangHolder, position: Int) {
    val item = getItem(position)
    holder.bind(item, item == selectedChoice)
  }

  fun submitChoices(choices: LanguageChoices) {
    selectedChoice = choices.selected
    submitList(choices.choices)
  }

  fun handleClick(position: Int) {
    val choice = getItemOrNull(position) ?: return
    listener.onLanguageChoiceClick(choice)
  }

  override fun getDiffCallback(
    oldList: List<LanguageChoice>,
    newList: List<LanguageChoice>
  ): DiffUtil.Callback {
    return object : ItemCallback<LanguageChoice>(oldList, newList) {
      override fun areItemsTheSame(oldItem: LanguageChoice, newItem: LanguageChoice): Boolean {
        return oldItem == newItem
      }

      override fun areContentsTheSame(oldItem: LanguageChoice, newItem: LanguageChoice): Boolean {
        return false
      }
    }
  }

}
