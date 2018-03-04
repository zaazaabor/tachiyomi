package tachiyomi.domain.manga.interactor

import io.reactivex.Completable
import tachiyomi.domain.manga.Manga
import tachiyomi.domain.manga.repository.MangaRepository
import javax.inject.Inject

// TODO either one use case containing everything or many
class SetMangaFlags @Inject constructor(private val mangaRepository: MangaRepository) {

  fun interact(manga: Manga, flags: Int): Completable {
    return mangaRepository.setFlags(manga, flags)
  }
}
