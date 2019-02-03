/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.ui.presenter

import androidx.annotation.CallSuper
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import timber.log.Timber

abstract class BasePresenter {

  val disposables = CompositeDisposable()

  @CallSuper
  open fun destroy() {
    disposables.dispose()
  }

  fun <T> Flowable<T>.logOnNext(): Flowable<T> {
    return doOnNext { Timber.d(it.toString()) }
  }

  fun <T> Observable<T>.logOnNext(): Observable<T> {
    return doOnNext { Timber.d(it.toString()) }
  }

}
