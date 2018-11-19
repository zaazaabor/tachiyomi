package tachiyomi.data.source

import android.app.Application
import tachiyomi.data.extension.ExtensionManager
import tachiyomi.domain.source.SourceManager
import toothpick.ProvidesSingletonInScope
import javax.inject.Inject
import javax.inject.Provider
import javax.inject.Singleton

@Singleton
@ProvidesSingletonInScope
internal class SourceManagerProvider @Inject constructor(
  private val context: Application,
  private val extensionManager: ExtensionManager
) : Provider<SourceManager> {

  override fun get(): SourceManager {
    val sourceManager = SourceManagerImpl(context)
    extensionManager.init(sourceManager)
    return sourceManager
  }

}
