package tachiyomi.ui.catalogs

import tachiyomi.domain.source.CatalogSource

data class CatalogsViewState(
  val catalogs: List<CatalogSource> = emptyList()
)
