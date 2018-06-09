package tachiyomi.source

import tachiyomi.source.model.ChapterMeta
import tachiyomi.source.model.Filter
import tachiyomi.source.model.MangaMeta
import tachiyomi.source.model.MangasPageMeta
import tachiyomi.source.model.PageMeta
import tachiyomi.source.model.SearchQuery
import tachiyomi.source.model.Sorting

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

  override fun fetchMangaList(query: SearchQuery, page: Int): MangasPageMeta {
    val filteredMangas = getTestManga(page).filter { query.query in it.title }
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

  inner class Alphabetically : Sorting {
    override val name = "Alphabetically"
    override fun getFilters() = listOf(Status(), Author(), GenreList(getGenreList()))
  }

  inner class Latest : Sorting {
    override val name = "Latest"
    override fun getFilters() = listOf(Status(), GenreList(getGenreList()))
  }

  override fun getSortings(): List<Sorting> {
    return listOf(Alphabetically(), Latest())
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
