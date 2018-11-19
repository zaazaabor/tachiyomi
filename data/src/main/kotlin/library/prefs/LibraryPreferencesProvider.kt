package tachiyomi.data.library.prefs

import android.app.Application
import android.content.Context
import tachiyomi.core.prefs.SharedPreferencesStore
import toothpick.ProvidesSingletonInScope
import javax.inject.Inject
import javax.inject.Provider
import javax.inject.Singleton

@Singleton
@ProvidesSingletonInScope
internal class LibraryPreferencesProvider @Inject constructor(
  private val context: Application
) : Provider<LibraryPreferences> {

  override fun get(): LibraryPreferences {
    val sharedPreferences = context.getSharedPreferences("library", Context.MODE_PRIVATE)
    val preferenceStore = SharedPreferencesStore(sharedPreferences)

    return LibraryPreferences(preferenceStore)
  }

}
