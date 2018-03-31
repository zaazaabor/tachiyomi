package tachiyomi.domain.manga.interactor

import io.reactivex.Flowable
import io.reactivex.Single
import tachiyomi.domain.manga.model.MangasPage
import tachiyomi.domain.source.CatalogueSource
import javax.inject.Inject

class GetMangaPageFromCatalogueSource @Inject internal constructor(
  private val getOrAddMangaFromSource: GetOrAddMangaFromSource
) {

  fun interact(source: CatalogueSource, page: Int): Single<MangasPage> {
    return Single.defer {
      val sourcePage = source.fetchMangaList(page)

      Flowable.fromIterable(sourcePage.mangas)
        .flatMapSingle({ getOrAddMangaFromSource.interact(it, source.id) }, false, 1)
        .toList()
        .map { MangasPage(it, sourcePage.hasNextPage) }
    }
  }
}
