package tachiyomi.core.prefs

interface PreferenceStore {

  fun getString(key: String): Preference<String>

  fun getLong(key: String): Preference<Long>

  fun getInt(key: String): Preference<Int>

  fun getFloat(key: String): Preference<Float>

  fun getDouble(key: String): Preference<Double>

  fun getBoolean(key: String): Preference<Boolean>

  fun getStringSet(key: String): Preference<Set<String>>
}
