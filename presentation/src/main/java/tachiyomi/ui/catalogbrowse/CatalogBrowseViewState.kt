package tachiyomi.ui.catalogbrowse

import tachiyomi.domain.manga.model.Manga
import tachiyomi.source.CatalogSource
import tachiyomi.source.model.Listing

data class CatalogBrowseViewState(
  val source: CatalogSource? = null,
  val mangas: List<Manga> = emptyList(),
  val queryMode: QueryMode? = null,
  val listings: List<Listing> = emptyList(),
  val filters: List<FilterWrapper<*>> = emptyList(),
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
