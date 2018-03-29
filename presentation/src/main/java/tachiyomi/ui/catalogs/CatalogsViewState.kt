package tachiyomi.ui.catalogs

import tachiyomi.domain.source.CatalogueSource

data class CatalogsViewState(
  val catalogues: List<CatalogueSource> = emptyList()
)
