package tachiyomi.ui.catalogbrowse

import tachiyomi.source.model.FilterList
import tachiyomi.source.model.Listing
import tachiyomi.ui.catalogbrowse.QueryMode.Filter
import tachiyomi.ui.catalogbrowse.QueryMode.List

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
