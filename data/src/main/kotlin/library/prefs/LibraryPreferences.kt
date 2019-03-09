/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.data.library.prefs

import tachiyomi.core.prefs.Preference
import tachiyomi.core.prefs.PreferenceStore
import tachiyomi.domain.category.model.Category
import tachiyomi.domain.library.model.LibraryFilter
import tachiyomi.domain.library.model.LibrarySort
import tachiyomi.domain.library.model.deserialize
import tachiyomi.domain.library.model.deserializeList
import tachiyomi.domain.library.model.serialize
import tachiyomi.domain.library.prefs.LibraryPreferences

class LibraryPreferencesImpl internal constructor(
  private val preferenceStore: PreferenceStore
) : LibraryPreferences {

  override fun lastSorting(): Preference<LibrarySort> {
    return preferenceStore.getObject<LibrarySort>(
      key = "last_sorting",
      defaultValue = LibrarySort.Title(true),
      serializer = { it.serialize() },
      deserializer = { LibrarySort.deserialize(it) }
    )
  }

  override fun filters(): Preference<List<LibraryFilter>> {
    return preferenceStore.getObject(
      key = "filters",
      defaultValue = emptyList(),
      serializer = { it.serialize() },
      deserializer = { LibraryFilter.deserializeList(it) }
    )
  }

  override fun lastUsedCategory(): Preference<Long> {
    return preferenceStore.getLong("last_used_category", Category.ALL_ID)
  }

  override fun defaultCategory(): Preference<Long> {
    return preferenceStore.getLong("default_category", Category.UNCATEGORIZED_ID)
  }

}
