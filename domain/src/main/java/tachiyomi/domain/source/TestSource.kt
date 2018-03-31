package tachiyomi.domain.source

import tachiyomi.domain.source.model.SChapter
import tachiyomi.domain.source.model.SManga
import tachiyomi.domain.source.model.SMangasPage
import tachiyomi.domain.source.model.SPage

class TestSource : CatalogueSource {

  override val id = 1L
  override val name = "Test source"
  override val lang get() = "en"

  override fun fetchMangaList(page: Int): SMangasPage {
    Thread.sleep(1000)
    return SMangasPage(getTestManga(), false)
  }

  override fun fetchMangaDetails(manga: SManga): SManga {
    Thread.sleep(1000)
    return manga.copy(cover = "cover.jpg", initialized = true)
  }

  override fun fetchChapterList(manga: SManga): List<SChapter> {
    Thread.sleep(1000)
    return getTestChapters()
  }

  override fun fetchPageList(chapter: SChapter): List<SPage> {
    Thread.sleep(1000)
    return getTestPages()
  }

  private fun getTestManga(): List<SManga> {
    val list = mutableListOf<SManga>()

    val manga1 = SManga(
      "1",
      "Manga 1",
      "",
      "",
      "",
      "",
      0,
      "",
      false
    )
    list += manga1

    for (i in 2..20) {
      list += manga1.copy(key = "$i", title = "Manga $i")
    }

    return list
  }

  private fun getTestChapters(): List<SChapter> {
    val chapter1 = SChapter(
      "1",
      "Chapter 1",
      System.currentTimeMillis()
    )
    val chapter2 = chapter1.copy(key = "2", name = "Manga2")
    val chapter3 = chapter1.copy(key = "3", name = "Manga3")

    return listOf(chapter1, chapter2, chapter3)
  }

  private fun getTestPages(): List<SPage> {
    return listOf(
      SPage("url1", "imageUrl1"),
      SPage("url2", "imageUrl2")
    )
  }

}
