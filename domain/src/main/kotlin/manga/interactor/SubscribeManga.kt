package tachiyomi.domain.manga.interactor

import io.reactivex.Flowable
import tachiyomi.core.rx.RxOptional
import tachiyomi.domain.manga.model.Manga
import tachiyomi.domain.manga.repository.MangaRepository
import javax.inject.Inject

class SubscribeManga @Inject constructor(
  private val mangaRepository: MangaRepository
) {

  fun interact(mangaId: Long): Flowable<RxOptional<Manga>> {
    return mangaRepository.subscribeManga(mangaId)

  }

}
