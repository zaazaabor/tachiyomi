/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.ui.catalogs

import android.content.pm.PackageManager
import android.view.View
import kotlinx.android.synthetic.main.catalogs_card_item.*
import tachiyomi.core.di.AppScope
import tachiyomi.data.extension.ExtensionManager
import tachiyomi.source.CatalogSource
import tachiyomi.ui.base.BaseViewHolder
import javax.inject.Inject

class SourceHolder(view: View, adapter: CatalogsAdapter) : BaseViewHolder(view) {

  // TODO extensions manager shouldn't be injected here. Consider having a 1-1 relationship
  // between catalogues and extensions and merge managers & functionality.
  @Inject
  lateinit var extensionManager: ExtensionManager

  init {
    AppScope.inject(this)
    view.setOnClickListener {
      adapter.handleRowClick(adapterPosition)
    }
  }

  fun bind(item: CatalogSource) {
    title.text = item.name
    bindImage(item)
  }

  private fun bindImage(item: CatalogSource) {
    val extension = extensionManager.installedExtensions.find { item in it.sources } ?: return
    val icon = try {
      itemView.context.packageManager.getApplicationIcon(extension.pkgName)
    } catch (e: PackageManager.NameNotFoundException) {
      null
    } ?: return

    image.setImageDrawable(icon)
  }

}
