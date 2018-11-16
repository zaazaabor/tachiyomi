package tachiyomi.core.prefs

import android.content.SharedPreferences
import com.f2prateek.rx.preferences2.RxSharedPreferences

/**
 * An implementation of a [PreferenceStore] backed by Android's [SharedPreferences] which is
 * instantiated lazily. All the preferences are read and written from the given [sharedPreferences]
 * instance.
 *
 * Note: there should be only one instance of this class per shared preferences file.
 */
class LazySharedPreferencesStore(
  private val sharedPreferences: Lazy<SharedPreferences>
) : PreferenceStore {

  /**
   * Instance of [RxSharedPreferences] that supports observing for changes on preferences.
   */
  private val rxSharedPreferences by lazy { RxSharedPreferences.create(sharedPreferences.value) }

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

  /**
   * Returns preference of type [T] for this [key]. The [serializer] and [deserializer] function
   * must be provided.
   */
  override fun <T> getObject(
    key: String,
    defaultValue: T,
    serializer: (T) -> String,
    deserializer: (String) -> T
  ): Preference<T> {
    return SharedPreference(rxSharedPreferences.getObject(key, defaultValue,
      object : com.f2prateek.rx.preferences2.Preference.Converter<T> {
        override fun deserialize(serialized: String): T {
          return deserializer(serialized)
        }

        override fun serialize(value: T): String {
          return serializer(value)
        }
      }
    ))
  }

}
