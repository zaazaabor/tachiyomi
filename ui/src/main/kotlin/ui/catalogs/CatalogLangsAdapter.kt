/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.ui.catalogs

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import tachiyomi.ui.R
import tachiyomi.ui.base.BaseListAdapter
import timber.log.Timber

class CatalogLangsAdapter(
  private val listener: CatalogsAdapter.Listener
) : BaseListAdapter<LanguageChoice, CatalogLangHolder>(Diff()) {

  private var selectedChoice: LanguageChoice? = null

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CatalogLangHolder {
    val inflater = LayoutInflater.from(parent.context)
    val view = inflater.inflate(R.layout.catalogs_lang_item, parent, false)
    return CatalogLangHolder(view, this)
  }

  override fun onBindViewHolder(holder: CatalogLangHolder, position: Int) {
    val item = getItem(position)
    Timber.w("Binding $item")
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

  private class Diff : DiffUtil.ItemCallback<LanguageChoice>() {

    override fun areItemsTheSame(oldItem: LanguageChoice, newItem: LanguageChoice): Boolean {
      return oldItem == newItem
    }

    override fun areContentsTheSame(oldItem: LanguageChoice, newItem: LanguageChoice): Boolean {
      return false
    }

  }

}
