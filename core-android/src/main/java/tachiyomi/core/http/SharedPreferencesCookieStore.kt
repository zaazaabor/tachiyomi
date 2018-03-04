package tachiyomi.core.http

import android.content.SharedPreferences

internal class SharedPreferencesCookieStore(
  private val sharedPreferences: SharedPreferences
) : CookieStore {

  override fun load(): Map<String, Set<String>> {
    @Suppress("UNCHECKED_CAST")
    return sharedPreferences.all as Map<String, Set<String>>
  }

  override fun clear() {
    sharedPreferences.edit().clear().apply()
  }

  override fun update(domain: String, cookies: Set<String>) {
    sharedPreferences.edit().putStringSet(domain, cookies).apply()
  }

}
