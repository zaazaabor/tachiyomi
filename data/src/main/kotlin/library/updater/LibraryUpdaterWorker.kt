/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.data.library.updater

import android.content.Context
import androidx.concurrent.futures.ResolvableFuture
import androidx.work.ListenableWorker
import androidx.work.WorkerParameters
import com.google.common.util.concurrent.ListenableFuture
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import tachiyomi.core.di.AppScope
import tachiyomi.core.rx.CoroutineDispatchers
import tachiyomi.domain.library.interactor.UpdateLibraryCategory
import timber.log.Timber
import timber.log.debug
import javax.inject.Inject

class LibraryUpdaterWorker(
  context: Context,
  params: WorkerParameters
) : ListenableWorker(context, params) {

  @Inject
  lateinit var dispatchers: CoroutineDispatchers

  @Inject
  lateinit var updater: UpdateLibraryCategory

  init {
    AppScope.inject(this)
  }

  private val categoryId = params.inputData.getLong(CATEGORY_KEY, -1)

  override fun startWork(): ListenableFuture<Result> {
    val future = ResolvableFuture.create<Result>()
    Timber.debug { "Starting scheduled update for category $categoryId" }
    if (categoryId == -1L) {
      future.set(Result.failure())
      return future
    }

    GlobalScope.launch(dispatchers.io) {
      updater.execute(categoryId).awaitWork()
      future.set(Result.success())
    }
    return future
  }

  companion object {
    const val CATEGORY_KEY = "category_id"
  }

}
