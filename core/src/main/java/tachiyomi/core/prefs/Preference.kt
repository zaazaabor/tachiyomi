package tachiyomi.core.prefs

import io.reactivex.Observable

interface Preference<T> {

  fun get(): T?

  fun set(value: T)

  fun isSet(): Boolean

  fun delete()

  fun defaultValue(): T

  fun asObservable(): Observable<T>
}
