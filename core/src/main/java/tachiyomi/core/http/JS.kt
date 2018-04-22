package tachiyomi.core.http

import java.io.Closeable

/**
 * A wrapper to allow executing JavaScript code without knowing the implementation details.
 */
interface JS : Closeable {

  /**
   * Evaluates the given JavaScript [script] and returns its result as [String] or throws an
   * exception.
   */
  fun evaluateAsString(script: String): String

  /**
   * Evaluates the given JavaScript [script] and returns its result as [Int] or throws an exception.
   */
  fun evaluateAsInt(script: String): Int

  /**
   * Evaluates the given JavaScript [script] and returns its result as [Double] or throws an
   * exception.
   */
  fun evaluateAsDouble(script: String): Double

  /**
   * Evaluates the given JavaScript [script] and returns its result as [Boolean] or throws an
   * exception.
   */
  fun evaluateAsBoolean(script: String): Boolean

}
