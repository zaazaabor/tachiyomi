/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.domain.library.updater

import io.reactivex.Completable
import io.reactivex.Single

interface LibraryUpdater {

  fun enqueue(categoryId: Long, target: Target, completable: Completable): Single<QueueResult>

  fun cancel(categoryId: Long, target: Target)

  fun schedule(categoryId: Long, target: Target, timeInHours: Int)

  fun unschedule(categoryId: Long, target: Target)

  fun unscheduleAll(categoryId: Long)

  enum class Target {
    Chapters, Metadata;
  }

  sealed class QueueResult {
    abstract val work: Completable

    data class Executing(override val work: Completable) : QueueResult()
    data class Queued(override val work: Completable) : QueueResult()
    object AlreadyEnqueued : QueueResult() {
      override val work = Completable.complete()
    }
  }

}
