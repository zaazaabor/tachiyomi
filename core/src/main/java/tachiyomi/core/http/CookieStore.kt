package tachiyomi.core.http

interface CookieStore {

  fun load(): Map<String, Set<String>>

  fun clear()

  fun update(domain: String, cookies: Set<String>)
}
