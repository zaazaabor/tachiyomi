package tachiyomi.domain.manga.interactor

import io.reactivex.Completable
import tachiyomi.domain.manga.repository.MangaRepository
import javax.inject.Inject

class DeleteNonFavoriteManga @Inject internal constructor(
  private val mangaRepository: MangaRepository
) {

  fun interact(): Completable {
    return mangaRepository.deleteNonFavorite()
  }
}
