package tachiyomi.source

import tachiyomi.source.model.FilterList
import tachiyomi.source.model.Listing
import tachiyomi.source.model.MangasPageMeta

interface CatalogSource : Source {

  val lang: String

  fun fetchMangaList(sort: Listing?, page: Int): MangasPageMeta

  fun fetchMangaList(filters: FilterList, page: Int): MangasPageMeta

  fun getListings(): List<Listing>

  fun getFilters(): FilterList

}
