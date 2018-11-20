/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.data.catalog.prefs

import tachiyomi.core.prefs.Preference
import tachiyomi.core.prefs.PreferenceStore

class CatalogPreferences internal constructor(private val preferenceStore: PreferenceStore) {

  fun gridMode(): Preference<Boolean> {
    return preferenceStore.getBoolean("grid_mode", true)
  }

  fun lastListingUsed(sourceId: Long): Preference<Int> {
    return preferenceStore.getInt("last_listing_$sourceId", 0)
  }

}
