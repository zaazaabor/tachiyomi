package tachiyomi.core.rx

import io.reactivex.Scheduler

/**
 * RxJava schedulers available to the app.
 */
class RxSchedulers(
  val io: Scheduler,
  val computation: Scheduler,
  val main: Scheduler
)
