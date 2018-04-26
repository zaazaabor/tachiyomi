package tachiyomi.domain.manga.interactor

import io.reactivex.Flowable
import io.reactivex.Single
import tachiyomi.domain.manga.model.MangasPage
import tachiyomi.domain.source.CatalogSource
import tachiyomi.domain.source.model.FilterList
import javax.inject.Inject

class SearchMangaPageFromCatalogSource @Inject internal constructor(
  private val getOrAddMangaFromSource: GetOrAddMangaFromSource
) {

  fun interact(
    source: CatalogSource,
    page: Int,
    query: String,
    filters: FilterList = emptyList()
  ): Single<MangasPage> {
    return Single.defer {
      val sourcePage = if (query.isEmpty() && filters.isEmpty()) {
        source.fetchMangaList(page)
      } else {
        source.searchMangaList(page, query, filters)
      }

      Flowable.fromIterable(sourcePage.mangas)
        .concatMapSingle { getOrAddMangaFromSource.interact(it, source.id) }
        .toList()
        .map { MangasPage(page, it, sourcePage.hasNextPage) }
    }
  }
}
