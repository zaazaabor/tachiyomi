package tachiyomi.ui.catalogs

import tachiyomi.source.CatalogSource

data class CatalogsViewState(
  val catalogs: List<CatalogSource> = emptyList()
)
