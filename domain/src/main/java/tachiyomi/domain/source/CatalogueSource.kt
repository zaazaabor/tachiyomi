package tachiyomi.domain.source

import tachiyomi.domain.source.model.SMangasPage

interface CatalogueSource : Source {

  val lang: String

  fun fetchMangaList(page: Int): SMangasPage

}
