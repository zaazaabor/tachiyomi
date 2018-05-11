package tachiyomi.domain.chapter.repository

import io.reactivex.Completable
import io.reactivex.Flowable
import tachiyomi.domain.chapter.model.Chapter

interface ChapterRepository {

  fun getChapters(mangaId: Long): Flowable<List<Chapter>>

  fun getChapter(id: Long): Flowable<Chapter>

  fun saveChapters(chapters: List<Chapter>): Completable

  fun deleteChapter(id: Long): Completable

  fun deleteChapters(ids: Iterable<Long>)

}
