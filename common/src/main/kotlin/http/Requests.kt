/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.core.http

import okhttp3.CacheControl
import okhttp3.FormBody
import okhttp3.Headers
import okhttp3.Request
import okhttp3.RequestBody
import java.util.concurrent.TimeUnit.MINUTES

private val DEFAULT_CACHE_CONTROL = CacheControl.Builder().maxAge(10, MINUTES).build()
private val DEFAULT_HEADERS = Headers.Builder().build()
private val DEFAULT_BODY: RequestBody = FormBody.Builder().build()

/**
 * Builds a HTTP GET request for the given [url], allowing optional [headers] and [cache] control.
 * Note this function does not execute the request.
 */
fun GET(
  url: String,
  headers: Headers = DEFAULT_HEADERS,
  cache: CacheControl = DEFAULT_CACHE_CONTROL
): Request {
  return Request.Builder()
    .url(url)
    .headers(headers)
    .cacheControl(cache)
    .build()
}

/**
 * Builds a HTTP POST request for the given [url], allowing optional [headers], [body] and [cache]
 * control.
 * Note this function does not execute the request.
 */
fun POST(
  url: String,
  headers: Headers = DEFAULT_HEADERS,
  body: RequestBody = DEFAULT_BODY,
  cache: CacheControl = DEFAULT_CACHE_CONTROL
): Request {
  return Request.Builder()
    .url(url)
    .post(body)
    .headers(headers)
    .cacheControl(cache)
    .build()
}
