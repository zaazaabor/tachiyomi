/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.domain.sync.api

import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import io.reactivex.Single
import okhttp3.Credentials
import okhttp3.MediaType
import okhttp3.Request
import okhttp3.RequestBody
import tachiyomi.core.http.Http
import tachiyomi.core.http.asSingle
import tachiyomi.domain.sync.prefs.SyncPreferences
import javax.inject.Inject

class SyncAPI @Inject constructor(
  http: Http,
  private val store: SyncPreferences,
  private val device: SyncDevice
) {

  private val client = http.defaultClient
  private val gson = GsonBuilder().create()
  private val jsonMediaType by lazy { MediaType.parse("application/json; charset=utf-8") }

  private val addressPref = store.address()
  private val tokenPref = store.token()

  val address get() = addressPref.get()
  val token get() = tokenPref.get()

  fun login(address: String, username: String, password: String): Single<LoginResult> {
    data class Response(val secret: String)

    val credentials = Credentials.basic(username, password)

    val json = JsonObject().apply {
      addProperty("deviceId", device.getId())
      addProperty("deviceName", device.getName())
      addProperty("platform", device.getPlatform())
    }

    val request = Request.Builder()
      .url("$address/api/v3/auth/tokens")
      .post(RequestBody.create(jsonMediaType, json.toString()))
      .addHeader("Authorization", credentials)
      .build()

    return client.newCall(request).asSingle()
      .map { response ->
        response.use {
          if (response.code() == 200) {
            val body = response.body()?.string() ?: throw Exception("Failed to read body")
            val responseBody = gson.fromJson(body, Response::class.java)
            LoginResult.Token(responseBody.secret)
          } else {
            LoginResult.InvalidCredentials
          }
        }
      }
      .onErrorReturn(LoginResult::NetworkError)
  }

}
