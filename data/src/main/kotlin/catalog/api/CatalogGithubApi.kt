/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.data.catalog.api

import com.github.salomonbrys.kotson.fromJson
import com.github.salomonbrys.kotson.get
import com.github.salomonbrys.kotson.int
import com.github.salomonbrys.kotson.long
import com.github.salomonbrys.kotson.nullBool
import com.github.salomonbrys.kotson.string
import com.google.gson.Gson
import com.google.gson.JsonArray
import io.reactivex.Single
import okhttp3.Response
import tachiyomi.core.http.GET
import tachiyomi.core.http.Http
import tachiyomi.core.http.asSingleSuccess
import tachiyomi.domain.catalog.model.CatalogRemote
import timber.log.Timber
import timber.log.warn
import javax.inject.Inject

internal class CatalogGithubApi @Inject constructor(private val http: Http) {

  private val gson = Gson() // TODO consider injecting a default instance

  // TODO create a new branch for 1.x extensions
//  private val repoUrl = "https://raw.githubusercontent.com/inorichi/tachiyomi-extensions/repo"
  private val repoUrl = "https://tachiyomi.kanade.eu/repo"

  fun findCatalogs(): Single<List<CatalogRemote>> {
    val call = GET("$repoUrl/index.min.json")

    return http.defaultClient.newCall(call).asSingleSuccess()
      .map(::parseResponse)
      .doOnError { Timber.warn(it) { it.message.orEmpty() } }
  }

  private fun parseResponse(response: Response): List<CatalogRemote> {
    val text = response.body()?.use { it.string() } ?: return emptyList()

    val json = gson.fromJson<JsonArray>(text)

    return json.map { element ->
      val name = element["name"].string
      val pkgName = element["pkg"].string
      val versionName = element["version"].string
      val versionCode = element["code"].int
      val lang = element["lang"].string
      val apkName = element["apk"].string
      val sourceId = element["id"].long
      val description = element["description"].string
      val nsfw = element["nsfw"].nullBool ?: false

      val apkUrl = "$repoUrl/apk/$apkName"
      val iconUrl = "$repoUrl/icon/${apkName.replace(".apk", ".png")}"

      CatalogRemote(name, description, sourceId, pkgName, versionName, versionCode, lang, apkUrl,
        iconUrl, nsfw)
    }
  }

}
