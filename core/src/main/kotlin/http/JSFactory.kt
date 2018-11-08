package tachiyomi.core.http

/**
 * A factory for creating [JS] instances.
 */
interface JSFactory {

  /**
   * Returns a new instance of [JS].
   */
  fun create(): JS

}
