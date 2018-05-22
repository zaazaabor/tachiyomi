package tachiyomi.domain.chapter.repository

import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Maybe
import io.reactivex.Single
import tachiyomi.core.rx.RxOptional
import tachiyomi.domain.chapter.interactor.SyncChaptersFromSource
import tachiyomi.domain.chapter.model.Chapter

interface ChapterRepository {

  fun subscribeChapters(mangaId: Long): Flowable<List<Chapter>>

  fun subscribeChapter(chapterId: Long): Flowable<RxOptional<Chapter>>

  fun getChapters(mangaId: Long): Single<List<Chapter>>

  fun getChapter(chapterId: Long): Maybe<Chapter>

  fun getChapter(chapterKey: String, mangaId: Long): Maybe<Chapter>

  fun saveChapters(chapters: List<Chapter>): Completable

  fun deleteChapter(chapterId: Long): Completable

  fun deleteChapters(chapterIds: List<Long>): Completable

  fun syncChapters(diff: SyncChaptersFromSource.Diff, sourceChapters: List<Chapter>): Completable

  fun syncChapter(chapter: Chapter, sourceChapters: List<Chapter>): Completable

}
