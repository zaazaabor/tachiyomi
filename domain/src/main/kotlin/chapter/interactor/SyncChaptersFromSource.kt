package tachiyomi.domain.chapter.interactor

import io.reactivex.Single
import tachiyomi.domain.chapter.model.Chapter
import tachiyomi.domain.chapter.repository.ChapterRepository
import tachiyomi.domain.chapter.util.ChapterRecognition
import tachiyomi.domain.manga.model.Manga
import tachiyomi.source.model.ChapterInfo
import javax.inject.Inject

class SyncChaptersFromSource @Inject constructor(
  private val chapterRepository: ChapterRepository
) {

  data class Diff(
    val added: List<Chapter> = emptyList(),
    val deleted: List<Chapter> = emptyList(),
    val updated: List<Chapter> = emptyList()
  )

  fun interact(
    rawSourceChapters: List<ChapterInfo>,
    manga: Manga
  ): Single<List<Chapter>> {

    if (rawSourceChapters.isEmpty()) {
      return Single.error(Exception("No chapters found"))
    }

    // Chapters from db.
    return chapterRepository.getChapters(manga.id).flatMap { dbChapters ->

      // Set the date fetch for new items in reverse order to allow another sorting method.
      // Sources MUST return the chapters from most to less recent, which is common.
      var endDateFetch = System.currentTimeMillis() + dbChapters.size

      val sourceChapters = rawSourceChapters.mapIndexed { i, meta ->
        Chapter(
          id = -1,
          mangaId = manga.id,
          key = meta.key,
          name = meta.name,
          dateUpload = meta.dateUpload,
          dateFetch = endDateFetch--,
          scanlator = meta.scanlator,
          number = meta.number.takeIf { it >= 0f } ?: ChapterRecognition.parse(meta, manga),
          sourceOrder = i
        )
      }

      // Chapters from the db not in the source.
      val toDelete = dbChapters.filterNot { dbChapter ->
        sourceChapters.any { it.key == dbChapter.key }
      }
      val toDeleteReadNumbers = toDelete.asSequence()
        .mapNotNull { chapter ->
          if (chapter.isRecognizedNumber && chapter.read) chapter.number else null
        }
        .toSet()

      // Chapters from the source not in db.
      val toAdd = mutableListOf<Chapter>()

      // Chapters whose metadata have changed.
      val toUpdate = mutableListOf<Chapter>()

      for (sourceChapter in sourceChapters) {
        val dbChapter = dbChapters.find { it.key == sourceChapter.key }

        // Add the chapter if not in db already, or update if the metadata changed.
        if (dbChapter == null) {
          // Try to mark already read chapters as read when the source deletes them
          toAdd += if (sourceChapter.number in toDeleteReadNumbers) {
            sourceChapter.copy(read = true)
          } else {
            sourceChapter
          }
        } else {
          //this forces metadata update for the main viewable things in the chapter list
          if (metaChanged(dbChapter, sourceChapter)) {
            toUpdate += dbChapter.copy(
              scanlator = sourceChapter.scanlator,
              name = sourceChapter.name,
              dateUpload = sourceChapter.dateUpload,
              number = sourceChapter.number
            )
          }
        }
      }

      // Return if there's nothing to add, delete or change, avoiding unnecessary db transactions.
      if (toAdd.isEmpty() && toDelete.isEmpty() && toUpdate.isEmpty()) {
        return@flatMap Single.just(emptyList<Chapter>())
      }

      val diff = Diff(toAdd, toDelete, toUpdate)

      // To avoid notifying chapters that changed the key, we emit downstream a modified list that
      // ignores thoses chapters
      val toDeleteNumbers = toDelete.asSequence()
        .mapNotNull { chapter -> if (chapter.isRecognizedNumber) chapter.number else null }
        .toSet()
      val chaptersToNotify = toAdd.toList() - toAdd.filter { it.number in toDeleteNumbers }

      chapterRepository.syncChapters(diff, sourceChapters)
        .andThen(Single.just(chaptersToNotify))
    }
  }

  // Checks if the chapter in db needs update
  private fun metaChanged(dbChapter: Chapter, sourceChapter: Chapter): Boolean {
    return dbChapter.scanlator != sourceChapter.scanlator ||
           dbChapter.name != sourceChapter.name ||
           dbChapter.dateUpload != sourceChapter.dateUpload ||
           dbChapter.number != sourceChapter.number
  }

}
