package com.example.hydrogram.presentation.util

import android.content.Context
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.util.Log
import android.util.LruCache
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.withContext
import java.util.HashMap

object VideoThumbnailExtractor {

    private val cache = LruCache<String, Bitmap>(30)
    private val semaphore = Semaphore(2)

    fun get(messageId: String): Bitmap? = cache.get(messageId)

    suspend fun extract(
        context: Context,
        messageId: String,
        url: String,
    ): Bitmap? = withContext(Dispatchers.IO) {
        cache.get(messageId)?.let { return@withContext it }
        if (url.isBlank()) return@withContext null

        semaphore.withPermit {
            val retriever = MediaMetadataRetriever()
            try {
                if (url.startsWith("http://") || url.startsWith("https://")) {
                    retriever.setDataSource(url, HashMap<String, String>())
                } else {
                    retriever.setDataSource(context, Uri.parse(url))
                }

                val frame = retriever.getFrameAtTime(
                    0L,
                    MediaMetadataRetriever.OPTION_CLOSEST_SYNC
                )

                if (frame != null) {
                    cache.put(messageId, frame)
                }
                frame
            } catch (e: Exception) {
                Log.e("VideoThumbnail", "Failed to extract for $url", e)
                null
            } finally {
                try {
                    retriever.release()
                } catch (_: Exception) {
                }
            }
        }
    }

    fun clear() {
        cache.evictAll()
    }
}