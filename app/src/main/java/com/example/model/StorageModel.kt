package com.example.model

data class StorageSpaceInfo(
    val title: String,
    val totalBytes: Long,
    val freeBytes: Long,
    val usedBytes: Long,
    val percentageUsed: Int,
    val isMounted: Boolean,
    val path: String,
    val isRemovable: Boolean = false
) {
    companion object {
        val EMPTY_INTERNAL = StorageSpaceInfo(
            title = "حافظه داخلی دستگاه",
            totalBytes = 0L,
            freeBytes = 0L,
            usedBytes = 0L,
            percentageUsed = 0,
            isMounted = true,
            path = "در حال بارگذاری…",
            isRemovable = false
        )

        val EMPTY_SD = StorageSpaceInfo(
            title = "کارت حافظه جانبی (MicroSD)",
            totalBytes = 0L,
            freeBytes = 0L,
            usedBytes = 0L,
            percentageUsed = 0,
            isMounted = false,
            path = "در حال بررسی…",
            isRemovable = true
        )
    }
}

data class RamInfo(
    val totalBytes: Long,
    val freeBytes: Long,
    val usedBytes: Long,
    val percentageUsed: Int
) {
    companion object {
        val EMPTY = RamInfo(0L, 0L, 0L, 0)
    }
}

/**
 * Lightweight application model. Note: Does not hold large android.graphics.drawable.Drawable
 * objects in memory to prevent OutOfMemoryError crashes on devices with <= 768MB RAM (e.g. Galaxy J1).
 */
data class AppStorageInfo(
    val packageName: String,
    val appName: String,
    val isSystemApp: Boolean,
    val isInstalledOnSd: Boolean,
    val canBeMovedToSd: Boolean,
    val installLocation: Int,
    val apkSizeBytes: Long,
    val versionName: String
)

data class StorageFileItem(
    val name: String,
    val path: String,
    val sizeBytes: Long,
    val fileType: FileType,
    val lastModified: Long
)

enum class FileType(val labelFa: String) {
    VIDEO("ویدیو"),
    IMAGE("تصویر"),
    AUDIO("صدا/موزیک"),
    APK("فایل نصبی APK"),
    DOCUMENT("سند"),
    ARCHIVE("فایل فشرده"),
    TEMP("فایل موقت"),
    OTHER("متفرقه")
}

data class JunkScanResult(
    val cacheBytes: Long = 0L,
    val tempFilesBytes: Long = 0L,
    val emptyFoldersCount: Int = 0,
    val totalCleanableBytes: Long = 0L,
    val largeFiles: List<StorageFileItem> = emptyList(),
    val duplicateCandidates: List<StorageFileItem> = emptyList(),
    val isScanned: Boolean = false
)
