/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

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
