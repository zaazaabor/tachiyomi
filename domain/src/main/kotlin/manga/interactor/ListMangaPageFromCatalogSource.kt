package tachiyomi.domain.manga.interactor

import io.reactivex.Flowable
import io.reactivex.Single
import tachiyomi.domain.manga.model.MangasPage
import tachiyomi.source.CatalogSource
import tachiyomi.source.model.Listing
import javax.inject.Inject

class ListMangaPageFromCatalogSource @Inject internal constructor(
  private val getOrAddMangaFromSource: GetOrAddMangaFromSource
) {

  fun interact(
    source: CatalogSource,
    listing: Listing?,
    page: Int
  ): Single<MangasPage> {
    return Single.defer {
      val sourcePage = source.fetchMangaList(listing, page)

      Flowable.fromIterable(sourcePage.mangas)
        .concatMapSingle { getOrAddMangaFromSource.interact(it, source.id) }
        .toList()
        .map { MangasPage(page, it, sourcePage.hasNextPage) }
    }
  }
}
