package tachiyomi.domain.manga.interactor

import io.reactivex.Flowable
import io.reactivex.Single
import tachiyomi.domain.manga.model.MangasPage
import tachiyomi.source.CatalogSource
import javax.inject.Inject

class GetMangaPageFromCatalogSource @Inject internal constructor(
  private val getOrAddMangaFromSource: GetOrAddMangaFromSource
) {

  fun interact(source: CatalogSource, page: Int): Single<MangasPage> {
    return Single.defer {
      val sourcePage = source.fetchMangaList(page)

      Flowable.fromIterable(sourcePage.mangas)
        .concatMapSingle { getOrAddMangaFromSource.interact(it, source.id) }
        .toList()
        .map { MangasPage(page, it, sourcePage.hasNextPage) }
    }
  }
}
