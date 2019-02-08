/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.ui.screens.catalogbrowse

import android.view.View
import tachiyomi.domain.manga.model.Manga
import tachiyomi.ui.adapter.BaseViewHolder

/**
 * Abstract holder to use when displaying a [Manga] from a [CatalogBrowseAdapter]. This base
 * class is used to have a common interface on list a grid holders.
 */
abstract class MangaHolder(view: View) : BaseViewHolder(view) {

  /**
   * Binds the given [manga] with this holder.
   */
  abstract fun bind(manga: Manga)

  /**
   * Binds only the cover of the given [manga] with this holder.
   */
  abstract fun bindImage(manga: Manga)

  abstract fun bindFavorite(manga: Manga)

}
