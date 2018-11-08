package tachiyomi.ui.deeplink

data class MangaDeepLinkViewState(
  val loading: Boolean = true,
  val mangaId: Long? = null,
  val error: Throwable? = null
)
