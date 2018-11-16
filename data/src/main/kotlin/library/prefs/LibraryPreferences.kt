package tachiyomi.data.library.prefs

import tachiyomi.core.prefs.Preference
import tachiyomi.core.prefs.PreferenceStore
import tachiyomi.domain.library.model.LibraryFilter
import tachiyomi.domain.library.model.LibrarySort
import tachiyomi.domain.library.model.deserialize
import tachiyomi.domain.library.model.deserializeList
import tachiyomi.domain.library.model.serialize

class LibraryPreferences internal constructor(private val preferenceStore: PreferenceStore) {

  fun lastSorting(): Preference<LibrarySort> {
    return preferenceStore.getObject<LibrarySort>(
      key = "last_sorting",
      defaultValue = LibrarySort.Title(true),
      serializer = { it.serialize() },
      deserializer = { LibrarySort.deserialize(it) }
    )
  }

  fun filters(): Preference<List<LibraryFilter>> {
    return preferenceStore.getObject(
      key = "filters",
      defaultValue = emptyList(),
      serializer = { it.serialize() },
      deserializer = { LibraryFilter.deserializeList(it) }
    )
  }

}
