package tachiyomi.core.prefs

import io.reactivex.Observable
import com.f2prateek.rx.preferences2.Preference as RxPreference

/**
 * An implementation of [Preference] to manage a single preference.
 */
class SharedPreference<T> internal constructor(
  private val preference: RxPreference<T>
) : Preference<T> {

  /**
   * Returns the current value of this preference.
   */
  override fun get(): T {
    return preference.get()
  }

  /**
   * Sets a new [value] for this preference.
   */
  override fun set(value: T) {
    preference.set(value)
  }

  /**
   * Returns whether there's an existing entry for this preference.
   */
  override fun isSet(): Boolean {
    return preference.isSet
  }

  /**
   * Deletes the entry of this preference.
   */
  override fun delete() {
    preference.delete()
  }

  /**
   * Returns the default value of this preference
   */
  override fun defaultValue(): T {
    return preference.defaultValue()
  }

  /**
   * Returns an observer of the changes made to this preference. The current value of the preference
   * must be returned when subscribed. Callers may decide to skip this initial value with the skip
   * operator.
   */
  override fun asObservable(): Observable<T> {
    return preference.asObservable()
  }

}
