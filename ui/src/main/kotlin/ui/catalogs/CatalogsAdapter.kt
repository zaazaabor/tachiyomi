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

class CatalogsAdapter(
  controller: CatalogsController
) : BaseListAdapter<Catalog, CatalogHolder>(Diff()) {

  private val listener: Listener = controller

  override fun getItemViewType(position: Int): Int {
    val item = getItem(position)
    return when (item) {
      is Catalog.Internal, is Catalog.Installed -> VIEW_TYPE_BROWSABLE
      else -> 0
    }
  }

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CatalogHolder {
    val inflater = LayoutInflater.from(parent.context)
    return when (viewType) {
      VIEW_TYPE_BROWSABLE -> {
        val view = inflater.inflate(R.layout.catalogs_card_item, parent, false)
        CatalogHolder(view, this)
      }
      else -> error("TODO")
    }
  }

  override fun onBindViewHolder(holder: CatalogHolder, position: Int) {
    holder.bind(getItem(position))
  }

  override fun onViewRecycled(holder: CatalogHolder) {
    holder.recycle()
  }

  fun handleRowClick(position: Int) {
    val item = getItemOrNull(position) ?: return
    listener.onRowClick(item)
  }

  fun submitBrowsableCatalogs(catalogs: List<Catalog>) {
    submitList(catalogs)
  }

  interface Listener {
    fun onRowClick(catalog: Catalog)
  }

  private class Diff : DiffUtil.ItemCallback<Catalog>() {
    override fun areItemsTheSame(oldItem: Catalog, newItem: Catalog): Boolean {
      return oldItem == newItem
    }

    override fun areContentsTheSame(oldItem: Catalog, newItem: Catalog): Boolean {
      return true
    }
  }

  private companion object {
    const val VIEW_TYPE_BROWSABLE = 1
  }

}
