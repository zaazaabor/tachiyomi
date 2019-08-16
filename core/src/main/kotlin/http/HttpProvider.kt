/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.core.http

import android.app.Application
import android.content.Context
import okhttp3.Cache
import java.io.File
import javax.inject.Inject
import javax.inject.Provider

/**
 * Provider to instantiate an [Http] class. The required dependencies to create the instance are
 * also provided through constructor injection.
 */
internal class HttpProvider @Inject constructor(
  private val context: Application,
  private val jsFactory: JSFactory
) : Provider<Http> {

  /**
   * Returns a new instance of [Http] providing it a [Cache], a [CookieManager] and a [JSFactory].
   */
  override fun get(): Http {
    val cacheDir = File(context.cacheDir, "network_cache")
    val cacheSize = 15L * 1024 * 1024
    val cache = Cache(cacheDir, cacheSize)
    val cookieManager = CookieManager(SharedPreferencesCookieStore(
      context.getSharedPreferences("cookie_store", Context.MODE_PRIVATE)
    ))
    return Http(cache, cookieManager, jsFactory)
  }
}
