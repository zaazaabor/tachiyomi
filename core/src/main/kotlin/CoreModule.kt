/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.core

import tachiyomi.core.di.bindProvider
import tachiyomi.core.di.bindTo
import tachiyomi.core.os.AndroidAppState
import tachiyomi.core.os.AppState
import tachiyomi.core.rx.CoroutineDispatchers
import tachiyomi.core.rx.RxSchedulers
import tachiyomi.core.rx.SchedulersModule
import toothpick.config.Module

object CoreModule : Module() {

  init {
    bindTo<AppState, AndroidAppState>().singletonInScope()
    bindProvider<RxSchedulers, SchedulersModule.RxSchedulersProvider>()
    bindProvider<CoroutineDispatchers, SchedulersModule.CoroutineDispatchersProvider>()
  }

}
