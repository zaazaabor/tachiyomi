package tachiyomi.ui.catalogbrowse

import android.view.View
import tachiyomi.domain.manga.model.Manga
import tachiyomi.ui.base.BaseViewHolder

/**
 * Abstract holder to use when displaying a [Manga] from a [CatalogBrowseAdapter]. This base
 * class is used to have a common interface on list a grid holders.
 */
abstract class MangaHolder(view: View) : BaseViewHolder(view) {

  /**
   * Binds the given [manga] with this holder.
   */
  abstract fun bind(manga: Manga)

  /**
   * Binds only the cover of the given [manga] with this holder.
   */
  abstract fun bindImage(manga: Manga)

}
