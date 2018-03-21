package tachiyomi.domain.source

interface SourceManager {

  fun get(key: Long): Source?

  fun registerSource(source: Source, overwrite: Boolean = false)

  fun unregisterSource(source: Source)
}
