package tachiyomi.domain.source

import tachiyomi.source.CatalogSource
import tachiyomi.source.Source

interface SourceManager {

  fun get(key: Long): Source?

  fun getSources(): List<CatalogSource>

  fun registerSource(source: Source, overwrite: Boolean = false)

  fun unregisterSource(source: Source)
}
