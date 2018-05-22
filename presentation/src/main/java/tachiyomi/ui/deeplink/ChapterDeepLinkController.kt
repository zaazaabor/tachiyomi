package tachiyomi.ui.deeplink

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import io.reactivex.Flowable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.processors.BehaviorProcessor
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.deeplink_chapter_controller.*
import tachiyomi.app.R
import tachiyomi.core.rx.addTo
import tachiyomi.core.rx.scanWithPrevious
import tachiyomi.domain.chapter.interactor.FindOrInitChapterFromSource
import tachiyomi.domain.chapter.model.Chapter
import tachiyomi.domain.manga.interactor.FindOrInitMangaFromChapterKey
import tachiyomi.domain.manga.model.Manga
import tachiyomi.domain.source.SourceManager
import tachiyomi.source.DeepLinkSource
import tachiyomi.ui.base.BasePresenter
import tachiyomi.ui.base.MvpScopedController
import javax.inject.Inject

class ChapterDeepLinkController(
  bundle: Bundle?
) : MvpScopedController<ChapterDeepLinkController.Presenter>(bundle) {

  override fun getPresenterClass() = Presenter::class.java

  override fun getModule() = Module(this)

  private fun getChapterKey() = args.getString(CHAPTER_KEY)

  private fun getSourceId() = args.getLong(SOURCE_KEY)

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup,
    savedViewState: Bundle?
  ): View {
    return inflater.inflate(R.layout.deeplink_chapter_controller, container, false)
  }

  override fun onViewCreated(view: View) {
    super.onViewCreated(view)
    presenter.stateObserver
      .scanWithPrevious()
      .subscribeWithView { (state, prevState) -> render(state, prevState) }
  }

  @SuppressLint("SetTextI18n")
  private fun render(state: ViewState, prevState: ViewState?) {
    if (state.loading != prevState?.loading) {
      loading_progress.visibility = if (state.loading) View.VISIBLE else View.INVISIBLE
    }
    if (state.manga != null && state.manga != prevState?.manga) {
      loading_manga_text.text = "Manga: ${state.manga.id} - ${state.manga.title}"
    }
    if (state.chapter != null && state.chapter != prevState?.chapter) {
      loading_chapter_text.text = "Chapter: ${state.chapter.id} - ${state.chapter.name}"
    }
  }

  class Module(val controller: ChapterDeepLinkController) : toothpick.config.Module() {
    init {
      bind(String::class.java).toInstance(controller.getChapterKey())
      bind(Long::class.javaObjectType).toInstance(controller.getSourceId())
    }
  }

  class Presenter @Inject constructor(
    private val chapterKey: String?,
    private val sourceId: Long?,
    private val sourceManager: SourceManager,
    private val findOrInitMangaFromChapterKey: FindOrInitMangaFromChapterKey,
    private val findOrInitChapterFromSource: FindOrInitChapterFromSource
  ) : BasePresenter() {

    private val stateRelay = BehaviorProcessor.create<ViewState>()

    val stateObserver: Flowable<ViewState> = stateRelay

    init {
      val initialState = ViewState(loading = true)

      bindChanges()
        .scan(initialState, ::reduce)
        .logOnNext()
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(stateRelay::onNext)
        .addTo(disposables)
    }

    private fun bindChanges(): Flowable<Change> {
      if (sourceId == null || chapterKey == null || chapterKey.isEmpty()) {
        return Flowable.just(Change.Error(
          Exception("Invalid input data: sourceId=$sourceId, chapterKey=$chapterKey"))
        )
      }

      val source = sourceManager.get(sourceId) as? DeepLinkSource
        ?: return Flowable.just(Change.Error(Exception("Not a valid DeepLinkSource")))

      val findManga = findOrInitMangaFromChapterKey.interact(chapterKey, source)
        .subscribeOn(Schedulers.io())
        .toFlowable()
        .share()

      val findMangaIntent = findManga.map(Change::MangaReady)
      val findChapterIntent = findManga
        .flatMapSingle { findOrInitChapterFromSource.interact(chapterKey, source, it) }
        .map { Change.ChapterReady(it) }

      return Flowable.merge(findMangaIntent, findChapterIntent)
    }

    private fun reduce(state: ViewState, change: Change): ViewState {
      return when (change) {
        is Change.MangaReady -> state.copy(manga = change.manga)
        is Change.ChapterReady -> state.copy(loading = false, chapter = change.chapter)
        is Change.Error -> state.copy(loading = false, error = change.error)
      }
    }

  }

  data class ViewState(
    val loading: Boolean = true,
    val manga: Manga? = null,
    val chapter: Chapter? = null,
    val error: Throwable? = null
  )

  sealed class Change {
    data class MangaReady(val manga: Manga) : Change()
    data class ChapterReady(val chapter: Chapter) : Change()
    data class Error(val error: Throwable) : Change()
  }

  companion object {
    const val CHAPTER_KEY = "chapter_key"
    const val SOURCE_KEY = "source_key"
  }

}
