package tachiyomi.source

import tachiyomi.source.model.ChapterMeta
import tachiyomi.source.model.Filter
import tachiyomi.source.model.FilterList
import tachiyomi.source.model.MangaMeta
import tachiyomi.source.model.MangasPageMeta
import tachiyomi.source.model.PageMeta

class TestSource : CatalogSource {

  override val id = 1L
  override val name = "Test source"
  override val lang get() = "en"

  override fun fetchMangaDetails(manga: MangaMeta): MangaMeta {
    Thread.sleep(1000)
    val noHipstersOffset = 10
    val picId = manga.title.split(" ")[1].toInt() + noHipstersOffset
    return manga.copy(cover = "https://picsum.photos/300/400/?image=$picId", initialized = true)
  }

  override fun fetchMangaList(
    page: Int,
    query: String,
    filters: FilterList
  ): MangasPageMeta {
//    Thread.sleep(1500)
    val filteredMangas = getTestManga(page).filter { query in it.title }
    return MangasPageMeta(filteredMangas, false)
  }

  override fun fetchChapterList(manga: MangaMeta): List<ChapterMeta> {
    Thread.sleep(1000)
    return getTestChapters()
  }

  override fun fetchPageList(chapter: ChapterMeta): List<PageMeta> {
    Thread.sleep(1000)
    return getTestPages()
  }

  private class Status : Filter.TriState("Completed")
  private class Author : Filter.Text("Author")
  private class Genre(name: String) : Filter.TriState(name)
  private class GenreList(genres: List<Genre>) : Filter.Group<Genre>("Genres", genres)

  private fun getGenreList() = listOf(
    Genre("Action"),
    Genre("Adventure"),
    Genre("Comedy")
  )

  override fun getFilterList(): FilterList {
    return listOf(Status(), Author(), GenreList(getGenreList()))
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
    val chapter2 = chapter1.copy(key = "2", name = "Chapter2")
    val chapter3 = chapter1.copy(key = "3", name = "Chapter3")

    return listOf(chapter1, chapter2, chapter3)
  }

  private fun getTestPages(): List<PageMeta> {
    return listOf(
      PageMeta("url1", "imageUrl1"),
      PageMeta("url2", "imageUrl2")
    )
  }

}
