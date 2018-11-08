package tachiyomi.ui.deeplink

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.deeplink_chapter_controller.*
import tachiyomi.app.R
import tachiyomi.core.rx.scanWithPrevious
import tachiyomi.ui.base.MvpScopedController

class ChapterDeepLinkController(
  bundle: Bundle?
) : MvpScopedController<ChapterDeepLinkPresenter>(bundle) {

  override fun getPresenterClass() = ChapterDeepLinkPresenter::class.java

  override fun getModule() = ChapterDeepLinkModule(this)

  fun getChapterKey() = args.getString(
    CHAPTER_KEY)

  fun getSourceId() = args.getLong(
    SOURCE_KEY)

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
  private fun render(state: ChapterDeepLinkViewState, prevState: ChapterDeepLinkViewState?) {
    if (state.loading != prevState?.loading) {
      loading_progress.visibility = if (state.loading) View.VISIBLE else View.INVISIBLE
    }
    if (state.manga != null && state.manga != prevState?.manga) {
      loading_manga_text.text = "Manga: ${state.manga.id} - ${state.manga.title}"
    }
    if (state.chapter != null && state.chapter != prevState?.chapter) {
      loading_chapter_text.text = "Chapter: ${state.chapter.id} - ${state.chapter.name}"
    }
    if (state.error != null && state.error != prevState?.error) {
      loading_chapter_text.text = state.error.toString()
    }
  }

  companion object {
    const val CHAPTER_KEY = "chapter_key"
    const val SOURCE_KEY = "source_key"
  }

}
