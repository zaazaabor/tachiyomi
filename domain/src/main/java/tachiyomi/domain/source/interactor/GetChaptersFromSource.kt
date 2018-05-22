package tachiyomi.domain.source.interactor

import io.reactivex.Single
import tachiyomi.domain.manga.model.Manga
import tachiyomi.source.Source
import tachiyomi.source.model.ChapterMeta
import tachiyomi.source.model.MangaMeta
import javax.inject.Inject

class GetChaptersFromSource @Inject constructor() {

  fun interact(source: Source, manga: Manga): Single<List<ChapterMeta>> {
    return Single.fromCallable {
      val meta = MangaMeta(
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
      source.fetchChapterList(meta)
    }
  }

}
