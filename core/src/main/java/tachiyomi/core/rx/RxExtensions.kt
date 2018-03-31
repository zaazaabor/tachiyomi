package tachiyomi.core.rx

import io.reactivex.Flowable
import io.reactivex.disposables.Disposable
import io.reactivex.internal.disposables.DisposableContainer

fun Disposable.addTo(disposables: DisposableContainer) {
  disposables.add(this)
}

@Suppress("UNCHECKED_CAST")
fun <T> Flowable<T>.scanWithPrevious(): Flowable<Pair<T, T?>> {
  return scan(Pair<T?, T?>(null, null), { prev, newValue -> Pair(newValue, prev.first) })
    .skip(1) as Flowable<Pair<T, T?>>
}

inline fun <T, R> Flowable<T>.mapNullable(crossinline block: (T) -> R?): Flowable<R> {
  return flatMap { block(it)?.let { Flowable.just(it) } ?: Flowable.empty() }
}
