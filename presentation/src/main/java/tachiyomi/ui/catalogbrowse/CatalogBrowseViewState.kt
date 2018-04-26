package tachiyomi.ui.catalogbrowse

import tachiyomi.domain.manga.model.Manga
import tachiyomi.domain.source.CatalogSource
import tachiyomi.domain.source.model.FilterList

data class CatalogBrowseViewState(
  val source: CatalogSource? = null,
  val mangas: List<Manga> = emptyList(),
  val query: String = "",
  val sourceFilters: FilterList = emptyList(),
  val activeFilters: FilterList = emptyList(),
  val isGridMode: Boolean = true,
  val isLoading: Boolean = false,
  val hasMorePages: Boolean = true,
  val error: Throwable? = null
) {

  override fun toString(): String {
    return "CatalogBrowseViewState(source=$source, mangas=${mangas.size}, query='$query', " +
           "sourceFilters=$sourceFilters, activeFilters=$activeFilters, isGridMode=$isGridMode, " +
           "isLoading=$isLoading, hasMorePages=$hasMorePages, error=$error)"
  }
}
