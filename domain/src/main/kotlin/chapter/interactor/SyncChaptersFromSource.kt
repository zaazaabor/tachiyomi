/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.domain.chapter.interactor

import io.reactivex.Completable
import io.reactivex.Single
import tachiyomi.core.db.Transaction
import tachiyomi.domain.chapter.model.Chapter
import tachiyomi.domain.chapter.repository.ChapterRepository
import tachiyomi.domain.chapter.util.ChapterRecognition
import tachiyomi.domain.manga.model.MangaBase
import tachiyomi.domain.source.SourceManager
import tachiyomi.source.model.MangaInfo
import javax.inject.Inject
import javax.inject.Provider

class SyncChaptersFromSource @Inject constructor(
  private val chapterRepository: ChapterRepository,
  private val sourceManager: SourceManager,
  private val transactions: Provider<Transaction>
) {

  data class Diff(
    val added: List<Chapter> = emptyList(),
    val deleted: List<Chapter> = emptyList(),
    val updated: List<Chapter> = emptyList()
  )

  fun interact(manga: MangaBase): Single<Diff> = Single.defer {
    val mangaInfo = MangaInfo(manga.key, manga.title)
    val source = sourceManager.get(manga.sourceId)!!
    val rawSourceChapters = source.fetchChapterList(mangaInfo)

    if (rawSourceChapters.isEmpty()) {
      return@defer Single.error<Diff>(Exception("No chapters found"))
    }

    // Chapters from db.
    chapterRepository.findForManga(manga.id).flatMap { dbChapters ->

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
          number = meta.number.takeIf { it >= 0f } ?: ChapterRecognition.parse(meta, manga.title,
            source),
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
        return@flatMap Single.just(Diff())
      }

      val diff = Diff(toAdd, toDelete, toUpdate)

      // To avoid notifying chapters that changed the key, we emit downstream a modified list that
      // ignores thoses chapters
      val toDeleteNumbers = toDelete.asSequence()
        .mapNotNull { chapter -> if (chapter.isRecognizedNumber) chapter.number else null }
        .toSet()

      val chaptersToNotify = toAdd.toList() - toAdd.filter { it.number in toDeleteNumbers }
      val notifyDiff = Diff(chaptersToNotify, toDelete, toUpdate)

      val chaptersToSave = diff.added + diff.updated
      val saveCompletable = if (chaptersToSave.isNotEmpty()) {
        chapterRepository.save(chaptersToSave)
      } else {
        Completable.complete()
      }
      val deleteCompletable = if (diff.deleted.isNotEmpty()) {
        chapterRepository.delete(diff.deleted.map { it.id })
      } else {
        Completable.complete()
      }

      transactions.get()
        .withCompletable {
          saveCompletable
            .andThen(deleteCompletable)
            .andThen(chapterRepository.saveNewOrder(sourceChapters))
        }
        .andThen(Single.just(notifyDiff))
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
