package tachiyomi.domain.manga.interactor

import io.reactivex.Single
import tachiyomi.domain.manga.model.Manga
import tachiyomi.domain.manga.repository.MangaRepository
import tachiyomi.source.model.MangaInfo
import javax.inject.Inject

class GetOrAddMangaFromSource @Inject internal constructor(
  private val mangaRepository: MangaRepository
) {

  fun interact(manga: MangaInfo, sourceId: Long): Single<Manga> {
    return mangaRepository.getManga(manga.key, sourceId)
      .switchIfEmpty(Single.defer { mangaRepository.saveAndReturnNewManga(manga, sourceId) })
  }

}
