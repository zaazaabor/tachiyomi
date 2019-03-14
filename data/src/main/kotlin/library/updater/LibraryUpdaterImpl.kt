/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.data.library.updater

import androidx.work.Constraints
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import com.jakewharton.rxrelay2.PublishRelay
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.disposables.Disposable
import io.reactivex.observables.ConnectableObservable
import tachiyomi.core.rx.RxSchedulers
import tachiyomi.domain.library.updater.LibraryUpdater
import tachiyomi.domain.library.updater.LibraryUpdater.QueueResult
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class LibraryUpdaterImpl @Inject constructor(
  private val schedulers: RxSchedulers
) : LibraryUpdater {

  private val workManager = WorkManager.getInstance()

  private val queueSubject = PublishRelay.create<Operation>()

  private var queuedOperations = mutableSetOf<Operation>()

  init {
    queueSubject
      .concatMapCompletable { operation -> operation.connect() }
      .subscribe()
  }

  override fun enqueue(
    categoryId: Long,
    target: LibraryUpdater.Target,
    completable: Completable
  ): Single<QueueResult> {
    return Single.just(Unit)
      .observeOn(schedulers.single)
      .map {
        val operation = Operation(categoryId, target)
        if (operation in queuedOperations) {
          return@map QueueResult.AlreadyEnqueued
        }

        operation.setSource(completable
          .subscribeOn(schedulers.io)
          .observeOn(schedulers.single)
          .doFinally { queuedOperations.remove(operation) }
        )

        if (queuedOperations.isEmpty()) {
          enqueue(operation)
          QueueResult.Executing(operation.completable)
        } else {
          enqueue(operation)
          QueueResult.Queued(operation.completable)
        }
      }
  }

  private fun enqueue(operation: Operation) {
    if (queuedOperations.add(operation)) {
      queueSubject.accept(operation)
    }
  }

  override fun cancel(categoryId: Long, target: LibraryUpdater.Target) {
    Completable
      .fromAction {
        val operation = queuedOperations.find { it.categoryId == categoryId && it.target == target }
        operation?.cancel()
      }
      .subscribeOn(schedulers.single)
      .subscribe()
  }

  override fun schedule(categoryId: Long, target: LibraryUpdater.Target, timeInHours: Int) {
    val work = OneTimeWorkRequest.Builder(LibraryUpdaterWorker::class.java)
      .setInitialDelay(timeInHours.toLong(), TimeUnit.HOURS)
      .addTag(LIBRARY_UPDATER_TAG)
      .addTag(getCategoryTag(categoryId))
      .setConstraints(Constraints.Builder()
        .setRequiredNetworkType(NetworkType.CONNECTED)
        .build())
      .setInputData(Data.Builder()
        .putLong(LibraryUpdaterWorker.CATEGORY_KEY, categoryId)
        .build()
      )
      .build()

    val workName = getUniqueWorkName(categoryId, target)
    workManager.enqueueUniqueWork(workName, ExistingWorkPolicy.REPLACE, work)
  }

  override fun unschedule(categoryId: Long, target: LibraryUpdater.Target) {
    workManager.cancelUniqueWork(getUniqueWorkName(categoryId, target))
  }

  override fun unscheduleAll(categoryId: Long) {
    workManager.cancelAllWorkByTag(getCategoryTag(categoryId))
  }

  private fun getUniqueWorkName(categoryId: Long, target: LibraryUpdater.Target): String {
    return "library_${target.name}_$categoryId"
  }

  private fun getCategoryTag(categoryId: Long): String {
    return "library_category_$categoryId"
  }

  private data class Operation(val categoryId: Long, val target: LibraryUpdater.Target) {
    lateinit var completable: Completable
    private lateinit var source: ConnectableObservable<Any>
    private var disposable: Disposable? = null
    var isCancelled = false

    fun cancel() {
      isCancelled = true
      disposable?.dispose()
      disposable = null
    }

    fun connect(): Completable {
      if (isCancelled) return Completable.complete()
      disposable = source.connect()
      return completable
    }

    fun setSource(completable: Completable) {
      source = completable.toObservable<Any>().replay(1)
      this.completable = source.ignoreElements()
    }
  }

  private companion object {
    const val LIBRARY_UPDATER_TAG = "library_updater"
  }

}
