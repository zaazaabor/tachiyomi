package tachiyomi.core.http

import android.app.Application
import android.content.Context
import okhttp3.Cache
import toothpick.ProvidesSingletonInScope
import java.io.File
import javax.inject.Inject
import javax.inject.Provider
import javax.inject.Singleton

/**
 * Provider to instantiate an [Http] class. The required dependencies to create the instance are
 * also provided through constructor injection.
 */
@Singleton
@ProvidesSingletonInScope
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
