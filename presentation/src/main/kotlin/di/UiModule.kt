/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.ui.di

import tachiyomi.core.di.bindTo
import tachiyomi.domain.library.updater.LibraryUpdaterNotification
import tachiyomi.ui.glide.GlideInitCallback
import tachiyomi.ui.glide.TachiyomiGlideInitCallback
import tachiyomi.ui.library.LibraryUpdaterNotificationImpl
import toothpick.config.Module

object UiModule : Module() {

  init {
    bindTo<GlideInitCallback, TachiyomiGlideInitCallback>().singletonInScope()
    bindTo<LibraryUpdaterNotification, LibraryUpdaterNotificationImpl>().singletonInScope()
  }

}
