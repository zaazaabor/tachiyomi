package tachiyomi.core.http

/**
 * An interface for a persistent cookie store.
 */
interface CookieStore {

  /**
   * Returns a map of all the cookies stored by domain.
   */
  fun load(): Map<String, Set<String>>

  /**
   * Updates the cookies stored for this [domain] with the provided by [cookies].
   */
  fun update(domain: String, cookies: Set<String>)

  /**
   * Clears all the cookies saved in this store.
   */
  fun clear()
}
