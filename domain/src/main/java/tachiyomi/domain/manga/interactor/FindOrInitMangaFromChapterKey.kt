package tachiyomi.domain.manga.interactor

import io.reactivex.Maybe
import io.reactivex.Single
import tachiyomi.domain.manga.model.Manga
import tachiyomi.source.DeepLinkSource
import tachiyomi.source.model.MangaMeta
import javax.inject.Inject

class FindOrInitMangaFromChapterKey @Inject constructor(
  private val getOrAddMangaFromSource: GetOrAddMangaFromSource,
  private val mangaInitializer: MangaInitializer
) {

  fun interact(chapterKey: String, source: DeepLinkSource): Single<Manga> {
    return Single
      .defer {
        val mangaKey = source.findMangaKey(chapterKey)
        if (mangaKey != null) {
          val mangaMeta = MangaMeta(key = mangaKey, title = "")
          getOrAddMangaFromSource.interact(mangaMeta, source.id)
        } else {
          Single.error(Exception("Manga key not found"))
        }
      }
      .flatMap {
        mangaInitializer.interact(source, it).switchIfEmpty(Maybe.just(it)).toSingle()
      }
  }

}
