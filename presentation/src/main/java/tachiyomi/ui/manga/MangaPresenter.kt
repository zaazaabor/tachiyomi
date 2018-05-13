package tachiyomi.ui.manga

import io.reactivex.Flowable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.processors.BehaviorProcessor
import tachiyomi.core.rx.addTo
import tachiyomi.core.rx.mapNullable
import tachiyomi.domain.manga.interactor.GetManga
import tachiyomi.domain.manga.interactor.MangaInitializer
import tachiyomi.domain.manga.model.Manga
import tachiyomi.ui.base.BasePresenter
import javax.inject.Inject

class MangaPresenter @Inject constructor(
  private val mangaId: Long?,
  private val getManga: GetManga,
  private val mangaInitializer: MangaInitializer
) : BasePresenter() {

  private val stateRelay = BehaviorProcessor.create<MangaViewState>()

  val stateObserver: Flowable<MangaViewState> = stateRelay

  init {
    val initialState = MangaViewState()

    val sharedManga = getManga.interact(mangaId!!).share()

    val mangaIntent = sharedManga
      .mapNullable { it.get() }
      .map(Change::MangaUpdate)

    val intents = listOf(mangaIntent)

    Flowable.merge(intents)
      .scan(initialState, ::reduce)
      .logOnNext()
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(stateRelay::onNext)
      .addTo(disposables)

    // Initialize manga if needed
    sharedManga.mapNullable { it.get() }
      .take(1)
      .flatMapMaybe { mangaInitializer.interact(it) }
      .ignoreElements()
      .subscribe()
  }

}

private sealed class Change {
  data class MangaUpdate(val manga: Manga) : Change()
}

private fun reduce(state: MangaViewState, change: Change): MangaViewState {
  return when (change) {
    is Change.MangaUpdate -> state.copy(
      header = state.header?.copy(manga = change.manga) ?: MangaHeader(manga = change.manga)
    )
  }
}
