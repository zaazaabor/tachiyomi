package tachiyomi.core.http

import okhttp3.Cache
import okhttp3.OkHttpClient
import tachiyomi.core.js.JSFactory

class Http(cache: Cache, cookieManager: CookieManager, jsFactory: JSFactory) {

  val defaultClient = OkHttpClient.Builder()
    .cookieJar(cookieManager)
    .cache(cache)
    .build()

  val cloudflareClient = defaultClient.newBuilder()
    .addInterceptor(CloudflareInterceptor(jsFactory))
    .build()
}
