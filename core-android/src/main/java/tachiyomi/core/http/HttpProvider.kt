package tachiyomi.core.http

import android.app.Application
import android.content.Context
import okhttp3.Cache
import tachiyomi.core.js.JSFactory
import java.io.File
import javax.inject.Inject
import javax.inject.Provider

internal class HttpProvider @Inject constructor(
  private val context: Application,
  private val jsFactory: JSFactory
) : Provider<Http> {

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
