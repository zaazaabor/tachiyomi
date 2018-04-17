package tachiyomi.ui.catalogbrowse

import tachiyomi.domain.manga.model.Manga
import tachiyomi.domain.source.CatalogueSource

data class CatalogBrowseViewState(
  val source: CatalogueSource? = null,
  val mangas: List<Manga> = emptyList(),
  val query: String = "",
  val isListMode: Boolean = false,
  val isLoading: Boolean = false,
  val hasMorePages: Boolean = true,
  val error: Throwable? = null
) {

  override fun toString(): String{
    return "CatalogBrowseViewState(source=$source, mangas={size: ${mangas.size}, ids: " +
           "[${mangas.joinToString(", ") { it.id.toString() }}]}, " +
           "query='$query', isListMode=$isListMode, isLoading=$isLoading, " +
           "hasMorePages=$hasMorePages error=$error)"
  }
}
