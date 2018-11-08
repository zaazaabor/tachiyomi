package tachiyomi.ui.manga

import android.view.View
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.manga_header_item.*
import tachiyomi.ui.base.BaseViewHolder

class MangaHeaderHolder(private val view: View) : BaseViewHolder(view) {

  fun bind(header: MangaHeader?) {
    if (header == null) return
    val manga = header.manga

    manga_full_title.text = manga.title
    manga_author.text = manga.author
    manga_artist.text = manga.artist
    manga_status.text = manga.status.toString()
    manga_source.text = manga.source.toString()

    Glide.with(view.context)
      .load(manga.cover) // TODO use custom model loader
      //.diskCacheStrategy(DiskCacheStrategy.RESOURCE)
      //.centerCrop()
      .into(manga_cover)

  }

}
