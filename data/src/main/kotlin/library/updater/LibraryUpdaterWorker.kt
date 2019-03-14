/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.data.library.updater

import android.content.Context
import androidx.work.RxWorker
import androidx.work.WorkerParameters
import io.reactivex.Scheduler
import io.reactivex.Single
import tachiyomi.core.di.AppScope
import tachiyomi.core.rx.RxSchedulers
import tachiyomi.domain.library.interactor.UpdateLibraryCategory
import timber.log.Timber
import timber.log.debug
import javax.inject.Inject

class LibraryUpdaterWorker(
  context: Context,
  params: WorkerParameters
) : RxWorker(context, params) {

  @Inject
  lateinit var schedulers: RxSchedulers

  @Inject
  lateinit var updater: UpdateLibraryCategory

  init {
    AppScope.inject(this)
  }

  private val categoryId = params.inputData.getLong(CATEGORY_KEY, -1)

  override fun createWork() = Single.defer<Result> {
    Timber.debug { "Starting scheduled update for category $categoryId" }
    if (categoryId == -1L) {
      return@defer Single.just(Result.failure())
    }

    updater.interact(categoryId)
      .flatMapCompletable { it.work }
      .toSingle { Result.success() }
      .onErrorReturn { Result.failure() }
  }

  override fun getBackgroundScheduler(): Scheduler {
    return schedulers.single // Work is scheduled on an IO thread anyways
  }

  companion object {
    const val CATEGORY_KEY = "category_id"
  }

}
