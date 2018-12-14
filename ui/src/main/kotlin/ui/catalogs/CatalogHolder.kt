/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.ui.catalogs

import android.view.View
import kotlinx.android.synthetic.main.catalogs_card_item.*
import tachiyomi.domain.catalog.model.Catalog
import tachiyomi.glide.GlideApp
import tachiyomi.ui.base.BaseViewHolder

class CatalogHolder(view: View, adapter: CatalogsAdapter) : BaseViewHolder(view) {

  init {
    view.setOnClickListener {
      adapter.handleRowClick(adapterPosition)
    }
  }

  fun bind(item: Catalog) {
    // TODO maybe don't prepend Tachiyomi: to extension catalogs
    val name = when (item) {
      is Catalog.Installed -> item.source.name
      else -> item.name
    }
    title.text = name

    GlideApp.with(itemView)
      .load(item)
      .into(image)
  }

  fun recycle() {
    GlideApp.with(itemView).clear(image)
  }

}
