package tachiyomi.ext.all.mangadex

import com.github.salomonbrys.kotson.forEach
import com.github.salomonbrys.kotson.string
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import tachiyomi.core.http.GET
import tachiyomi.source.Dependencies
import tachiyomi.source.HttpSource
import tachiyomi.source.model.ChapterMeta
import tachiyomi.source.model.Filter
import tachiyomi.source.model.FilterList
import tachiyomi.source.model.MangaMeta
import tachiyomi.source.model.MangasPageMeta
import tachiyomi.source.model.PageMeta
import java.net.URLEncoder
import java.util.Date
import java.util.concurrent.TimeUnit
import kotlin.collections.set

open class Mangadex(
  private val deps: Dependencies
) : HttpSource(deps) {

  override val name = "MangaDex"

  override val baseUrl = "https://mangadex.org"

  override val client = clientBuilder(ALL)

  override val lang: String
    get() = "en"

  private val langCode get() = 1

  private fun clientBuilder(r18Toggle: Int): OkHttpClient = deps.http.cloudflareClient.newBuilder()
    .connectTimeout(10, TimeUnit.SECONDS)
    .readTimeout(30, TimeUnit.SECONDS)
    .addNetworkInterceptor { chain ->
      val newReq = chain
        .request()
        .newBuilder()
        .addHeader("Cookie", cookiesHeader(r18Toggle, langCode))
        .addHeader("User-Agent", "Mozilla/5.0 (Windows NT 6.3; WOW64)")
        .build()
      chain.proceed(newReq)
    }.build()!!

  private fun cookiesHeader(r18Toggle: Int, langCode: Int): String {
    val cookies = mutableMapOf<String, String>()
    cookies["mangadex_h_toggle"] = r18Toggle.toString()
    cookies["mangadex_filter_langs"] = langCode.toString()
    return buildCookies(cookies)
  }

  private fun buildCookies(cookies: Map<String, String>) =
    cookies.entries.joinToString(separator = "; ", postfix = ";") {
      "${URLEncoder.encode(it.key, "UTF-8")}=${URLEncoder.encode(it.value, "UTF-8")}"
    }

  private fun popularMangaSelector() = "div.col-sm-6"

  override fun getMangaListRequest(
    page: Int,
    query: String,
    filters: FilterList
  ): Request {
    return GET("$baseUrl/titles/0/$page", headers)
  }

  override fun parseMangaListResponse(response: Response): MangasPageMeta {
    val document = Jsoup.parse(response.body()!!.string(), response.request().url().toString())

    val mangas = document.select(popularMangaSelector()).map { element ->
      popularMangaFromElement(element)
    }

    val hasNextPage = popularMangaNextPageSelector().let { selector ->
      document.select(selector).first()
    } != null

    return MangasPageMeta(mangas, hasNextPage)
  }

  private fun popularMangaFromElement(element: Element): MangaMeta {
    val titleElement = element.select("a.manga_title").first()
    val coverElement = element.select("div.large_logo img").first()

    return MangaMeta(
      key = removeMangaNameFromUrl(titleElement.attr("href")),
      title = titleElement.text().trim(),
      cover = baseUrl + coverElement.attr("src")
    )
  }

  private fun removeMangaNameFromUrl(url: String): String = url.substringBeforeLast("/") + "/"

  private fun popularMangaNextPageSelector() =
    ".pagination li:not(.disabled) span[title*=last page]:not(disabled)"

  private fun apiRequest(manga: MangaMeta): Request {
    return GET(baseUrl + URL + getMangaId(manga.key), headers)
  }

  private fun getMangaId(url: String): String {
    val lastSection = url.trimEnd('/').substringAfterLast("/")
    return if (lastSection.toIntOrNull() != null) {
      lastSection
    } else {
      //this occurs if person has manga from before that had the id/name/
      url.trimEnd('/').substringBeforeLast("/").substringAfterLast("/")
    }
  }

  override fun parseMangaDetailsResponse(response: Response): MangaMeta {
//    val manga = MangaMeta.create()
//    var jsonData = response.body()!!.string()
//    val json = JsonParser().parse(jsonData).asJsonObject
//    val mangaJson = json.getAsJsonObject("manga")
//    manga.thumbnail_url = baseUrl + mangaJson.get("cover_url").string
//    manga.description = cleanString(mangaJson.get("description").string)
//    manga.author = mangaJson.get("author").string
//    manga.artist = mangaJson.get("artist").string
//    manga.status = parseStatus(mangaJson.get("status").int)
//    var genres = mutableListOf<String>()
//
//    mangaJson.get("genres").asJsonArray.forEach { id ->
//      getGenreList().find { it -> it.id == id.string }?.let { genre ->
//        genres.add(genre.name)
//      }
//    }
//    manga.genre = genres.joinToString(", ")
//    return manga
    TODO()
  }

  //remove bbcode as well as parses any html characters in description or chapter name to actual characters for example &hearts will show a heart
  private fun cleanString(description: String): String {
    return Jsoup.parseBodyFragment(description.replace("[list]", "").replace("[/list]", "").replace("[*]", "").replace("""\[(\w+)[^\]]*](.*?)\[/\1]""".toRegex(), "$2")).text()
  }

  /**
   * Parses the response from the site and returns the absolute url to the source image.
   *
   * @param response the response from the site.
   */
  override fun parseImageUrlResponse(response: Response): String {
    TODO()
  }

  override fun parseChapterResponse(response: Response): List<ChapterMeta> {
    val now = Date().time
    var jsonData = response.body()!!.string()
    val json = JsonParser().parse(jsonData).asJsonObject
    val chapterJson = json.getAsJsonObject("chapter")
    val chapters = mutableListOf<ChapterMeta>()

    //skip chapters that dont match the desired language, or are future releases
    chapterJson?.forEach { key, jsonElement ->
      val chapterElement = jsonElement.asJsonObject
      if (chapterElement.get("lang_code").string == "en" &&
          (chapterElement.get("timestamp").asLong * 1000) <= now) {

        chapterElement.toString()
        chapters.add(chapterFromJson(key, chapterElement))
      }
    }
    return chapters
  }

  private fun chapterFromJson(chapterId: String, chapterJson: JsonObject): ChapterMeta {
//    val chapter = ChapterMeta.create()
//    chapter.url = BASE_CHAPTER + chapterId
//    var chapterName = mutableListOf<String>()
//    //build chapter name
//    if (chapterJson.get("volume").string.isNotBlank()) {
//      chapterName.add("Vol." + chapterJson.get("volume").string)
//    }
//    if (chapterJson.get("chapter").string.isNotBlank()) {
//      chapterName.add("Ch." + chapterJson.get("chapter").string)
//    }
//    if (chapterJson.get("title").string.isNotBlank()) {
//      chapterName.add("-")
//      chapterName.add(chapterJson.get("title").string)
//    }
//
//    chapter.name = cleanString(chapterName.joinToString(" "))
//    //convert from unix time
//    chapter.date_upload = chapterJson.get("timestamp").long * 1000
//    var scanlatorName = mutableListOf<String>()
//    if (!chapterJson.get("group_name").nullString.isNullOrBlank()) {
//      scanlatorName.add(chapterJson.get("group_name").string)
//    }
//    if (!chapterJson.get("group_name_2").nullString.isNullOrBlank()) {
//      scanlatorName.add(chapterJson.get("group_name_2").string)
//    }
//    if (!chapterJson.get("group_name_3").nullString.isNullOrBlank()) {
//      scanlatorName.add(chapterJson.get("group_name_3").string)
//    }
//    chapter.scanlator = scanlatorName.joinToString(" & ")
//
//    return chapter
    TODO()
  }

//  override fun chapterFromElement(element: Element) = throw Exception("Not used")

  /**
   * Parses the response from the site and returns a list of pages.
   *
   * @param response the response from the site.
   */
  override fun parsePageList(response: Response): List<PageMeta> {
    TODO()
//    val pages = mutableListOf<PageMeta>()
//    val url = document.baseUri()
//
//    val dataUrl = document.select("script").last().html().substringAfter("dataurl = '").substringBefore("';")
//    val imageUrl = document.select("script").last().html().substringAfter("page_array = [").substringBefore("];")
//    val listImageUrls = imageUrl.replace("'", "").split(",")
//    val server = document.select("script").last().html().substringAfter("server = '").substringBefore("';")
//
//    listImageUrls.filter { it.isNotBlank() }.forEach {
//      val url = "$server$dataUrl/$it"
//      pages.add(PageMeta("", getImageUrl(url)))
//    }
//
//    return pages
  }

//  override fun imageUrlParse(document: Document): String = ""

  private fun parseStatus(status: Int) = when (status) {
    1 -> MangaMeta.ONGOING
    2 -> MangaMeta.COMPLETED
    else -> MangaMeta.UNKNOWN
  }

  private fun getImageUrl(attr: String): String {
    //some images are hosted elsewhere
    if (attr.startsWith("http")) {
      return attr
    }
    return baseUrl + attr
  }

  private class TextField(name: String, val key: String) : Filter.Text(name)
  private class Genre(val id: String, name: String) : Filter.CheckBox(name)
  private class GenreList(genres: List<Genre>) : Filter.Group<Genre>("Genres", genres)
  private class R18 : Filter.Select<String>("R18+", arrayOf("Show all", "Show only", "Show none"))

  override fun getFilterList() = listOf(
    TextField("Author", "author"),
    TextField("Artist", "artist"),
    R18(),
    GenreList(getGenreList())
  )

  private fun getGenreList() = listOf(
    Genre("1", "4-koma"),
    Genre("2", "Action"),
    Genre("3", "Adventure"),
    Genre("4", "Award Winning"),
    Genre("5", "Comedy"),
    Genre("6", "Cooking"),
    Genre("7", "Doujinshi"),
    Genre("8", "Drama"),
    Genre("9", "Ecchi"),
    Genre("10", "Fantasy"),
    Genre("11", "Gender Bender"),
    Genre("12", "Harem"),
    Genre("13", "Historical"),
    Genre("14", "Horror"),
    Genre("15", "Josei"),
    Genre("16", "Martial Arts"),
    Genre("17", "Mecha"),
    Genre("18", "Medical"),
    Genre("19", "Music"),
    Genre("20", "Mystery"),
    Genre("21", "Oneshot"),
    Genre("22", "Psychological"),
    Genre("23", "Romance"),
    Genre("24", "School Life"),
    Genre("25", "Sci-Fi"),
    Genre("26", "Seinen"),
    Genre("27", "Shoujo"),
    Genre("28", "Shoujo Ai"),
    Genre("29", "Shounen"),
    Genre("30", "Shounen Ai"),
    Genre("31", "Slice of Life"),
    Genre("32", "Smut"),
    Genre("33", "Sports"),
    Genre("34", "Supernatural"),
    Genre("35", "Tragedy"),
    Genre("36", "Webtoon"),
    Genre("37", "Yaoi"),
    Genre("38", "Yuri"),
    Genre("39", "[no chapters]"),
    Genre("40", "Game"),
    Genre("41", "Isekai")
  )

  companion object {
    //this number matches to the cookie
    private const val NO_R18 = 0
    private const val ALL = 1
    private const val ONLY_R18 = 2
    private const val URL = "/api/3640f3fb/"
    private const val BASE_CHAPTER = "/chapter/"

  }
}
