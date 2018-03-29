package tachiyomi.ui.library

import io.reactivex.processors.BehaviorProcessor
import io.reactivex.schedulers.Schedulers
import tachiyomi.core.rx.addTo
import tachiyomi.domain.library.LibraryCategory
import tachiyomi.domain.library.interactor.GetLibraryByCategory
import tachiyomi.ui.base.BasePresenter
import javax.inject.Inject

class LibraryPresenter @Inject constructor(
  private val getLibraryByCategory: GetLibraryByCategory
) : BasePresenter() {

  val stateRelay = BehaviorProcessor.create<LibraryViewState>().toSerialized()

  init {
    val initialState = LibraryViewState()

    val libraryChanges = getLibraryByCategory.interact()
      .map(Change::LibraryUpdate)
      .subscribeOn(Schedulers.io())

    libraryChanges.scan(initialState, ::reduce)
      .logOnNext()
      .subscribe(stateRelay::onNext)
      .addTo(disposables)
  }

  private fun reduce(state: LibraryViewState, change: Change): LibraryViewState {
    return when (change) {
      is Change.LibraryUpdate -> state.copy(library = change.library)
    }
  }

}

private sealed class Change {
  data class LibraryUpdate(val library: List<LibraryCategory>) : Change()
}
