package tachiyomi.core.http

interface ProgressListener {
  fun update(bytesRead: Long, contentLength: Long, done: Boolean)
}
