package tachiyomi.data.extension.api

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
import tachiyomi.data.extension.model.Extension

internal class ExtensionGithubApi(private val http: Http) {

  private val repoUrl = "https://raw.githubusercontent.com/inorichi/tachiyomi-extensions/repo"

  fun findExtensions(): Single<List<Extension.Available>> {
    val call = GET("$repoUrl/index.json")

    return http.defaultClient.newCall(call).asSingleSuccess()
      .map(::parseResponse)
  }

  private fun parseResponse(response: Response): List<Extension.Available> {
    val text = response.body()?.use { it.string() } ?: return emptyList()

    val json = Gson().fromJson<JsonArray>(text)

    return json.map { element ->
      val name = element["name"].string.substringAfter("Tachiyomi: ")
      val pkgName = element["pkg"].string
      val apkName = element["apk"].string
      val versionName = element["version"].string
      val versionCode = element["code"].int
      val lang = element["lang"].string
      val icon = "$repoUrl/icon/${apkName.replace(".apk", ".png")}"

      Extension.Available(name, pkgName, versionName, versionCode, lang, apkName, icon)
    }
  }

  fun getApkUrl(extension: Extension.Available): String {
    return "$repoUrl/apk/${extension.apkName}"
  }

}
