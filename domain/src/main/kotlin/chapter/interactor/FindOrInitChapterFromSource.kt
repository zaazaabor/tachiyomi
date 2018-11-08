package tachiyomi.domain.chapter.interactor

import io.reactivex.Single
import tachiyomi.domain.chapter.model.Chapter
import tachiyomi.domain.chapter.repository.ChapterRepository
import tachiyomi.domain.chapter.util.ChapterRecognition
import tachiyomi.domain.manga.model.Manga
import tachiyomi.domain.source.interactor.GetChaptersFromSource
import tachiyomi.source.Source
import javax.inject.Inject

class FindOrInitChapterFromSource @Inject constructor(
  private val chapterRepository: ChapterRepository,
  private val getChaptersFromSource: GetChaptersFromSource
) {

  fun interact(
    chapterKey: String,
    source: Source,
    manga: Manga
  ): Single<Chapter> {
    return chapterRepository.getChapter(chapterKey, manga.id)
      .switchIfEmpty(Single.defer {
        getChaptersFromSource.interact(source, manga)
          .flatMapCompletable { chapters ->
            if (chapters.isEmpty()) {
              throw Exception("No chapters found")
            }

            val sourceChapters = chapters.mapIndexed { i, meta ->
              Chapter(
                id = -1,
                mangaId = manga.id,
                key = meta.key,
                name = meta.name,
                dateUpload = meta.dateUpload,
                dateFetch = System.currentTimeMillis(),
                scanlator = meta.scanlator,
                number = meta.number.takeIf { it >= 0f } ?: ChapterRecognition.parse(meta, manga),
                sourceOrder = i
              )
            }

            val chapterToAdd = sourceChapters.find { it.key == chapterKey }
              ?: throw Exception("Requested chapter $chapterKey not found in source list")

            chapterRepository.syncChapter(chapterToAdd, sourceChapters)
          }
          .andThen(chapterRepository.getChapter(chapterKey, manga.id).toSingle())
      })
  }

}
