package tachiyomi.domain.manga.interactor

import io.reactivex.Maybe
import tachiyomi.domain.manga.model.Manga
import tachiyomi.domain.manga.repository.MangaRepository
import tachiyomi.source.Source
import tachiyomi.source.model.MangaMeta
import javax.inject.Inject

class MangaInitializer @Inject internal constructor(
  private val mangaRepository: MangaRepository
) {

  fun interact(source: Source, manga: Manga): Maybe<Manga> {
    if (manga.initialized) return Maybe.empty()

    val stubManga = MangaMeta(
      key = manga.key,
      title = manga.title,
      artist = manga.artist,
      author = manga.author,
      description = manga.description,
      genres = manga.genres,
      status = manga.status,
      cover = manga.cover,
      initialized = manga.initialized
    )
    return Maybe.fromCallable { source.fetchMangaDetails(stubManga) }
      .flatMap { sourceManga ->
        val updatedManga = manga.copy(
          key = sourceManga.key,
          title = sourceManga.title,
          artist = sourceManga.artist,
          author = sourceManga.author,
          description = sourceManga.description,
          genres = sourceManga.genres,
          status = sourceManga.status,
          cover = sourceManga.cover,
          initialized = true
        )
        mangaRepository.updateMangaDetails(updatedManga)
          .andThen(Maybe.just(updatedManga))
      }
      .onErrorComplete()
  }
}
