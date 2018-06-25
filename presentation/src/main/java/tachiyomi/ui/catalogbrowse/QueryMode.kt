package tachiyomi.ui.catalogbrowse

import tachiyomi.source.model.FilterList
import tachiyomi.source.model.Listing

sealed class QueryMode {
  data class List(val listing: Listing?) : QueryMode()
  data class Filter(val filters: FilterList) : QueryMode()
}
