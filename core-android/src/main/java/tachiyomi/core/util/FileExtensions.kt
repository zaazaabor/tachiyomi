package tachiyomi.core.util

import android.content.Context
import android.net.Uri
import android.os.Build
import android.support.v4.content.FileProvider
import tachiyomi.core.android.BuildConfig
import java.io.File

/**
 * Returns the uri of a file
 *
 * @param context context of application
 */
fun File.getUriCompat(context: Context): Uri {
  // TODO check the application id matches the provider
  return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
    FileProvider.getUriForFile(context, BuildConfig.APPLICATION_ID + ".provider", this)
  else Uri.fromFile(this)
}

