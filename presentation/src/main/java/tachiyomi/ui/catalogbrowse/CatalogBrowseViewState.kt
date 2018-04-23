package tachiyomi.ui.catalogbrowse

import tachiyomi.domain.manga.model.Manga
import tachiyomi.domain.source.CatalogSource

data class CatalogBrowseViewState(
  val source: CatalogSource? = null,
  val mangas: List<Manga> = emptyList(),
  val query: String = "",
  val isGridMode: Boolean = true,
  val isLoading: Boolean = false,
  val hasMorePages: Boolean = true,
  val error: Throwable? = null
) {

  override fun toString(): String{
    return "CatalogBrowseViewState(source=$source, mangas={size: ${mangas.size}, ids: " +
           "[${mangas.joinToString(", ") { it.id.toString() }}]}, " +
           "query='$query', isGridMode=$isGridMode, isLoading=$isLoading, " +
           "hasMorePages=$hasMorePages error=$error)"
  }
}
