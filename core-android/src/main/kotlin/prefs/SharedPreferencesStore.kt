package tachiyomi.core.prefs

import android.content.SharedPreferences
import com.f2prateek.rx.preferences2.RxSharedPreferences

/**
 * An implementation of a [PreferenceStore] backed by Android's [SharedPreferences]. All the
 * preferences are read and written from the given [sharedPreferences] instance.
 *
 * Note: there should be only one instance of this class per shared preferences file.
 */
class SharedPreferencesStore(
  private val sharedPreferences: SharedPreferences
) : PreferenceStore {

  /**
   * Instance of [RxSharedPreferences] that supports observing for changes on preferences.
   */
  private val rxSharedPreferences = RxSharedPreferences.create(sharedPreferences)

  /**
   * Returns an [String] preference for this [key].
   */
  override fun getString(key: String, defaultValue: String): Preference<String> {
    return SharedPreference(rxSharedPreferences.getString(key, defaultValue))
  }

  /**
   * Returns a [Long] preference for this [key].
   */
  override fun getLong(key: String, defaultValue: Long): Preference<Long> {
    return SharedPreference(rxSharedPreferences.getLong(key, defaultValue))
  }

  /**
   * Returns an [Int] preference for this [key].
   */
  override fun getInt(key: String, defaultValue: Int): Preference<Int> {
    return SharedPreference(rxSharedPreferences.getInteger(key, defaultValue))
  }

  /**
   * Returns a [Float] preference for this [key].
   */
  override fun getFloat(key: String, defaultValue: Float): Preference<Float> {
    return SharedPreference(rxSharedPreferences.getFloat(key, defaultValue))
  }

  /**
   * Returns a [Boolean] preference for this [key].
   */
  override fun getBoolean(key: String, defaultValue: Boolean): Preference<Boolean> {
    return SharedPreference(rxSharedPreferences.getBoolean(key, defaultValue))
  }

  /**
   * Returns a [Set<String>] preference for this [key].
   */
  override fun getStringSet(key: String, defaultValue: Set<String>): Preference<Set<String>> {
    return SharedPreference(rxSharedPreferences.getStringSet(key, defaultValue))
  }

}
