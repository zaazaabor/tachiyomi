/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.domain.library.updater

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import tachiyomi.core.rx.CoroutineDispatchers
import timber.log.Timber
import timber.log.warn
import toothpick.ProvidesSingletonInScope
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.CoroutineContext

@Singleton
@ProvidesSingletonInScope
class LibraryUpdater @Inject constructor(
  private val dispatchers: CoroutineDispatchers
) {

  private val queueChannel = Channel<Operation>(Channel.UNLIMITED)
  private val queuedOperations = mutableSetOf<Operation>()

  init {
    GlobalScope.launch(dispatchers.single) {
      for (operation in queueChannel) {
        Timber.warn { "Received job ${operation.categoryId}" }
        operation.start(this, dispatchers.io)
        queuedOperations.remove(operation)
        Timber.warn { "End job" }
      }
    }
  }

  suspend fun enqueue(categoryId: Long, target: Target, source: (Job) -> Any): QueueResult {
    return withContext(dispatchers.single) {
      val operation = Operation(categoryId, target, source)
      if (operation in queuedOperations) {
        QueueResult.AlreadyEnqueued
      } else if (queuedOperations.isEmpty()) {
        enqueue(operation)
        QueueResult.Executing { operation.await() }
      } else {
        enqueue(operation)
        QueueResult.Queued { operation.await() }
      }
    }
  }

  private suspend fun enqueue(operation: Operation) {
    if (queuedOperations.add(operation)) {
      queueChannel.send(operation)
    }
  }

  fun cancel(categoryId: Long, target: Target) {
    GlobalScope.launch(dispatchers.single) {
      val operation = queuedOperations.find { it.categoryId == categoryId && it.target == target }
      if (operation != null) {
        operation.cancel()
        queuedOperations.remove(operation)
      }
    }
  }

  fun cancelFirst() {
    GlobalScope.launch(dispatchers.single) {
      val operation = queuedOperations.firstOrNull()
      if (operation != null) {
        operation.cancel()
        queuedOperations.remove(operation)
      }
    }
  }

  enum class Target {
    Chapters, Metadata;
  }

  sealed class QueueResult {
    abstract val awaitWork: suspend () -> Unit

    data class Executing(override val awaitWork: suspend () -> Unit) : QueueResult()
    data class Queued(override val awaitWork: suspend () -> Unit) : QueueResult()
    object AlreadyEnqueued : QueueResult() {
      override val awaitWork: suspend () -> Unit
        get() = {}
    }
  }

  private data class Operation(
    val categoryId: Long,
    val target: Target,
    val source: (Job) -> Any
  ) {

    private val workJob = Job() // TODO is this really needed? Maybe awaitJob is enough
    private val awaitJob = Job()

    fun cancel() {
      workJob.cancel()
      awaitJob.complete()
    }

    suspend fun start(scope: CoroutineScope, context: CoroutineContext) {
      if (workJob.isCompleted) return

      @Suppress("DeferredResultUnused")
      scope.async(context + workJob) {
        try {
          source(awaitJob)
        } finally {
          awaitJob.complete()
        }
        workJob.complete()
      }
      awaitJob.join()
    }

    suspend fun await() {
      try {
        awaitJob.join()
      } catch (e: CancellationException) {
      }
    }

    override fun equals(other: Any?): Boolean {
      if (this === other) return true
      if (javaClass != other?.javaClass) return false

      other as Operation

      if (categoryId != other.categoryId) return false
      if (target != other.target) return false

      return true
    }

    override fun hashCode(): Int {
      var result = categoryId.hashCode()
      result = 31 * result + target.hashCode()
      return result
    }

  }

}
