package tachiyomi.domain.source.interactor

import io.reactivex.Single
import tachiyomi.domain.manga.model.Manga
import tachiyomi.source.Source
import tachiyomi.source.model.ChapterInfo
import tachiyomi.source.model.MangaInfo
import javax.inject.Inject

class GetChaptersFromSource @Inject constructor() {

  fun interact(source: Source, manga: Manga): Single<List<ChapterInfo>> {
    return Single.fromCallable {
      val mangaInfo = MangaInfo(
        manga.key,
        manga.title,
        manga.artist,
        manga.author,
        manga.description,
        manga.genres,
        manga.status,
        manga.cover,
        manga.initialized
      )
      source.fetchChapterList(mangaInfo)
    }
  }

}
