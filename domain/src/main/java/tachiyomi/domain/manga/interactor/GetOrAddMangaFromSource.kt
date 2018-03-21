package tachiyomi.domain.manga.interactor

import io.reactivex.Single
import tachiyomi.core.util.Optional
import tachiyomi.domain.manga.model.Manga
import tachiyomi.domain.manga.repository.MangaRepository
import tachiyomi.domain.source.model.SManga
import javax.inject.Inject

class GetOrAddMangaFromSource @Inject internal constructor(
  private val mangaRepository: MangaRepository
) {

  fun interact(manga: SManga, sourceId: Long): Single<Manga> {
    return mangaRepository.getManga(manga.key, sourceId)
      .take(1)
      .singleOrError()
      .flatMap { optional ->
        if (optional is Optional.Some) {
          Single.just(optional.value)
        } else {
          mangaRepository.saveAndReturnNewManga(manga, sourceId)
        }
      }
  }

}
