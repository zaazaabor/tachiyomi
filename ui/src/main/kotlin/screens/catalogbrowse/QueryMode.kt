/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.ui.screens.catalogbrowse

import tachiyomi.source.model.FilterList
import tachiyomi.source.model.Listing
import tachiyomi.ui.screens.catalogbrowse.QueryMode.Filter
import tachiyomi.ui.screens.catalogbrowse.QueryMode.List

/**
 * Query mode to use when querying a catalog. It currently supports a listing query through [List]
 * and a filters query through [Filter].
 */
sealed class QueryMode {

  /**
   * Query to use when requesting a listing, like alphabetically, popular, latest...
   */
  data class List(val listing: Listing?) : QueryMode()

  /**
   * Querty to use when searching the catalog with a list of filters, like a title search or a
   * genre search.
   */
  data class Filter(val filters: FilterList) : QueryMode()

}
