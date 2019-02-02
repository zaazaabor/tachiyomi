/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.di

import tachiyomi.core.di.bindProvider
import tachiyomi.core.di.bindTo
import tachiyomi.glide.GlideInitCallback
import tachiyomi.glide.TachiyomiGlideInitCallback
import tachiyomi.prefs.UiPreferences
import tachiyomi.prefs.UiPreferencesProvider
import toothpick.config.Module

object UiModule : Module() {

  init {
    bindProvider<UiPreferences, UiPreferencesProvider>()
    bindTo<GlideInitCallback, TachiyomiGlideInitCallback>().singletonInScope()
  }

}
