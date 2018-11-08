package tachiyomi.domain.manga.interactor

import io.reactivex.Maybe
import tachiyomi.domain.manga.model.Manga
import tachiyomi.domain.manga.repository.MangaRepository
import javax.inject.Inject

class GetManga @Inject constructor(
  private val mangaRepository: MangaRepository
) {

  fun interact(mangaId: Long): Maybe<Manga> {
    return mangaRepository.getManga(mangaId)
  }

  fun interact(mangaKey: String, sourceId: Long): Maybe<Manga> {
    return mangaRepository.getManga(mangaKey, sourceId)
  }

}
