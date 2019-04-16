/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.core.rx

import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import io.reactivex.functions.BiFunction
import io.reactivex.internal.disposables.DisposableContainer
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.reactive.openSubscription
import kotlinx.coroutines.rx2.openSubscription

/**
 * Adds this disposable to a [disposables] container.
 */
fun Disposable.addTo(disposables: DisposableContainer) {
  disposables.add(this)
}

/**
 * Returns a flowable that emits the current emission paired with the previous one.
 */
@Suppress("UNCHECKED_CAST")
fun <T> Flowable<T>.scanWithPrevious(): Flowable<Pair<T, T?>> {
  return scan(Pair<T?, T?>(null, null), { prev, newValue -> Pair(newValue, prev.first) })
    .skip(1) as Flowable<Pair<T, T?>>
}

/**
 * Returns an observable that emits the current emission paired with the previous one.
 */
@Suppress("UNCHECKED_CAST")
fun <T> Observable<T>.scanWithPrevious(): Observable<Pair<T, T?>> {
  return scan(Pair<T?, T?>(null, null), { prev, newValue -> Pair(newValue, prev.first) })
    .skip(1) as Observable<Pair<T, T?>>
}

/**
 * Returns a flowable that skips the null values of the result of the given [block] function.
 */
inline fun <T, R> Flowable<T>.filterNotNull(crossinline block: (T) -> R?): Flowable<R> {
  return flatMap { block(it)?.let { Flowable.just(it) } ?: Flowable.empty() }
}

/**
 * Returns an observable that skips the null values of the result of the given [block] function.
 */
inline fun <T, R> Observable<T>.filterNotNull(crossinline block: (T) -> R?): Observable<R> {
  return flatMap { block(it)?.let { Observable.just(it) } ?: Observable.empty() }
}

fun <T, U, R> Flowable<T>.combineLatest(o2: Flowable<U>, combineFn: (T, U) -> R): Flowable<R> {
  return Flowable.combineLatest(this, o2, BiFunction<T, U, R>(combineFn))
}

fun <T> Observable<T>.asFlow(): Flow<T> {
  return flow {
    val channel = openSubscription()

    channel.consumeEach { value ->
      emit(value)
    }
  }
}

fun <T> Flowable<T>.asFlow(): Flow<T> {
  return flow {
    val channel = openSubscription()

    channel.consumeEach { value ->
      emit(value)
    }
  }
}
