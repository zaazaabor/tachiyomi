/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.ui.catalogs

import android.view.ViewGroup
import kotlinx.android.synthetic.main.catalogs_langs_item.*
import tachiyomi.ui.R
import tachiyomi.ui.base.BaseViewHolder
import tachiyomi.util.inflate

class CatalogLangsHolder(
  parent: ViewGroup,
  adapter: CatalogLangsAdapter
) : BaseViewHolder(parent.inflate(R.layout.catalogs_langs_item)) {

  init {
    catalogs_langs_recycler.itemAnimator = null
    catalogs_langs_recycler.adapter = adapter
  }

}
