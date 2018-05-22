package tachiyomi.ui.manga

import io.reactivex.Flowable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.processors.BehaviorProcessor
import tachiyomi.core.rx.addTo
import tachiyomi.core.rx.mapNullable
import tachiyomi.domain.chapter.interactor.SyncChaptersFromSource
import tachiyomi.domain.manga.interactor.MangaInitializer
import tachiyomi.domain.manga.interactor.SubscribeManga
import tachiyomi.domain.manga.model.Manga
import tachiyomi.domain.source.SourceManager
import tachiyomi.source.model.MangaMeta
import tachiyomi.ui.base.BasePresenter
import timber.log.Timber
import javax.inject.Inject

class MangaPresenter @Inject constructor(
  private val mangaId: Long?,
  private val subscribeManga: SubscribeManga,
  private val mangaInitializer: MangaInitializer,
  private val syncChaptersFromSource: SyncChaptersFromSource,
  private val sourceManager: SourceManager
) : BasePresenter() {

  private val stateRelay = BehaviorProcessor.create<MangaViewState>()

  val stateObserver: Flowable<MangaViewState> = stateRelay

  init {
    val initialState = MangaViewState()

    val sharedManga = subscribeManga.interact(mangaId!!).share()

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

    sharedManga.map { it.get() }
      .flatMapSingle { manga ->
        val meta = MangaMeta(key = manga.key, title = "")
        val source = sourceManager.get(manga.source)!!
        Single.fromCallable { source.fetchChapterList(meta) }
          .flatMap { syncChaptersFromSource.interact(it, manga) }
          .doOnSuccess { Timber.e(it.toString()) }
          .doOnError { Timber.e(it) }
      }
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
