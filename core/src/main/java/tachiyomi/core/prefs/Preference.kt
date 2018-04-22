package tachiyomi.core.prefs

import io.reactivex.Observable

/**
 * A wrapper around application preferences without knowing implementation details. Instances of
 * this interface must be provided through a [PreferenceStore].
 */
interface Preference<T> {

  /**
   * Returns the current value of this preference.
   */
  fun get(): T

  /**
   * Sets a new [value] for this preference.
   */
  fun set(value: T)

  /**
   * Returns whether there's an existing entry for this preference.
   */
  fun isSet(): Boolean

  /**
   * Deletes the entry of this preference.
   */
  fun delete()

  /**
   * Returns the default value of this preference
   */
  fun defaultValue(): T

  /**
   * Returns an observer of the changes made to this preference. The current value of the preference
   * must be returned when subscribed. Callers may decide to skip this initial value with the skip
   * operator.
   */
  fun asObservable(): Observable<T>
}
