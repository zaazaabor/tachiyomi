package tachiyomi.ui.deeplink

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import io.reactivex.Flowable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.processors.BehaviorProcessor
import kotlinx.android.synthetic.main.deeplink_chapter_controller.*
import tachiyomi.app.R
import tachiyomi.core.rx.addTo
import tachiyomi.ui.base.BasePresenter
import tachiyomi.ui.base.MvpScopedController
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class ChapterDeepLinkController(
  bundle: Bundle?
) : MvpScopedController<ChapterDeepLinkController.Presenter>(bundle) {

  override fun getPresenterClass() = Presenter::class.java

  override fun getModule() = Module(this)

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup,
    savedViewState: Bundle?
  ): View {
    return inflater.inflate(R.layout.deeplink_chapter_controller, container, false)
  }

  override fun onViewCreated(view: View) {
    super.onViewCreated(view)
    presenter.stateRelay
      .subscribeWithView { render(it) }
  }

  private fun render(step: Step) {
    loading_text.text = when (step) {
      Step.Start -> "Loading"
      Step.RetrieveManga -> "Retrieving manga"
      Step.RetrieveChapter -> "Retrieving chapter"
      Step.End -> "End"
      Step.Error -> "Error"
    }
  }

  class Module(val controller: ChapterDeepLinkController) : toothpick.config.Module() {
    init {

    }
  }

  class Presenter @Inject constructor() : BasePresenter() {

    val stateRelay = BehaviorProcessor.create<Step>()

    init {
      // TODO this is just a placeholder
      Flowable.interval(0, 2, TimeUnit.SECONDS)
        .scan(Step.Start, { prev, now ->
          when (prev) {
            Step.Start -> Step.RetrieveManga
            Step.RetrieveManga -> Step.RetrieveChapter
            Step.RetrieveChapter -> Step.End
            else -> Step.Error
          }
        })
        .observeOn(AndroidSchedulers.mainThread())
        .doOnNext(stateRelay::onNext)
        .subscribe()
        .addTo(disposables)
    }

  }

  enum class Step { Start, RetrieveManga, RetrieveChapter, End, Error }

  companion object {
    const val CHAPTER_KEY = "chapter_key"
    const val SOURCE_KEY = "source_key"
  }

}
