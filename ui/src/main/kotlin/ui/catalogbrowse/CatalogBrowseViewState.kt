/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.ui.catalogbrowse

import tachiyomi.domain.manga.model.Manga
import tachiyomi.source.CatalogSource
import tachiyomi.source.model.Listing

/**
 * View state of the catalog browse UI.
 *
 * [source] contains the selected catalog. Can be null if the catalog wasn't found.
 * [mangas] contains the list of manga returned by the current [queryMode].
 * [queryMode] contains the query mode: either a listing (alphabetically, latest...) or a search.
 * [listings] contains all the listings from this [source].
 * [filters] contains all the wrapped filters from this [source].
 * [isGridMode] whether the UI should display results as a grid or a list.
 * [isLoading] whether the catalog is currently loading more results.
 * [hasMorePages] whether the catalog has more pages that can be loaded.
 * [error] contains any error that could occur when loading, or null if there's no error.
 */
data class CatalogBrowseViewState(
  val source: CatalogSource? = null,
  val mangas: List<Manga> = emptyList(),
  val queryMode: QueryMode? = null,
  val listings: List<Listing> = emptyList(),
  val filters: List<FilterWrapper<*>> = emptyList(),
  val isGridMode: Boolean = true,
  val isLoading: Boolean = false,
  val currentPage: Int = 0,
  val hasMorePages: Boolean = true,
  val error: Throwable? = null
) {

  override fun toString(): String {
    return "CatalogBrowseViewState(" +
      "source=$source, " +
      "mangas=${mangas.size}, " +
      "queryMode=$queryMode, " +
      "listings=${listings.size}, " +
      "filters=${filters.size}, " +
      "isGridMode=$isGridMode, " +
      "isLoading=$isLoading, " +
      "currentPage=$currentPage, " +
      "hasMorePages=$hasMorePages, " +
      "error=$error)"
  }
}
