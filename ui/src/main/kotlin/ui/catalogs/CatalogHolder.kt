/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.ui.catalogs

import android.view.ViewGroup
import kotlinx.android.synthetic.main.catalogs_browsable_item.*
import tachiyomi.domain.catalog.model.Catalog
import tachiyomi.glide.GlideApp
import tachiyomi.ui.R
import tachiyomi.ui.base.BaseViewHolder
import tachiyomi.util.inflate

class CatalogHolder(
  parent: ViewGroup,
  adapter: CatalogsAdapter
) : BaseViewHolder(parent.inflate(R.layout.catalogs_browsable_item)) {

  init {
    itemView.setOnClickListener {
      adapter.handleRowClick(adapterPosition)
    }
  }

  fun bind(item: Catalog) {
    title.text = item.name

    GlideApp.with(itemView)
      .load(item)
      .into(image)
  }

  fun recycle() {
    GlideApp.with(itemView).clear(image)
  }

}
