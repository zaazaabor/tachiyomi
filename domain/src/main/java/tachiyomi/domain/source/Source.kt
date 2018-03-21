package tachiyomi.domain.source

import tachiyomi.domain.source.model.SChapter
import tachiyomi.domain.source.model.SManga
import tachiyomi.domain.source.model.SPage

/**
 * A basic interface for creating a source. It could be an online source, a local source, etc...
 */
interface Source {

  /**
   * Id for the source. Must be unique.
   */
  val id: Long

  /**
   * Name of the source.
   */
  val name: String

  /**
   * Returns an observable with the updated details for a manga.
   *
   * @param manga the manga to update.
   */
  fun fetchMangaDetails(manga: SManga): SManga

  /**
   * Returns an observable with all the available chapters for a manga.
   *
   * @param manga the manga to update.
   */
  fun fetchChapterList(manga: SManga): List<SChapter>

  /**
   * Returns an observable with the list of pages a chapter has.
   *
   * @param chapter the chapter.
   */
  fun fetchPageList(chapter: SChapter): List<SPage>

}
