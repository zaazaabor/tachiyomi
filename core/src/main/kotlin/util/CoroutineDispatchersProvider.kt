/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.core.util

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.asCoroutineDispatcher
import toothpick.ProvidesSingletonInScope
import java.util.concurrent.Executors
import javax.inject.Provider
import javax.inject.Singleton

@Singleton
@ProvidesSingletonInScope
internal class CoroutineDispatchersProvider : Provider<CoroutineDispatchers> {

  override fun get(): CoroutineDispatchers {
    return CoroutineDispatchers(
      io = Dispatchers.IO,
      computation = Dispatchers.Default,
      single = Executors.newSingleThreadExecutor().asCoroutineDispatcher(),
      main = Dispatchers.Main
    )
  }

}
