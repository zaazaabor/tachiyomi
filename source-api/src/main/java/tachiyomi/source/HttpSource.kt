package tachiyomi.source

import okhttp3.Headers
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import tachiyomi.core.http.GET
import tachiyomi.source.model.ChapterMeta
import tachiyomi.source.model.FilterList
import tachiyomi.source.model.MangaMeta
import tachiyomi.source.model.MangasPageMeta
import tachiyomi.source.model.PageMeta
import java.lang.Exception
import java.net.URI
import java.net.URISyntaxException
import java.security.MessageDigest

/**
 * A simple implementation for sources from a website.
 */
@Suppress("unused", "unused_parameter")
abstract class HttpSource(private val dependencies: Dependencies) : CatalogSource {

  /**
   * Base url of the website without the trailing slash, like: http://mysite.com
   */
  abstract val baseUrl: String

  /**
   * Version id used to generate the source id. If the site completely changes and urls are
   * incompatible, you may increase this value and it'll be considered as a new source.
   */
  open val versionId = 1

  /**
   * Id of the source. By default it uses a generated id using the first 16 characters (64 bits)
   * of the MD5 of the string: sourcename/language/versionId
   * Note the generated id sets the sign bit to 0.
   */
  override val id by lazy {
    val key = "${name.toLowerCase()}/$lang/$versionId"
    val bytes = MessageDigest.getInstance("MD5").digest(key.toByteArray())
    (0..7).map { bytes[it].toLong() and 0xff shl 8 * (7 - it) }.reduce(Long::or) and Long.MAX_VALUE
  }

  /**
   * Headers used for requests.
   */
  val headers: Headers by lazy { headersBuilder().build() }

  /**
   * Default network client for doing requests.
   */
  open val client: OkHttpClient
    get() = dependencies.http.defaultClient

  /**
   * Headers builder for requests. Implementations can override this method for custom headers.
   */
  protected open fun headersBuilder() = Headers.Builder().apply {
    add("User-Agent", "Mozilla/5.0 (Windows NT 6.3; WOW64)")
  }

  /**
   * Visible name of the source.
   */
  override fun toString() = "$name (${lang.toUpperCase()})"

  /**
   * Returns an observable containing a page with a list of manga. Normally it's not needed to
   * override this method.
   *
   * @param page the page number to retrieve.
   */
  override fun fetchMangaList(page: Int): MangasPageMeta {
    return client.newCall(popularMangaRequest(page))
      .execute()
      .let(::popularMangaParse)
  }

  /**
   * Returns the request for the popular manga given the page.
   *
   * @param page the page number to retrieve.
   */
  protected abstract fun popularMangaRequest(page: Int): Request

  /**
   * Parses the response from the site and returns a [MangasPageMeta] object.
   *
   * @param response the response from the site.
   */
  protected abstract fun popularMangaParse(response: Response): MangasPageMeta

  /**
   * Returns an observable containing a page with a list of manga. Normally it's not needed to
   * override this method.
   *
   * @param page the page number to retrieve.
   * @param query the search query.
   * @param filters the list of filters to apply.
   */
  override fun searchMangaList(page: Int, query: String, filters: FilterList): MangasPageMeta {
    return client.newCall(searchMangaRequest(page, query, filters))
      .execute()
      .let(::searchMangaParse)
  }

  /**
   * Returns the request for the search manga given the page.
   *
   * @param page the page number to retrieve.
   * @param query the search query.
   * @param filters the list of filters to apply.
   */
  protected abstract fun searchMangaRequest(page: Int, query: String, filters: FilterList): Request

  /**
   * Parses the response from the site and returns a [MangasPageMeta] object.
   *
   * @param response the response from the site.
   */
  protected abstract fun searchMangaParse(response: Response): MangasPageMeta

  /**
   * Returns an observable with the updated details for a manga. Normally it's not needed to
   * override this method.
   *
   * @param manga the manga to be updated.
   */
  override fun fetchMangaDetails(manga: MangaMeta): MangaMeta {
    return client.newCall(mangaDetailsRequest(manga))
      .execute()
      .let(::mangaDetailsParse)
  }

  /**
   * Returns the request for the details of a manga. Override only if it's needed to change the
   * url, send different headers or request method like POST.
   *
   * @param manga the manga to be updated.
   */
  open fun mangaDetailsRequest(manga: MangaMeta): Request {
    return GET(baseUrl + manga.key, headers)
  }

  /**
   * Parses the response from the site and returns the details of a manga.
   *
   * @param response the response from the site.
   */
  protected abstract fun mangaDetailsParse(response: Response): MangaMeta

