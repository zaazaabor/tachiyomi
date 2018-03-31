package tachiyomi.ui.catalogbrowse

import android.view.View
import tachiyomi.domain.manga.model.Manga
import tachiyomi.ui.base.BaseViewHolder

abstract class MangaHolder(view: View, adapter: CatalogBrowseAdapter) : BaseViewHolder(view) {

  abstract fun bind(manga: Manga)

  abstract fun bindImage(manga: Manga)

}
