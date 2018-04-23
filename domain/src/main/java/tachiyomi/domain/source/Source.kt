package tachiyomi.domain.source

import tachiyomi.domain.source.model.ChapterMeta
import tachiyomi.domain.source.model.MangaMeta
import tachiyomi.domain.source.model.PageMeta

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
  fun fetchMangaDetails(manga: MangaMeta): MangaMeta

  /**
   * Returns an observable with all the available chapters for a manga.
   *
   * @param manga the manga to update.
   */
  fun fetchChapterList(manga: MangaMeta): List<ChapterMeta>

  /**
   * Returns an observable with the list of pages a chapter has.
   *
   * @param chapter the chapter.
   */
  fun fetchPageList(chapter: ChapterMeta): List<PageMeta>

}
