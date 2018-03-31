package tachiyomi.ui.catalogbrowse

import io.reactivex.Flowable
import io.reactivex.Maybe
import io.reactivex.disposables.Disposable
import io.reactivex.processors.BehaviorProcessor
import io.reactivex.processors.PublishProcessor
import io.reactivex.schedulers.Schedulers
import tachiyomi.core.rx.addTo
import tachiyomi.core.rx.mapNullable
import tachiyomi.core.util.replaceFirst
import tachiyomi.domain.manga.interactor.GetMangaPageFromCatalogueSource
import tachiyomi.domain.manga.interactor.MangaInitializer
import tachiyomi.domain.manga.model.Manga
import tachiyomi.domain.source.CatalogueSource
import tachiyomi.domain.source.SourceManager
import tachiyomi.ui.base.BasePresenter
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class CatalogBrowsePresenter @Inject constructor(
  private val sourceId: Long?,
  private val sourceManager: SourceManager, // TODO new use case to retrieve a catalogue?
  private val getMangaPageFromCatalogueSource: GetMangaPageFromCatalogueSource,
  private val mangaInitializer: MangaInitializer
) : BasePresenter() {

  val stateRelay = BehaviorProcessor.create<CatalogBrowseViewState>().toSerialized()

  private val mangaInitializerSubject = PublishProcessor.create<List<Manga>>().toSerialized()

  private val mangaInitializedSubject = PublishProcessor.create<Manga>().toSerialized()

  private var mangaInitializerDisposable: Disposable? = null

  init {
    val initialState = CatalogBrowseViewState()

    val sourceAvailable = Maybe.fromCallable { sourceManager.get(sourceId!!) as? CatalogueSource }
      .doOnSuccess { prepareMangaInitializer(it!!) }
      .map(Change::SourceUpdate)
      .toFlowable()

    val test = Flowable.timer(6, TimeUnit.SECONDS)
      .map { Change.ListModeUpdate(true) } // TODO temporary

    val firstPage = stateRelay
      .mapNullable { it.source }
      .distinctUntilChanged()
      .switchMap { source ->
        getMangaPageFromCatalogueSource.interact(source, 1)
          .doOnSuccess { mangaInitializerSubject.onNext(it.mangas) }
          .map { Change.PageReceived(1, it.mangas) }
          .subscribeOn(Schedulers.io())
          .toFlowable()
      }

    val mangaInitialized = mangaInitializedSubject
      .map(Change::MangaInitialized)

    val intents = listOf(sourceAvailable, firstPage, mangaInitialized)

    Flowable.merge(intents)
      .scan(initialState, ::reduce)
      .logOnNext()
      .subscribe(stateRelay::onNext)
      .addTo(disposables)
  }

  private fun prepareMangaInitializer(source: CatalogueSource) {
    mangaInitializerDisposable?.dispose()
    mangaInitializerDisposable = mangaInitializerSubject
      .observeOn(Schedulers.io())
      .flatMapIterable { it }
      .flatMapMaybe({ mangaInitializer.interact(source, it) }, false, 1)
      .subscribe(mangaInitializedSubject::onNext)
  }

  override fun destroy() {
    super.destroy()
    mangaInitializerDisposable?.dispose()
  }

}

private sealed class Change {
  data class SourceUpdate(val source: CatalogueSource) : Change()
  data class PageReceived(val page: Int, val mangas: List<Manga>) : Change()
  data class ListModeUpdate(val isListMode: Boolean) : Change()
  data class MangaInitialized(val manga: Manga) : Change()
}

private fun reduce(state: CatalogBrowseViewState, change: Change): CatalogBrowseViewState {
  return when (change) {
    is Change.SourceUpdate -> state.copy(source = change.source, mangas = emptyList())
    is Change.PageReceived -> state.copy(mangas = state.mangas + change.mangas)
    is Change.ListModeUpdate -> state.copy(isListMode = change.isListMode)
    is Change.MangaInitialized -> state.copy(mangas = state.mangas
      .replaceFirst({ it.id == change.manga.id }, change.manga)
    )
  }
}
