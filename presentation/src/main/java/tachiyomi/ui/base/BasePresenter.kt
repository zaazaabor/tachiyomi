package tachiyomi.ui.base

import androidx.annotation.CallSuper
import com.jakewharton.rxrelay2.PublishRelay
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.disposables.CompositeDisposable
import timber.log.Timber
import kotlin.reflect.KClass

abstract class BasePresenter {

  val disposables = CompositeDisposable()

  @CallSuper
  open fun destroy() {
    disposables.dispose()
  }

  fun <T> Flowable<T>.logOnNext(): Flowable<T> {
    return doOnNext { Timber.d(it.toString()) }
  }

  class ActionsPublisher<Action : Any> {
    private val relay = PublishRelay.create<Action>()
    val observer: Flowable<Action> = relay.toFlowable(BackpressureStrategy.BUFFER)

    fun emit(action: Action) {
      relay.accept(action)
    }

    fun <SubAction : Action> ofType(subActionCls: KClass<SubAction>): Flowable<SubAction> {
      return observer.ofType(subActionCls.java)
    }
  }

}
