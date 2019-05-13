/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.domain.library.prefs

import tachiyomi.core.prefs.Preference
import tachiyomi.domain.library.model.LibraryFilter
import tachiyomi.domain.library.model.LibrarySorting

interface LibraryPreferences {

  fun lastSorting(): Preference<LibrarySorting>

  fun filters(): Preference<List<LibraryFilter>>

  fun lastUsedCategory(): Preference<Long>

  fun defaultCategory(): Preference<Long>

  fun quickCategories(): Preference<Boolean>

}
