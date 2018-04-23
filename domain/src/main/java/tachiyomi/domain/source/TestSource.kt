package tachiyomi.domain.source

import tachiyomi.domain.source.model.ChapterMeta
import tachiyomi.domain.source.model.MangaMeta
import tachiyomi.domain.source.model.MangasPageMeta
import tachiyomi.domain.source.model.PageMeta

class TestSource : CatalogSource {

  override val id = 1L
  override val name = "Test source"
  override val lang get() = "en"

  override fun fetchMangaList(page: Int): MangasPageMeta {
    Thread.sleep(1500)
    return MangasPageMeta(getTestManga(page), page < 3)
  }

  override fun fetchMangaDetails(manga: MangaMeta): MangaMeta {
    Thread.sleep(1000)
    val noHipstersOffset = 10
    val picId = manga.title.split(" ")[1].toInt() + noHipstersOffset
    return manga.copy(cover = "https://picsum.photos/300/400/?image=$picId", initialized = true)
  }

  override fun fetchChapterList(manga: MangaMeta): List<ChapterMeta> {
    Thread.sleep(1000)
    return getTestChapters()
  }

  override fun fetchPageList(chapter: ChapterMeta): List<PageMeta> {
    Thread.sleep(1000)
    return getTestPages()
  }

  private fun getTestManga(page: Int): List<MangaMeta> {
    val list = mutableListOf<MangaMeta>()
    val id = (page - 1) * 20 + 1
    val manga1 = MangaMeta(
      "$id",
      "Manga $id",
      "",
      "",
      "",
      "",
      0,
      "",
      false
    )
    list += manga1

    for (i in 1..19) {
      list += manga1.copy(key = "${id + i}", title = "Manga ${id + i}")
    }

    return list
  }

  private fun getTestChapters(): List<ChapterMeta> {
    val chapter1 = ChapterMeta(
      "1",
      "Chapter 1",
      System.currentTimeMillis()
    )
    val chapter2 = chapter1.copy(key = "2", name = "Manga2")
    val chapter3 = chapter1.copy(key = "3", name = "Manga3")

    return listOf(chapter1, chapter2, chapter3)
  }

  private fun getTestPages(): List<PageMeta> {
    return listOf(
      PageMeta("url1", "imageUrl1"),
      PageMeta("url2", "imageUrl2")
    )
  }

}
