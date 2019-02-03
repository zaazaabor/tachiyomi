/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.core.http

import android.content.SharedPreferences

/**
 * An implementation of a [CookieStore] backed by a file managed through [SharedPreferences].
 */
internal class SharedPreferencesCookieStore(
  private val sharedPreferences: SharedPreferences
) : CookieStore {

  /**
   * Returns a map of all the cookies stored by domain.
   */
  override fun load(): Map<String, Set<String>> {
    @Suppress("UNCHECKED_CAST")
    return sharedPreferences.all as Map<String, Set<String>>
  }

  /**
   * Updates the cookies stored for this [domain] with the provided by [cookies].
   */
  override fun update(domain: String, cookies: Set<String>) {
    sharedPreferences.edit().putStringSet(domain, cookies).apply()
  }

  /**
   * Clears all the cookies saved in this store.
   */
  override fun clear() {
    sharedPreferences.edit().clear().apply()
  }

}
