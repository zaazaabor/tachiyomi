/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.core.rx

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.rx2.asCoroutineDispatcher
import toothpick.ProvidesSingletonInScope
import javax.inject.Inject
import javax.inject.Provider
import javax.inject.Singleton

@Singleton
@ProvidesSingletonInScope
internal class CoroutineDispatchersProvider @Inject constructor(
  private val rxSchedulers: RxSchedulers
) : Provider<CoroutineDispatchers> {

  override fun get(): CoroutineDispatchers {
    return CoroutineDispatchers(
      io = rxSchedulers.io.asCoroutineDispatcher(),
      computation = rxSchedulers.computation.asCoroutineDispatcher(),
      single = rxSchedulers.single.asCoroutineDispatcher(),
      main = Dispatchers.Main
    )
  }

}
