package tachiyomi.ui.catalogbrowse

import android.view.View
import com.bumptech.glide.load.engine.DiskCacheStrategy
import kotlinx.android.synthetic.main.manga_grid_item.*
import tachiyomi.domain.manga.model.Manga
import tachiyomi.glide.GlideApp
import tachiyomi.widget.StateImageViewTarget

class MangaGridHolder(
  private val view: View,
  private val adapter: CatalogBrowseAdapter
) : MangaHolder(view, adapter) {

  init {
    view.setOnClickListener {
      adapter.handleClick(adapterPosition)
    }
  }

  override fun bind(manga: Manga) {
    // Set manga title
    title.text = manga.title

    // Set alpha of thumbnail.
    thumbnail.alpha = if (manga.favorite) 0.3f else 1.0f

    bindImage(manga)
  }

  override fun bindImage(manga: Manga) {
    GlideApp.with(view.context).clear(thumbnail)
    if (!manga.cover.isEmpty()) {
      GlideApp.with(view.context)
        .load(manga.cover)
        .diskCacheStrategy(DiskCacheStrategy.DATA)
        .centerCrop()
        .placeholder(android.R.color.transparent)
        .into(StateImageViewTarget(thumbnail, progress))
    }
  }

}
