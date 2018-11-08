package tachiyomi.core.di

import toothpick.Scope
import toothpick.Toothpick

/**
 * The global scope for dependency injection that will provide all the application level components.
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

  /**
   * Injects the application dependencies on the given object. Note the provided object must have
   * members annotated with the @Inject annotation.
   */
  fun inject(obj: Any) {
    Toothpick.inject(obj, AppScope.root())
  }

}
