package tachiyomi.source

import tachiyomi.source.model.FilterList
import tachiyomi.source.model.MangasPageMeta

interface CatalogSource : Source {

  val lang: String

  fun fetchMangaList(page: Int): MangasPageMeta

  fun searchMangaList(page: Int, query: String, filters: FilterList): MangasPageMeta

  fun getFilterList(): FilterList

}
