package tachiyomi.core.http

import com.squareup.duktape.Duktape

/**
 * An implementation of [JS] to execute JavaScript code backed by the duktape-android library.
 */
internal class DuktapeJS(private val duktape: Duktape) : JS {

  /**
   * Evaluates the given JavaScript [script] and returns its result as [String] or throws an
   * exception.
   */
  override fun evaluateAsString(script: String): String {
    return duktape.evaluate(script) as String
  }

  /**
   * Evaluates the given JavaScript [script] and returns its result as [Int] or throws an exception.
   */
  override fun evaluateAsInt(script: String): Int {
    return duktape.evaluate(script) as Int
  }

  /**
   * Evaluates the given JavaScript [script] and returns its result as [Double] or throws an
   * exception.
   */
  override fun evaluateAsDouble(script: String): Double {
    return duktape.evaluate(script) as Double
  }

  /**
   * Evaluates the given JavaScript [script] and returns its result as [Boolean] or throws an
   * exception.
   */
  override fun evaluateAsBoolean(script: String): Boolean {
    return duktape.evaluate(script) as Boolean
  }

  /**
   * Closes this duktape instance. No evaluations can be made on this instance after calling this
   * method.
   */
  override fun close() {
    duktape.close()
  }

}
