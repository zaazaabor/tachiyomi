package tachiyomi.data.manga.util

import tachiyomi.data.manga.model.NewManga
import tachiyomi.domain.manga.Manga
import tachiyomi.domain.source.SManga

internal fun SManga.asNewManga(sourceId: Long): NewManga {
  return NewManga(
    source = sourceId,
    url = url,
    title = title,
    artist = artist,
    author = author,
    description = description,
    genre = genre,
    status = status,
    cover = cover,
    initialized = initialized,
    favorite = false,
    lastUpdate = 0,
    viewer = 0,
    flags = 0
  )
}

internal fun NewManga.asDbManga(mangaId: Long): Manga {
  return Manga(
    id = mangaId,
    source = source,
    url = url,
    title = title,
    artist = artist,
    author = author,
    description = description,
    genre = genre,
    status = status,
    cover = cover,
    initialized = initialized,
    favorite = false,
    lastUpdate = 0,
    viewer = 0,
    flags = 0
  )
}
