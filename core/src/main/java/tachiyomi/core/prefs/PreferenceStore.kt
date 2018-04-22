package tachiyomi.core.prefs

/**
 * A wrapper around an application preferences store. Implementations of this interface should
 * persist these preferences on disk.
 */
interface PreferenceStore {

  /**
   * Returns an [String] preference for this [key].
   */
  fun getString(key: String): Preference<String>

  /**
   * Returns a [Long] preference for this [key].
   */
  fun getLong(key: String): Preference<Long>

  /**
   * Returns an [Int] preference for this [key].
   */
  fun getInt(key: String): Preference<Int>

  /**
   * Returns a [Float] preference for this [key].
   */
  fun getFloat(key: String): Preference<Float>

  /**
   * Returns a [Double] preference for this [key].
   */
  fun getDouble(key: String): Preference<Double>

  /**
   * Returns a [Boolean] preference for this [key].
   */
  fun getBoolean(key: String): Preference<Boolean>

  /**
   * Returns a [Set<String>] preference for this [key].
   */
  fun getStringSet(key: String): Preference<Set<String>>
}
