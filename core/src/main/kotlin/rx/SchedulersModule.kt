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
import tachiyomi.core.di.bindProvider
import toothpick.ProvidesSingletonInScope
import toothpick.config.Module
import javax.inject.Inject
import javax.inject.Provider
import javax.inject.Singleton

object SchedulersModule : Module() {

  init {
    bindProvider<RxSchedulers, RxSchedulersProvider>()
    bindProvider<CoroutineDispatchers, CoroutineDispatchersProvider>()
  }

  @Singleton
  @ProvidesSingletonInScope
  internal class RxSchedulersProvider : Provider<RxSchedulers> {

    override fun get(): RxSchedulers {
      return RxSchedulers(
        io = Schedulers.io(),
        computation = Schedulers.computation(),
        single = Schedulers.single(),
        main = AndroidSchedulers.mainThread()
      )
    }

  }

  @Singleton
  @ProvidesSingletonInScope
  internal class CoroutineDispatchersProvider @Inject constructor(
    private val rxSchedulers: RxSchedulers
  ) : Provider<CoroutineDispatchers> {

    override fun get(): CoroutineDispatchers {
      return CoroutineDispatchers(
        io = rxSchedulers.io.asCoroutineDispatcher(),
        computation = rxSchedulers.computation.asCoroutineDispatcher(),
        main = Dispatchers.Main
      )
    }

  }

}
