package tachiyomi.ui.catalogs

import io.reactivex.processors.BehaviorProcessor
import io.reactivex.schedulers.Schedulers
import tachiyomi.core.rx.addTo
import tachiyomi.domain.source.CatalogSource
import tachiyomi.domain.source.interactor.GetCatalogSources
import tachiyomi.ui.base.BasePresenter
import javax.inject.Inject

class CatalogsPresenter @Inject constructor(
  private val getCatalogSources: GetCatalogSources
) : BasePresenter() {

  val stateRelay = BehaviorProcessor.create<CatalogsViewState>().toSerialized()

  init {
    val initialState = CatalogsViewState()

    val catalogueChanges = getCatalogSources.interact()
      .subscribeOn(Schedulers.io())
      .map(Change::CatalogueUpdate)
      .toFlowable() // TODO reactive SourceManager?

    catalogueChanges.scan(initialState, ::reduce)
      .logOnNext()
      .subscribe(stateRelay::onNext)
      .addTo(disposables)
  }

}

private sealed class Change {
  data class CatalogueUpdate(val catalogs: List<CatalogSource>) : Change()
}

private fun reduce(state: CatalogsViewState, change: Change): CatalogsViewState {
  return when (change) {
    is Change.CatalogueUpdate -> state.copy(catalogs = change.catalogs)
  }
}
