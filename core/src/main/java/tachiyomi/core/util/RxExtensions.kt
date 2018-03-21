package tachiyomi.core.util

import io.reactivex.disposables.Disposable
import io.reactivex.internal.disposables.DisposableContainer

fun Disposable.addTo(disposables: DisposableContainer) {
  disposables.add(this)
}
