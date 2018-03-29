package tachiyomi.ui.catalogbrowse

import io.reactivex.Flowable
import io.reactivex.Maybe
import io.reactivex.processors.BehaviorProcessor
import tachiyomi.domain.source.CatalogueSource
import tachiyomi.domain.source.SourceManager
import tachiyomi.ui.base.BasePresenter
import javax.inject.Inject

class CatalogBrowsePresenter @Inject constructor(
  val sourceId: Long?,
  val sourceManager: SourceManager // TODO new use case to retrieve a catalogue?
) : BasePresenter() {

  val stateRelay = BehaviorProcessor.create<CatalogBrowseViewState>().toSerialized()

  init {
    val initialState = CatalogBrowseViewState()

    val catalogAvailable = Maybe.fromCallable { sourceManager.get(sourceId!!) as? CatalogueSource }
      .map(Change::SourceFound)
      .toFlowable()

    val intents = listOf(catalogAvailable)

    Flowable.merge(intents)
      .scan(initialState, ::reduce)
      .logOnNext()
      .subscribe(stateRelay::onNext)
  }

}

private sealed class Change {
  data class SourceFound(val source: CatalogueSource) : Change()
}

private fun reduce(state: CatalogBrowseViewState, change: Change): CatalogBrowseViewState {
  return when (change) {
    is Change.SourceFound -> state.copy(source = change.source)
  }
}
