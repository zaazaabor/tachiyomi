package tachiyomi.ui.deeplink

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import io.reactivex.Flowable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.processors.BehaviorProcessor
import io.reactivex.schedulers.Schedulers
import tachiyomi.app.R
import tachiyomi.core.rx.addTo
import tachiyomi.domain.manga.interactor.GetOrAddMangaFromSource
import tachiyomi.source.model.MangaMeta
import tachiyomi.ui.base.BasePresenter
import tachiyomi.ui.base.MvpScopedController
import tachiyomi.ui.base.withFadeTransition
import tachiyomi.ui.manga.MangaController
import javax.inject.Inject

class MangaDeepLinkController(
  bundle: Bundle? = null
) : MvpScopedController<MangaDeepLinkController.Presenter>(bundle) {

  override fun getPresenterClass() = Presenter::class.java

  override fun getModule() = Module(this)

  fun getMangaKey() = args.getString(MANGA_KEY)

  fun getSourceId() = args.getLong(SOURCE_KEY)

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup,
    savedViewState: Bundle?
  ): View {
    return inflater.inflate(R.layout.deeplink_manga_controller, container, false)
  }

  override fun onViewCreated(view: View) {
    super.onViewCreated(view)
    presenter.stateObserver
      .subscribeWithView(::render)
  }

  // TODO complete all renderizable components
  private fun render(state: ViewState) {
    if (state.mangaId != null) {
      router.setRoot(MangaController(state.mangaId).withFadeTransition())
    }
  }

  class Presenter @Inject constructor(
    private val mangaKey: String?,
    private val sourceId: Long?,
    private val getOrAddMangaFromSource: GetOrAddMangaFromSource
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
      if (sourceId == null || mangaKey == null || mangaKey.isEmpty()) {
        return Flowable.just(Change.Error(
          Exception("Invalid input data: sourceId=$sourceId, mangaKey=$mangaKey"))
        )
      }

      val meta = MangaMeta(key = mangaKey, title = "")

      return getOrAddMangaFromSource.interact(meta, sourceId)
        .toFlowable()
        .subscribeOn(Schedulers.io())
        .map<Change> { Change.MangaReady(it.id) }
        .onErrorReturn(Change::Error)
    }

    private fun reduce(state: ViewState, change: Change): ViewState {
      return when (change) {
        is Change.MangaReady -> state.copy(loading = false, mangaId = change.mangaId)
        is Change.Error -> state.copy(loading = false, error = change.error)
      }
    }

  }

  class Module(controller: MangaDeepLinkController) : toothpick.config.Module() {
    init {
      bind(String::class.java).toInstance(controller.getMangaKey())
      bind(Long::class.javaObjectType).toInstance(controller.getSourceId())
    }
  }

  data class ViewState(
    val loading: Boolean = true,
    val mangaId: Long? = null,
    val error: Throwable? = null
  )

  sealed class Change {
    data class MangaReady(val mangaId: Long) : Change()
    data class Error(val error: Throwable?) : Change()
  }

  companion object {
    const val MANGA_KEY = "manga_key"
    const val SOURCE_KEY = "source_key"
  }

}
