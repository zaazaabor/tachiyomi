package tachiyomi.data.catalog.prefs

import tachiyomi.core.prefs.Preference
import tachiyomi.core.prefs.PreferenceStore

class CatalogPreferences internal constructor(private val preferenceStore: PreferenceStore) {

  fun gridMode(): Preference<Boolean> {
    return preferenceStore.getBoolean("grid_mode", true)
  }

}
