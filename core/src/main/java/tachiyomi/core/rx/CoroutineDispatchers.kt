package tachiyomi.core.rx

import kotlinx.coroutines.CoroutineDispatcher

/**
 * Coroutines dispatchers available to the app.
 */
class CoroutineDispatchers(
  val io: CoroutineDispatcher,
  val computation: CoroutineDispatcher,
  val main: CoroutineDispatcher
)
