/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.core.prefs

import android.content.SharedPreferences

internal object StringAdapter : AndroidPreference.Adapter<String> {
  override fun get(key: String, preferences: SharedPreferences): String {
    return preferences.getString(key, null)!! // Not called unless key is present.
  }

  override fun set(key: String, value: String, editor: SharedPreferences.Editor) {
    editor.putString(key, value)
  }
}

internal object LongAdapter : AndroidPreference.Adapter<Long> {
  override fun get(key: String, preferences: SharedPreferences): Long {
    return preferences.getLong(key, 0)
  }

  override fun set(key: String, value: Long, editor: SharedPreferences.Editor) {
    editor.putLong(key, value)
  }
}

internal object IntAdapter : AndroidPreference.Adapter<Int> {
  override fun get(key: String, preferences: SharedPreferences): Int {
    return preferences.getInt(key, 0)
  }

  override fun set(key: String, value: Int, editor: SharedPreferences.Editor) {
    editor.putInt(key, value)
  }
}

internal object FloatAdapter : AndroidPreference.Adapter<Float> {
  override fun get(key: String, preferences: SharedPreferences): Float {
    return preferences.getFloat(key, 0f)
  }

  override fun set(key: String, value: Float, editor: SharedPreferences.Editor) {
    editor.putFloat(key, value)
  }
}

internal object BooleanAdapter : AndroidPreference.Adapter<Boolean> {
  override fun get(key: String, preferences: SharedPreferences): Boolean {
    return preferences.getBoolean(key, false)
  }

  override fun set(key: String, value: Boolean, editor: SharedPreferences.Editor) {
    editor.putBoolean(key, value)
  }
}

internal object StringSetAdapter : AndroidPreference.Adapter<Set<String>> {
  override fun get(key: String, preferences: SharedPreferences): Set<String> {
    return preferences.getStringSet(key, null)!! // Not called unless key is present.
  }

  override fun set(key: String, value: Set<String>, editor: SharedPreferences.Editor) {
    editor.putStringSet(key, value)
  }
}

internal class ObjectAdapter<T>(
  private val serializer: (T) -> String,
  private val deserializer: (String) -> T
) : AndroidPreference.Adapter<T> {

  override fun get(key: String, preferences: SharedPreferences): T {
    return deserializer(preferences.getString(key, null)!!) // Not called unless key is present.
  }

  override fun set(key: String, value: T, editor: SharedPreferences.Editor) {
    editor.putString(key, serializer(value))
  }

}
