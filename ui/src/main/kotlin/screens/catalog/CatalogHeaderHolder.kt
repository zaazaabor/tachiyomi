/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.ui.screens.catalogs

import android.view.ViewGroup
import android.widget.TextView
import tachiyomi.ui.R
import tachiyomi.ui.adapter.BaseViewHolder
import tachiyomi.ui.util.inflate

class CatalogHeaderHolder(
  parent: ViewGroup
) : BaseViewHolder(parent.inflate(R.layout.catalogs_header_item)) {

  private val textView = itemView as TextView

  fun bind(header: CatalogHeader) {
    // TODO string resources
    textView.text = when (header) {
      CatalogHeader.Installed -> "Installed"
      CatalogHeader.Available -> "Available"
    }
  }

}
