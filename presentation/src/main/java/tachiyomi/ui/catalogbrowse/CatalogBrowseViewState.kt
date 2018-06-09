package tachiyomi.ui.catalogbrowse

import tachiyomi.domain.manga.model.Manga
import tachiyomi.source.CatalogSource
import tachiyomi.source.model.FilterList
import tachiyomi.source.model.Sorting

data class CatalogBrowseViewState(
  val source: CatalogSource? = null,
  val mangas: List<Manga> = emptyList(),
  val sortings: List<Sorting> = emptyList(),
  val activeSorting: Sorting? = null,
  val query: String = "",
  val filters: FilterList = emptyList(),
  val isGridMode: Boolean = true,
  val isLoading: Boolean = false,
  val hasMorePages: Boolean = true,
  val error: Throwable? = null
) {

//  override fun toString(): String {
//    return "CatalogBrowseViewState(source=$source, mangas=${mangas.size}, query='$query', " +
//           "sourceFilters=$sourceFilters, activeFilters=$activeFilters, isGridMode=$isGridMode, " +
//           "isLoading=$isLoading, hasMorePages=$hasMorePages, error=$error)"
//  }
}
