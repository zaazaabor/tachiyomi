package tachiyomi.data.extension.prefs

import android.app.Application
import android.content.Context
import tachiyomi.core.prefs.SharedPreferencesStore
import javax.inject.Inject
import javax.inject.Provider

internal class ExtensionPreferencesProvider @Inject constructor(
  private val context: Application
) : Provider<ExtensionPreferences> {

  override fun get(): ExtensionPreferences {
    val sharedPreferences = context.getSharedPreferences("extension", Context.MODE_PRIVATE)
    val preferenceStore = SharedPreferencesStore(sharedPreferences)

    return ExtensionPreferences(preferenceStore)
  }

}
