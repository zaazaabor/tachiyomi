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
import tachiyomi.app.R
import tachiyomi.source.CatalogSource
import tachiyomi.ui.base.BaseListAdapter

class CatalogsAdapter(
  controller: CatalogsController
) : BaseListAdapter<CatalogSource, SourceHolder>(Diff()) {

  private val listener: Listener = controller

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SourceHolder {
    val inflater = LayoutInflater.from(parent.context)
    val view = inflater.inflate(R.layout.catalogs_card_item, parent, false)
    return SourceHolder(view, this)
  }

  override fun onBindViewHolder(holder: SourceHolder, position: Int) {
    holder.bind(getItem(position))
  }

  fun handleRowClick(position: Int) {
    val item = getItemOrNull(position) ?: return
    listener.onRowClick(item)
  }

  fun handleBrowseClick(position: Int) {
    val item = getItemOrNull(position) ?: return
    listener.onBrowseClick(item)
  }

  fun handleLatestClick(position: Int) {
    val item = getItemOrNull(position) ?: return
    listener.onLatestClick(item)
  }

  interface Listener {
    fun onRowClick(catalog: CatalogSource)
    fun onBrowseClick(catalog: CatalogSource)
    fun onLatestClick(catalog: CatalogSource)
  }

  private class Diff : DiffUtil.ItemCallback<CatalogSource>() {
    override fun areItemsTheSame(oldItem: CatalogSource, newItem: CatalogSource): Boolean {
      return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: CatalogSource, newItem: CatalogSource): Boolean {
      return true
    }
  }

}
