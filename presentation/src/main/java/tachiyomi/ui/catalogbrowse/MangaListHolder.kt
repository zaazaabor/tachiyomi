package tachiyomi.ui.catalogbrowse

import android.view.View
import kotlinx.android.synthetic.main.manga_list_item.*
import tachiyomi.domain.manga.model.Manga

class MangaListHolder(
  private val view: View,
  private val adapter: CatalogBrowseAdapter
) : MangaHolder(view, adapter) {

  override fun bind(manga: Manga) {
    // Set manga title
    title.text = manga.title

    // Set alpha of thumbnail.
    thumbnail.alpha = if (manga.favorite) 0.3f else 1.0f

    bindImage(manga)
  }

  override fun bindImage(manga: Manga) {
  }

//  override fun setImage(manga: Manga) {
//    GlideApp.with(view.context).clear(thumbnail)
//    if (!manga.thumbnail_url.isNullOrEmpty()) {
//      GlideApp.with(view.context)
//        .load(manga)
//        .diskCacheStrategy(DiskCacheStrategy.DATA)
//        .centerCrop()
//        .skipMemoryCache(true)
//        .placeholder(android.R.color.transparent)
//        .into(StateImageViewTarget(thumbnail, progress))
//    }
//  }
}
