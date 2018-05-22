package tachiyomi.source

interface DeepLinkSource : Source {

  fun handlesLink(url: String): DeepLink?

  fun findMangaKey(chapterKey: String): String?

}
