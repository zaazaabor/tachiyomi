package tachiyomi.di

import tachiyomi.prefs.UiPreferences
import tachiyomi.prefs.UiPreferencesProvider
import toothpick.config.Module

object UiModule : Module() {

  init {
    bind(UiPreferences::class.java).toProvider(UiPreferencesProvider::class.java)
  }

}
