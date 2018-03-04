package tachiyomi.core.http

import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl

class CookieManager(private val store: CookieStore) : CookieJar {

  private val cookieMap = hashMapOf<String, List<Cookie>>()

  init {
    for ((domain, cookies) in store.load()) {
      try {
        val url = HttpUrl.parse("http://$domain") ?: continue
        val nonExpiredCookies = cookies.mapNotNull { Cookie.parse(url, it) }
          .filter { !it.hasExpired() }
        cookieMap[domain] = nonExpiredCookies
      } catch (e: Exception) {
        // Ignore
      }
    }
  }

  @Synchronized
  override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
    val domain = url.host()

    // Append or replace the cookies for this domain.
    val cookiesForDomain = cookieMap[domain].orEmpty().toMutableList()
    for (cookie in cookies) {
      // Find a cookie with the same name. Replace it if found, otherwise add a new one.
      val pos = cookiesForDomain.indexOfFirst { it.name() == cookie.name() }
      if (pos == -1) {
        cookiesForDomain.add(cookie)
      } else {
        cookiesForDomain[pos] = cookie
      }
    }
    cookieMap[domain] = cookiesForDomain

    // Get cookies to be stored
    val newValues = cookiesForDomain.asSequence()
      .filter { it.persistent() && !it.hasExpired() }
      .map(Cookie::toString)
      .toSet()

    store.update(domain, newValues)
  }

  @Synchronized
  fun clear() {
    cookieMap.clear()
    store.clear()
  }

  override fun loadForRequest(url: HttpUrl): List<Cookie> {
    val cookies = cookieMap[url.host()].orEmpty().filter { !it.hasExpired() }
    return if (url.isHttps) {
      cookies
    } else {
      cookies.filter { !it.secure() }
    }
  }

  private fun Cookie.hasExpired() = System.currentTimeMillis() >= expiresAt()
}
