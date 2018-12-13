/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.app.initializers

import android.os.Looper
import io.reactivex.android.plugins.RxAndroidPlugins
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.exceptions.UndeliverableException
import io.reactivex.plugins.RxJavaPlugins
import timber.log.Timber
import java.io.IOException
import java.net.SocketException
import javax.inject.Inject

class RxJavaInitializer @Inject constructor() {

  init {
    // Init async scheduler
    val asyncMainThreadScheduler = AndroidSchedulers.from(Looper.getMainLooper(), true)
    RxAndroidPlugins.setInitMainThreadSchedulerHandler { asyncMainThreadScheduler }

    // Install default error handler
    RxJavaPlugins.setErrorHandler { e ->
      var error = e
      if (error is UndeliverableException) {
        error = error.cause
      }
      when (error) {
        is InterruptedException, is IOException, is SocketException -> {
          // fine, irrelevant network problem or API that throws on cancellation or
          // some blocking code was interrupted by a dispose call
        }
        is NullPointerException, is IllegalArgumentException, is IllegalStateException -> {
          // that's likely a bug in the application or in a custom operator (if IllegalState)
          Thread.currentThread().uncaughtExceptionHandler.uncaughtException(Thread.currentThread(),
            error)
        }
        else -> {
          Timber.w(error, "Undeliverable exception received, not sure what to do")
        }
      }
    }
  }

}
