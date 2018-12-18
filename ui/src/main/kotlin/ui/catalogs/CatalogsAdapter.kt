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
import tachiyomi.domain.catalog.model.Catalog
import tachiyomi.ui.R
import tachiyomi.ui.base.BaseListAdapter
import tachiyomi.ui.base.BaseViewHolder

class CatalogsAdapter(
  controller: CatalogsController
) : BaseListAdapter<Any, BaseViewHolder>(Diff()) {

  private val listener: Listener = controller

  private val langsAdapter = CatalogLangsAdapter(listener)

  override fun getItemViewType(position: Int): Int {
    val item = getItem(position)
    return when (item) {
      is Catalog.Internal, is Catalog.Installed -> VIEW_TYPE_BROWSABLE
      is Catalog.Available -> VIEW_TYPE_AVAILABLE
      is LanguageChoices -> VIEW_TYPE_LANGUAGES
      else -> error("Unknown view type for item class ${item.javaClass}")
    }
  }

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
    val inflater = LayoutInflater.from(parent.context)
    return when (viewType) {
      VIEW_TYPE_BROWSABLE -> {
        val view = inflater.inflate(R.layout.catalogs_browsable_item, parent, false)
        CatalogHolder(view, this)
      }
      VIEW_TYPE_AVAILABLE -> {
        val view = inflater.inflate(R.layout.catalogs_browsable_item, parent, false)
        CatalogHolder(view, this)
      }
      VIEW_TYPE_LANGUAGES -> {
        val view = inflater.inflate(R.layout.catalogs_langs_item, parent, false)
        CatalogLangsHolder(view, langsAdapter)
      }
      else -> error("TODO")
    }
  }

  @Suppress("UNCHECKED_CAST")
  override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
    val item = getItem(position)
    when (holder) {
      is CatalogHolder -> holder.bind(item as Catalog)
    }
  }

  override fun onViewRecycled(holder: BaseViewHolder) {
    when (holder) {
      is CatalogHolder -> holder.recycle()
    }
  }

  fun handleRowClick(position: Int) {
    val item = getItemOrNull(position) as? Catalog ?: return
    listener.onCatalogClick(item)
  }

  fun submitItems(items: List<Any>) {
    submitList(items)

    val choices = items.filterIsInstance<LanguageChoices>().firstOrNull()
    if (choices != null) {
      langsAdapter.submitChoices(choices)
    }
  }

  interface Listener {
    fun onCatalogClick(catalog: Catalog)
    fun onLanguageChoiceClick(languageChoice: LanguageChoice)
  }

  private class Diff : DiffUtil.ItemCallback<Any>() {
    override fun areItemsTheSame(oldItem: Any, newItem: Any): Boolean {
      return when {
        // Handled by child adapter
        oldItem is LanguageChoices && newItem is LanguageChoices -> true
        else -> oldItem == newItem
      }
    }

    override fun areContentsTheSame(oldItem: Any, newItem: Any): Boolean {
      return true
    }
  }

  companion object {
    const val VIEW_TYPE_BROWSABLE = 1
    const val VIEW_TYPE_AVAILABLE = 2
    const val VIEW_TYPE_LANGUAGES = 3
  }

}
