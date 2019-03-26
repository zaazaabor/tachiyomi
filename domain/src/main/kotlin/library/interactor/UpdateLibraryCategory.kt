/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.domain.library.interactor

import io.reactivex.Observable
import io.reactivex.Single
import tachiyomi.domain.library.repository.CategoryRepository
import tachiyomi.domain.library.updater.LibraryUpdater
import tachiyomi.domain.library.updater.LibraryUpdaterNotification
import tachiyomi.domain.manga.interactor.SyncChaptersFromSource
import timber.log.Timber
import timber.log.debug
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class UpdateLibraryCategory @Inject constructor(
  private val findLibraryCategory: FindLibraryCategory,
  private val syncChaptersFromSource: SyncChaptersFromSource,
  private val notifier: LibraryUpdaterNotification,
  private val libraryUpdater: LibraryUpdater,
  private val categoryRepository: CategoryRepository
) {

  fun interact(categoryId: Long): Single<LibraryUpdater.QueueResult> = Single.defer {
    val completable = findLibraryCategory.interact(categoryId)
      .flatMapCompletable { mangas ->
        var progress = 0
        val total = mangas.size

        Observable.fromIterable(mangas)
          .concatMapSingle { manga ->
            notifier.showProgress(manga, progress++, total)
            syncChaptersFromSource.interact(manga)
          }
          .ignoreElements()
      }
      .doOnSubscribe {
        Timber.debug { "Updating category $categoryId" }
        notifier.start()
      }
      .doFinally {
        Timber.debug { "Finished updating category $categoryId" }
        notifier.end()
      }

    libraryUpdater.enqueue(categoryId, LibraryUpdater.Target.Chapters, completable)
      .doOnSuccess { result -> rescheduleCategory(result, categoryId) }
  }

  private fun rescheduleCategory(result: LibraryUpdater.QueueResult, categoryId: Long) {
    if (result == LibraryUpdater.QueueResult.AlreadyEnqueued) {
      // Nothing to do. The other running operation will reschedule
      return
    }

    val rescheduleOperation = categoryRepository.find(categoryId)
      .doOnSuccess { category ->
        if (category.updateInterval > 0) {
          Timber.debug { "Rescheduling category $categoryId" }
          libraryUpdater.schedule(categoryId, LibraryUpdater.Target.Chapters,
            category.updateInterval)
        }
      }

    result.work
      // Let the work be marked as completed, 1ms should be enough (we only need to run async)
      .delay(1, TimeUnit.MILLISECONDS)
      .andThen(rescheduleOperation)
      // We have to run on another subscription
      .subscribe()
  }

}
