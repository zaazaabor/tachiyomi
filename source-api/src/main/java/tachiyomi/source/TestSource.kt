package tachiyomi.source

import tachiyomi.source.model.ChapterMeta
import tachiyomi.source.model.Filter
import tachiyomi.source.model.FilterList
import tachiyomi.source.model.Listing
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

  override fun fetchMangaList(sort: Listing?, page: Int): MangasPageMeta {
    return MangasPageMeta(getTestManga(1), true)
  }

  override fun fetchMangaList(filters: FilterList, page: Int): MangasPageMeta {
    //.filter { query.query in it.title }
    return MangasPageMeta(getTestManga(1), true)
  }

  override fun fetchChapterList(manga: MangaMeta): List<ChapterMeta> {
    Thread.sleep(1000)
    return getTestChapters()
  }

  override fun fetchPageList(chapter: ChapterMeta): List<PageMeta> {
    Thread.sleep(1000)
    return getTestPages()
  }

  class Alphabetically : Listing("Alphabetically")

  class Latest : Listing("Latest")

  override fun getListings(): List<Listing> {
    return listOf(Alphabetically(), Latest())
  }

  override fun getFilters(): FilterList {
    return listOf(
      Filter.Title(),
      Filter.Author(),
      Filter.Artist(),
      GenreList(getGenreList())
    )
  }

//  private class Status : Filter.TriState("Completed")
//  private class Author : Filter.Text("Author")
//  private class Genre(name: String) : Filter.TriState(name)
  private class GenreList(genres: List<Filter.Genre>) : Filter.Group<Filter.Genre>("Genres", genres)

  private fun getGenreList() = listOf(
    Filter.GenreCheckBox("4-koma"),
    Filter.GenreCheckBox("Action"),
    Filter.GenreCheckBox("Adventure"),
    Filter.GenreCheckBox("Award Winning"),
    Filter.GenreCheckBox("Comedy"),
    Filter.GenreCheckBox("Cooking"),
    Filter.GenreCheckBox("Doujinshi"),
    Filter.GenreCheckBox("Drama"),
    Filter.GenreCheckBox("Ecchi"),
    Filter.GenreCheckBox("Fantasy"),
    Filter.GenreCheckBox("Gender Bender"),
    Filter.GenreCheckBox("Harem"),
    Filter.GenreCheckBox("Historical"),
    Filter.GenreCheckBox("Horror"),
    Filter.GenreCheckBox("Josei"),
    Filter.GenreCheckBox("Martial Arts"),
    Filter.GenreCheckBox("Mecha"),
    Filter.GenreCheckBox("Medical"),
    Filter.GenreCheckBox("Music"),
    Filter.GenreCheckBox("Mystery"),
    Filter.GenreCheckBox("Oneshot"),
    Filter.GenreCheckBox("Psychological"),
    Filter.GenreCheckBox("Romance"),
    Filter.GenreCheckBox("School Life"),
    Filter.GenreCheckBox("Sci-Fi"),
    Filter.GenreCheckBox("Seinen"),
    Filter.GenreCheckBox("Shoujo"),
    Filter.GenreCheckBox("Shoujo Ai"),
    Filter.GenreCheckBox("Shounen"),
    Filter.GenreCheckBox("Shounen Ai"),
    Filter.GenreCheckBox("Slice of Life"),
    Filter.GenreCheckBox("Smut"),
    Filter.GenreCheckBox("Sports"),
    Filter.GenreCheckBox("Supernatural"),
    Filter.GenreCheckBox("Tragedy"),
    Filter.GenreCheckBox("Webtoon"),
    Filter.GenreCheckBox("Yaoi"),
    Filter.GenreCheckBox("Yuri"),
    Filter.GenreCheckBox("[no chapters]"),
    Filter.GenreCheckBox("Game"),
    Filter.GenreCheckBox("Isekai")
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
