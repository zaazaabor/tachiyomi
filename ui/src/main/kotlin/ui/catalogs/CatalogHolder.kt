/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.ui.catalogs

import android.content.Context
import android.graphics.PorterDuff
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import com.jaredrummler.cyanea.Cyanea
import kotlinx.android.synthetic.main.catalogs_catalog_item.*
import tachiyomi.domain.catalog.model.Catalog
import tachiyomi.domain.catalog.model.CatalogInstalled
import tachiyomi.domain.catalog.model.CatalogRemote
import tachiyomi.glide.GlideApp
import tachiyomi.ui.R
import tachiyomi.ui.base.BaseViewHolder
import tachiyomi.util.inflate
import tachiyomi.util.visibleIf

class CatalogHolder(
  parent: ViewGroup,
  theme: Theme,
  adapter: CatalogsAdapter
) : BaseViewHolder(parent.inflate(R.layout.catalogs_catalog_item)) {

  val cyanea = Cyanea.instance

  init {
    itemView.setOnClickListener {
      adapter.handleRowClick(adapterPosition)
    }

    install_btn.setColorFilter(theme.iconColor, PorterDuff.Mode.SRC_IN)
  }

  fun bind(catalog: Catalog) {
    title.text = catalog.name
    description.text = catalog.description
    description.visibleIf { catalog.description.isNotEmpty() }

    install_btn.visibleIf {
      catalog is CatalogRemote || (catalog is CatalogInstalled && catalog.hasUpdate)
    }

    GlideApp.with(itemView)
      .load(catalog)
      .into(image)
  }

  fun recycle() {
    GlideApp.with(itemView).clear(image)
  }

  class Theme(context: Context) {
    private val cyanea: Cyanea get() = Cyanea.instance

    val iconColor = ContextCompat.getColor(context, if (cyanea.isDark) {
      R.color.textColorIconInverse
    } else {
      R.color.textColorIcon
    })
  }

}
