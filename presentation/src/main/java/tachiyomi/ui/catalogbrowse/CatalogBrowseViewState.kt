package tachiyomi.ui.catalogbrowse

import tachiyomi.domain.manga.model.Manga
import tachiyomi.domain.source.CatalogueSource

data class CatalogBrowseViewState(
  val source: CatalogueSource? = null,
  val mangas: List<Manga> = emptyList(),
  val isListMode: Boolean = false
)
