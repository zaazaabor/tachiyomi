package tachiyomi.domain.manga.interactor

import io.reactivex.Single
import tachiyomi.domain.manga.Manga
import tachiyomi.domain.manga.repository.MangaRepository
import tachiyomi.domain.source.SManga
import javax.inject.Inject

class GetOrAddMangaFromSource @Inject internal constructor(
  private val mangaRepository: MangaRepository
) {

  fun interact(manga: SManga, sourceId: Long): Single<Manga> {
    return mangaRepository.getManga(manga.url, sourceId)
      .switchIfEmpty(Single.defer { mangaRepository.saveAndReturnNewManga(manga, sourceId) })
  }

}
