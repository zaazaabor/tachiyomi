package tachiyomi.core.http

import okhttp3.Cache
import okhttp3.OkHttpClient

/**
 * This singleton contains the HTTP clients available to the application. It receives a [cache] for
 * temporarily storing requests, a [cookieManager] for cookies which are also persisted across
 * application restarts and a [jsFactory] in case any client needs to execute JavaScript code.
 */
class Http(cache: Cache, cookieManager: CookieManager, jsFactory: JSFactory) {

  /**
   * This is the client that will be used by default for every request.
   */
  val defaultClient = OkHttpClient.Builder()
    .cookieJar(cookieManager)
    .cache(cache)
    .build()

  /**
   * This client should be used for sites that need to bypass Cloudflare.
   */
  val cloudflareClient = defaultClient.newBuilder()
    .addInterceptor(CloudflareInterceptor(jsFactory))
    .build()
}
