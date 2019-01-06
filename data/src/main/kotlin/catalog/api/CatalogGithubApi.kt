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
import java.security.MessageDigest
import javax.inject.Inject

internal class CatalogGithubApi @Inject constructor(private val http: Http) {

  private val gson = Gson() // TODO consider injecting a default instance

  // TODO create a new branch for 1.x extensions
  private val repoUrl = "https://raw.githubusercontent.com/inorichi/tachiyomi-extensions/repo"

  fun findCatalogs(): Single<List<CatalogRemote>> {
    val call = GET("$repoUrl/index.json")

    return http.defaultClient.newCall(call).asSingleSuccess()
      .map(::parseResponse)
      .doOnError { Timber.w(it) }
  }

  private fun parseResponse(response: Response): List<CatalogRemote> {
    val text = response.body()?.use { it.string() } ?: return emptyList()

    val json = gson.fromJson<JsonArray>(tmpJson) // TODO

    return json.map { element ->
      val name = element["name"].string.substringAfter("Tachiyomi: ")
      val pkgName = element["pkg"].string
      val apkName = element["apk"].string
      val versionName = element["version"].string
      val versionCode = element["code"].int
      val lang = element["lang"].string
      val icon = "$repoUrl/icon/${apkName.replace(".apk", ".png")}"
      val sourceId = getDefaultId(name, lang)
      val description = element["description"].string
      val nsfw = element["nsfw"].nullBool ?: false

      CatalogRemote(name, description, sourceId, pkgName, versionName, versionCode, lang, apkName,
        icon, nsfw)
    }
  }

  // TODO id must be provided in JSON
  private fun getDefaultId(name: String, lang: String): Long {
    val key = "${name.toLowerCase()}/$lang/1"
    val bytes = MessageDigest.getInstance("MD5").digest(key.toByteArray())
    return (0..7).map { bytes[it].toLong() and 0xff shl 8 * (7 - it) }
      .reduce(Long::or) and Long.MAX_VALUE
  }

  fun getApkUrl(extension: CatalogRemote): String {
    return "$repoUrl/apk/${extension.apkName}"
  }
}

private val tmpJson = """[
  {
    "name": "Tachiyomi: MangaDex",
    "id": 1,
    "pkg": "eu.kanade.tachiyomi.extension.all.mangadex",
    "apk": "tachiyomi-all.mangadex-v1.2.44.apk",
    "lang": "en",
    "code": 44,
    "version": "1.2.44",
    "description": "Highest quality and scanlator-approved source",
    "nsfw": false
  },
  {
    "name": "Tachiyomi: MangaDex",
    "id": 2,
    "pkg": "eu.kanade.tachiyomi.extension.all.mangadex",
    "apk": "tachiyomi-all.mangadex-v1.2.44.apk",
    "lang": "es",
    "code": 44,
    "version": "1.2.44",
    "description": "Highest quality and scanlator-approved source",
    "nsfw": false
  },
  {
    "name": "Tachiyomi: E-Hentai",
    "id": 3,
    "pkg": "eu.kanade.tachiyomi.extension.all.ehentai",
    "apk": "tachiyomi-all.ehentai-v1.0.1.apk",
    "lang": "en",
    "code": 1,
    "version": "1.0.1",
    "description": "",
    "nsfw": true
  },
  {
    "name": "Tachiyomi: Japscan",
    "id": 4,
    "pkg": "eu.kanade.tachiyomi.extension.fr.japscan",
    "apk": "tachiyomi-fr.japscan-v1.2.5.apk",
    "lang": "fr",
    "code": 5,
    "version": "1.2.5",
    "description": "",
    "nsfw": false
  },
  {
    "name": "Tachiyomi: WieManga",
    "id": 5,
    "pkg": "eu.kanade.tachiyomi.extension.de.wiemanga",
    "apk": "tachiyomi-de.wiemanga-v1.2.2.apk",
    "lang": "de",
    "code": 2,
    "version": "1.2.2",
    "description": "",
    "nsfw": false
  }
]"""
