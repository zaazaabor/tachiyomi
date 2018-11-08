package tachiyomi.data.catalog.prefs

import android.app.Application
import android.content.Context
import tachiyomi.core.prefs.SharedPreferencesStore
import javax.inject.Inject
import javax.inject.Provider

internal class CatalogPreferencesProvider @Inject constructor(
  private val context: Application
) : Provider<CatalogPreferences> {

  override fun get(): CatalogPreferences {
    val sharedPreferences = context.getSharedPreferences("catalog", Context.MODE_PRIVATE)
    val preferenceStore = SharedPreferencesStore(sharedPreferences)

    return CatalogPreferences(preferenceStore)
  }

}
