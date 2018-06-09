package tachiyomi.source

import tachiyomi.source.model.MangasPageMeta
import tachiyomi.source.model.SearchQuery
import tachiyomi.source.model.Sorting

interface CatalogSource : Source {

  val lang: String

  fun fetchMangaList(query: SearchQuery, page: Int): MangasPageMeta

  fun getSortings(): List<Sorting>

}
