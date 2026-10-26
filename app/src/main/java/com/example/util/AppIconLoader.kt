package com.example.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap

/**
 * Memory-safe LRU icon loader for low-RAM devices (such as Samsung Galaxy J1 with 512MB-768MB RAM).
 * Keeps a maximum of 24 downscaled 48x48 bitmaps in memory to prevent OutOfMemoryError.
 */
object AppIconLoader {
    private const val MAX_CACHE_SIZE = 24
    private val memoryCache = object : LinkedHashMap<String, ImageBitmap>(MAX_CACHE_SIZE, 0.75f, true) {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<String, ImageBitmap>?): Boolean {
            return size > MAX_CACHE_SIZE
        }
    }

    fun loadIcon(context: Context, packageName: String): ImageBitmap? {
        synchronized(memoryCache) {
            val cached = memoryCache[packageName]
            if (cached != null) return cached
        }

        return try {
            val pm = context.packageManager
            val drawable = pm.getApplicationIcon(packageName)
            val size = 64 // 64x64 is crisp on J1's WVGA 480x800 display and takes only 16KB per icon
            val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            drawable.setBounds(0, 0, size, size)
            drawable.draw(canvas)
            val imageBitmap = bitmap.asImageBitmap()

            synchronized(memoryCache) {
                memoryCache[packageName] = imageBitmap
            }
            imageBitmap
        } catch (oom: OutOfMemoryError) {
            AppLog.w("AppIconLoader", "OOM when loading icon for $packageName, clearing cache")
            synchronized(memoryCache) {
                memoryCache.clear()
            }
            null
        } catch (t: Throwable) {
            AppLog.w("AppIconLoader", "Failed to load icon for $packageName", t)
            null
        }
    }
}
