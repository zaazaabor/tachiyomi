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
import com.github.salomonbrys.kotson.string
import com.google.gson.Gson
import com.google.gson.JsonArray
import io.reactivex.Single
import okhttp3.Response
import tachiyomi.core.http.GET
import tachiyomi.core.http.Http
import tachiyomi.core.http.asSingleSuccess
import tachiyomi.domain.catalog.model.Catalog
import timber.log.Timber
import javax.inject.Inject

internal class CatalogGithubApi @Inject constructor(private val http: Http) {

  private val gson = Gson() // TODO consider injecting a default instance

  // TODO create a new branch for 1.x extensions
  private val repoUrl = "https://raw.githubusercontent.com/inorichi/tachiyomi-extensions/repo"

  fun findCatalogs(): Single<List<Catalog.Available>> {
    val call = GET("$repoUrl/index.json")

    return http.defaultClient.newCall(call).asSingleSuccess()
      .map(::parseResponse)
      .doOnError { Timber.w(it) }
  }

  private fun parseResponse(response: Response): List<Catalog.Available> {
    val text = response.body()?.use { it.string() } ?: return emptyList()

    val json = gson.fromJson<JsonArray>(text)

    return json.map { element ->
      val name = element["name"].string.substringAfter("Tachiyomi: ")
      val pkgName = element["pkg"].string
      val apkName = element["apk"].string
      val versionName = element["version"].string
      val versionCode = element["code"].int
      val lang = element["lang"].string
      val icon = "$repoUrl/icon/${apkName.replace(".apk", ".png")}"

      Catalog.Available(name, pkgName, versionName, versionCode, lang, apkName, icon)
    }
  }

  fun getApkUrl(extension: Catalog.Available): String {
    return "$repoUrl/apk/${extension.apkName}"
  }
}
