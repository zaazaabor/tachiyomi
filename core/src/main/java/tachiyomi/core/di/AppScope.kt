package tachiyomi.core.di

import toothpick.Scope
import toothpick.Toothpick

/**
 * The global scope that will provide all the application level components.
 */
object AppScope {

  /**
   * Returns the root scope.
   */
  fun root(): Scope {
    return Toothpick.openScope(AppScope)
  }

  /**
   * Returns a new subscope inheriting the root scope.
   */
  fun subscope(any: Any): Scope {
    return Toothpick.openScopes(AppScope, any)
  }

}
