/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.ui.catalogs

import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import tachiyomi.domain.catalog.model.Catalog
import tachiyomi.ui.base.BaseListAdapter
import tachiyomi.ui.base.BaseViewHolder

class CatalogsAdapter(
  controller: CatalogsController
) : BaseListAdapter<Any, BaseViewHolder>(Diff()) {

  private val listener: Listener = controller

  private val langsAdapter = CatalogLangsAdapter(controller, listener)

  private val catalogTheme = CatalogHolder.Theme(controller.activity!!)

  override fun getItemViewType(position: Int): Int {
    val item = getItem(position)
    return when (item) {
      is Catalog -> VIEW_TYPE_CATALOG
      is LanguageChoices -> VIEW_TYPE_LANGUAGES
      is CatalogHeader -> VIEW_TYPE_HEADER
      else -> error("Unknown view type for item class ${item.javaClass}")
    }
  }

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
    return when (viewType) {
      VIEW_TYPE_CATALOG -> CatalogHolder(parent, catalogTheme, this)
      VIEW_TYPE_LANGUAGES -> CatalogLangsHolder(parent, langsAdapter)
      VIEW_TYPE_HEADER -> CatalogHeaderHolder(parent)
      else -> error("TODO")
    }
  }

  @Suppress("UNCHECKED_CAST")
  override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
    val item = getItem(position)
    when (holder) {
      is CatalogHolder -> holder.bind(item as Catalog)
      is CatalogHeaderHolder -> holder.bind(item as CatalogHeader)
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
    const val VIEW_TYPE_CATALOG = 1
    const val VIEW_TYPE_LANGUAGES = 2
    const val VIEW_TYPE_HEADER = 3
  }

}
