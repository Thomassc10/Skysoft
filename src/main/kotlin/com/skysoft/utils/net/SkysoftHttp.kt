package com.skysoft.utils.net

import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Duration
import java.util.concurrent.CompletableFuture

object SkysoftHttp {
    private val client: HttpClient = HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(10))
        .build()

    fun getString(
        url: String,
        timeout: Duration = Duration.ofSeconds(DEFAULT_REQUEST_TIMEOUT_SECONDS),
    ): CompletableFuture<String> {
        val request = HttpRequest.newBuilder(URI.create(url))
            .timeout(timeout)
            .header("User-Agent", "Skysoft")
            .GET()
            .build()

        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
            .thenApply { response ->
                if (response.statusCode() !in 200..299) {
                    throw IllegalStateException("GET $url returned HTTP ${response.statusCode()}")
                }
                response.body()
            }
    }

    fun getBytes(
        url: String,
        timeout: Duration = Duration.ofSeconds(DEFAULT_REQUEST_TIMEOUT_SECONDS),
    ): CompletableFuture<ByteArray> {
        val request = HttpRequest.newBuilder(URI.create(url))
            .timeout(timeout)
            .header("User-Agent", "Skysoft")
            .GET()
            .build()

        return client.sendAsync(request, HttpResponse.BodyHandlers.ofByteArray())
            .thenApply { response ->
                if (response.statusCode() !in 200..299) {
                    throw IllegalStateException("GET $url returned HTTP ${response.statusCode()}")
                }
                response.body()
            }
    }

    private const val DEFAULT_REQUEST_TIMEOUT_SECONDS = 30L
}

class PendingHttpRequests {
    private val pendingRequests = mutableSetOf<CompletableFuture<*>>()

    fun getString(url: String): CompletableFuture<String> = track(SkysoftHttp.getString(url))

    fun cancelAll() {
        val requests = synchronized(pendingRequests) {
            pendingRequests.toList().also {
                pendingRequests.clear()
            }
        }
        requests.forEach { it.cancel(true) }
    }

    private fun <T> track(future: CompletableFuture<T>): CompletableFuture<T> {
        synchronized(pendingRequests) {
            pendingRequests += future
        }
        future.whenComplete { _, _ ->
            synchronized(pendingRequests) {
                pendingRequests -= future
            }
        }
        return future
    }
}
