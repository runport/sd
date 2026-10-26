package com.example.model

import android.graphics.drawable.Drawable

data class StorageSpaceInfo(
    val title: String,
    val totalBytes: Long,
    val freeBytes: Long,
    val usedBytes: Long,
    val percentageUsed: Int,
    val isMounted: Boolean,
    val path: String,
    val isRemovable: Boolean = false
)

data class RamInfo(
    val totalBytes: Long,
    val freeBytes: Long,
    val usedBytes: Long,
    val percentageUsed: Int
)

data class AppStorageInfo(
    val packageName: String,
    val appName: String,
    val icon: Drawable?,
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
