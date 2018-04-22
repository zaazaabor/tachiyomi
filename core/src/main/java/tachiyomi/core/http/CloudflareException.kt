package tachiyomi.core.http

/**
 * Custom exception for errors related to Cloudflare.
 */
@Suppress("unused")
class CloudflareException : Exception {

  constructor() : super()

  constructor(message: String) : super(message)

  constructor(cause: Exception) : super(cause)

  constructor(message: String, cause: Exception) : super(message, cause)

}
