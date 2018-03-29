package tachiyomi.ui.catalogs

import io.reactivex.processors.BehaviorProcessor
import io.reactivex.schedulers.Schedulers
import tachiyomi.domain.source.CatalogueSource
import tachiyomi.domain.source.interactor.GetCatalogueSources
import tachiyomi.ui.base.BasePresenter
import javax.inject.Inject

class CatalogsPresenter @Inject constructor(
  private val getCatalogueSources: GetCatalogueSources
) : BasePresenter() {

  val stateRelay = BehaviorProcessor.create<CatalogsViewState>().toSerialized()

  init {
    val initialState = CatalogsViewState()

    val catalogueChanges = getCatalogueSources.interact()
      .subscribeOn(Schedulers.io())
      .map(Change::CatalogueUpdate)
      .toFlowable() // TODO reactive SourceManager?

    catalogueChanges.scan(initialState, ::reduce)
      .logOnNext()
      .subscribe(stateRelay::onNext)
  }

}

private sealed class Change {
  data class CatalogueUpdate(val catalogues: List<CatalogueSource>) : Change()
}

private fun reduce(state: CatalogsViewState, change: Change): CatalogsViewState {
  return when (change) {
    is Change.CatalogueUpdate -> state.copy(catalogues = change.catalogues)
  }
}
