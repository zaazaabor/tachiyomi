package tachiyomi.data.source

import android.content.Context
import tachiyomi.domain.source.Source
import tachiyomi.domain.source.SourceManager
import javax.inject.Inject

class SourceManagerImpl @Inject constructor(
  private val context: Context
) : SourceManager {

  private val sources = mutableMapOf<Long, Source>()

  override fun get(key: Long): Source? {
    return sources[key]
  }

  override fun registerSource(source: Source, overwrite: Boolean) {
    if (overwrite || !sources.containsKey(source.id)) {
      sources[source.id] = source
    }
  }

  override fun unregisterSource(source: Source) {
    sources.remove(source.id)
  }
}
