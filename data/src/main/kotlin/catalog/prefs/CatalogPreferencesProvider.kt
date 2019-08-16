/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.data.catalog.prefs

import android.app.Application
import android.content.Context
import tachiyomi.core.prefs.AndroidPreferenceStore
import javax.inject.Inject
import javax.inject.Provider

internal class CatalogPreferencesProvider @Inject constructor(
  private val context: Application
) : Provider<CatalogPreferences> {

  override fun get(): CatalogPreferences {
    val sharedPreferences = context.getSharedPreferences("catalog", Context.MODE_PRIVATE)
    val preferenceStore = AndroidPreferenceStore(sharedPreferences)

    return CatalogPreferences(preferenceStore)
  }

}
