/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.ui.screens.catalogs

import android.content.Context
import android.graphics.PorterDuff
import android.text.SpannedString
import android.text.style.AbsoluteSizeSpan
import android.text.style.ForegroundColorSpan
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.text.buildSpannedString
import androidx.core.text.inSpans
import com.jaredrummler.cyanea.Cyanea
import kotlinx.android.synthetic.main.catalogs_catalog_item.*
import tachiyomi.domain.catalog.model.Catalog
import tachiyomi.domain.catalog.model.CatalogInstalled
import tachiyomi.domain.catalog.model.CatalogRemote
import tachiyomi.ui.R
import tachiyomi.ui.adapter.BaseViewHolder
import tachiyomi.ui.glide.GlideRequests
import tachiyomi.ui.util.getColorFromAttr
import tachiyomi.ui.util.inflate
import tachiyomi.ui.util.visibleIf

class CatalogHolder(
  parent: ViewGroup,
  private val theme: Theme,
  private val glideRequests: GlideRequests,
  adapter: CatalogAdapter
) : BaseViewHolder(parent.inflate(R.layout.catalogs_catalog_item)) {

  init {
    itemView.setOnClickListener {
      adapter.handleRowClick(adapterPosition)
    }
    install_btn.setOnClickListener {
      adapter.handleInstallClick(adapterPosition)
    }
    settings_btn.setOnClickListener {
      adapter.handleSettingsClick(adapterPosition)
    }

    install_btn.setColorFilter(theme.iconColor, PorterDuff.Mode.SRC_IN)
    settings_btn.setColorFilter(theme.iconColor, PorterDuff.Mode.SRC_IN)
  }

  fun bind(catalog: Catalog) {
    title.text = when (catalog) {
      is CatalogInstalled -> getTitleWithVersion(catalog.name, catalog.versionCode)
      is CatalogRemote -> getTitleWithVersion(catalog.name, catalog.versionCode)
      else -> catalog.name
    }

    description.text = catalog.description
    description.visibleIf { catalog.description.isNotEmpty() }

    install_btn.visibleIf {
      catalog is CatalogRemote || (catalog is CatalogInstalled && catalog.hasUpdate)
    }
    settings_btn.visibleIf { catalog is CatalogInstalled }

    glideRequests.load(catalog)
      .into(image)
  }

  override fun recycle() {
    glideRequests.clear(image)
  }

  private fun getTitleWithVersion(title: String, version: Int): SpannedString {
    return buildSpannedString {
      append("$title ")

      inSpans(AbsoluteSizeSpan(12, true), ForegroundColorSpan(theme.textColorSecondary)) {
        append("v$version")
      }
    }
  }

  class Theme(context: Context) {
    private val cyanea: Cyanea get() = Cyanea.instance

    val iconColor = ContextCompat.getColor(context, if (cyanea.isDark) {
      R.color.textColorIconInverse
    } else {
      R.color.textColorIcon
    })

    val textColorSecondary = context.getColorFromAttr(android.R.attr.textColorSecondary)
  }

}
