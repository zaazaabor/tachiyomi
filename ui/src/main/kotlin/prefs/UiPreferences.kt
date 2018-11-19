package tachiyomi.prefs

import tachiyomi.core.prefs.Preference
import tachiyomi.core.prefs.PreferenceStore

class UiPreferences internal constructor(private val preferenceStore: PreferenceStore) {

  fun useDrawer(): Preference<Boolean> {
    return preferenceStore.getBoolean("use_drawer", false)
  }

}