  /**
   * Returns an observable with the updated chapter list for a manga. Normally it's not needed to
   * override this method.  If a manga is licensed an empty chapter list observable is returned
   *
   * @param manga the manga to look for chapters.
   */
  override fun fetchChapterList(manga: MangaMeta): List<ChapterMeta> {
    if (manga.status != MangaMeta.LICENSED) {
      return client.newCall(chapterListRequest(manga))
        .execute()
        .let(::chapterListParse)
    } else {
      throw Exception("Licensed - No chapters to show")
    }
  }

  /**
   * Returns the request for updating the chapter list. Override only if it's needed to override
   * the url, send different headers or request method like POST.
   *
   * @param manga the manga to look for chapters.
   */
  protected open fun chapterListRequest(manga: MangaMeta): Request {
    return GET(baseUrl + manga.key, headers)
  }

  /**
   * Parses the response from the site and returns a list of chapters.
   *
   * @param response the response from the site.
   */
  protected abstract fun chapterListParse(response: Response): List<ChapterMeta>

  /**
   * Returns an observable with the page list for a chapter.
   *
   * @param chapter the chapter whose page list has to be fetched.
   */
  override fun fetchPageList(chapter: ChapterMeta): List<PageMeta> {
    return client.newCall(pageListRequest(chapter))
      .execute()
      .let(::pageListParse)
  }

  /**
   * Returns the request for getting the page list. Override only if it's needed to override the
   * url, send different headers or request method like POST.
   *
   * @param chapter the chapter whose page list has to be fetched.
   */
  protected open fun pageListRequest(chapter: ChapterMeta): Request {
    return GET(baseUrl + chapter.key, headers)
  }

  /**
   * Parses the response from the site and returns a list of pages.
   *
   * @param response the response from the site.
   */
  protected abstract fun pageListParse(response: Response): List<PageMeta>

  /**
   * Returns an observable with the page containing the source url of the image. If there's any
   * error, it will return null instead of throwing an exception.
   *
   * @param page the page whose source image has to be fetched.
   */
  open fun fetchImageUrl(page: PageMeta): String {
    return client.newCall(imageUrlRequest(page))
      .execute()
      .let(::imageUrlParse)
  }

  /**
   * Returns the request for getting the url to the source image. Override only if it's needed to
   * override the url, send different headers or request method like POST.
   *
   * @param page the chapter whose page list has to be fetched
   */
  protected open fun imageUrlRequest(page: PageMeta): Request {
    return GET(page.url, headers)
  }

  /**
   * Parses the response from the site and returns the absolute url to the source image.
   *
   * @param response the response from the site.
   */
  protected abstract fun imageUrlParse(response: Response): String

  /**
   * Returns an observable with the response of the source image.
   *
   * @param page the page whose source image has to be downloaded.
   */
  fun fetchImage(page: PageMeta): Response {
    // TODO progress listener
    return client.newCall(imageRequest(page))
      .execute()
  }

  /**
   * Returns the request for getting the source image. Override only if it's needed to override
   * the url, send different headers or request method like POST.
   *
   * @param page the chapter whose page list has to be fetched
   */
  protected open fun imageRequest(page: PageMeta): Request {
    return GET(page.imageUrl!!, headers)
  }

  /**
   * Assigns the url of the chapter without the scheme and domain. It saves some redundancy from
   * database and the urls could still work after a domain change.
   *
   * @param url the full url to the chapter.
   */
  fun ChapterMeta.setUrlWithoutDomain(url: String) {
    this.key = getUrlWithoutDomain(url)
  }

  /**
   * Assigns the url of the manga without the scheme and domain. It saves some redundancy from
   * database and the urls could still work after a domain change.
   *
   * @param url the full url to the manga.
   */
  fun MangaMeta.setUrlWithoutDomain(url: String) {
    // TODO everything is immutable
//    this.key = getUrlWithoutDomain(url)
  }

  /**
   * Returns the url of the given string without the scheme and domain.
   *
   * @param orig the full url.
   */
  private fun getUrlWithoutDomain(orig: String): String {
    return try {
      val uri = URI(orig)
      var out = uri.path
      if (uri.query != null)
        out += "?" + uri.query
      if (uri.fragment != null)
        out += "#" + uri.fragment
      out
    } catch (e: URISyntaxException) {
      orig
    }
  }

  /**
   * Called before inserting a new chapter into database. Use it if you need to override chapter
   * fields, like the title or the chapter number. Do not change anything to [manga].
   *
   * @param chapter the chapter to be added.
   * @param manga the manga of the chapter.
   */
  open fun prepareNewChapter(chapter: ChapterMeta, manga: MangaMeta) {
  }

  /**
   * Returns the list of filters for the source.
   */
  override fun getFilterList(): FilterList {
    return emptyList()
  }
}
