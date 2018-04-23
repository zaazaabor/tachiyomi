package tachiyomi.domain.source

import tachiyomi.domain.source.model.MangasPageMeta

interface CatalogSource : Source {

  val lang: String

  fun fetchMangaList(page: Int): MangasPageMeta

}
