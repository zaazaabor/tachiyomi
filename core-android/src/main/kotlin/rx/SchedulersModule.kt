/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.core.rx

import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.rx2.asCoroutineDispatcher
import tachiyomi.core.di.bindInstance
import toothpick.config.Module

object SchedulersModule : Module() {

  init {
    val rxSchedulers = RxSchedulers(
      io = Schedulers.io(),
      computation = Schedulers.computation(),
      main = AndroidSchedulers.mainThread()
    )
    val coroutineDispatchers = CoroutineDispatchers(
      io = rxSchedulers.io.asCoroutineDispatcher(),
      computation = rxSchedulers.computation.asCoroutineDispatcher(),
      main = Dispatchers.Main
    )

    bindInstance(rxSchedulers)
    bindInstance(coroutineDispatchers)
  }

}
