package tachiyomi.prefs

import android.app.Application
import android.content.Context
import tachiyomi.core.prefs.SharedPreferencesStore
import toothpick.ProvidesSingletonInScope
import javax.inject.Inject
import javax.inject.Provider
import javax.inject.Singleton

@Singleton
@ProvidesSingletonInScope
internal class UiPreferencesProvider @Inject constructor(
  private val context: Application
) : Provider<UiPreferences> {

  override fun get(): UiPreferences {
    val sharedPreferences = context.getSharedPreferences("ui", Context.MODE_PRIVATE)
    val preferenceStore = SharedPreferencesStore(sharedPreferences)

    return UiPreferences(preferenceStore)
  }

}
