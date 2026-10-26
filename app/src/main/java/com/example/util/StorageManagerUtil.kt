package com.example.util

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.StatFs
import android.provider.Settings
import androidx.core.content.ContextCompat
import com.example.model.AppStorageInfo
import com.example.model.FileType
import com.example.model.JunkScanResult
import com.example.model.RamInfo
import com.example.model.StorageFileItem
import com.example.model.StorageSpaceInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.Locale

object StorageManagerUtil {

    /**
     * Convert digits to Persian digits for natural readability
     */
    fun toPersianDigits(text: String): String {
        val persianDigits = charArrayOf('۰', '۱', '۲', '۳', '۴', '۵', '۶', '۷', '۸', '۹')
        val sb = StringBuilder()
        for (ch in text) {
            if (ch in '0'..'9') {
                sb.append(persianDigits[ch - '0'])
            } else {
                sb.append(ch)
            }
        }
        return sb.toString()
    }

    /**
     * Formats bytes to Persian readable string (e.g., ۱.۵ گیگابایت or ۴۵۰ مگابایت)
     */
    fun formatBytes(bytes: Long): String {
        if (bytes <= 0) return "۰ بایت"
        val kb = 1024.0
        val mb = kb * 1024.0
        val gb = mb * 1024.0
        val tb = gb * 1024.0

        val formatted = when {
            bytes >= tb -> String.format(Locale.US, "%.2f ترابایت", bytes / tb)
            bytes >= gb -> String.format(Locale.US, "%.2f گیگابایت", bytes / gb)
            bytes >= mb -> String.format(Locale.US, "%.1f مگابایت", bytes / mb)
            bytes >= kb -> String.format(Locale.US, "%.1f کیلوبایت", bytes / kb)
            else -> "$bytes بایت"
        }
        return toPersianDigits(formatted)
    }

    /**
     * Get internal storage stats using StatFs (Android 5.0+ compatible)
     */
    fun getInternalStorageInfo(): StorageSpaceInfo {
        return try {
            val path = Environment.getDataDirectory()
            val stat = StatFs(path.path)
            val blockSize = stat.blockSizeLong
            val totalBlocks = stat.blockCountLong
            val availableBlocks = stat.availableBlocksLong

            val total = totalBlocks * blockSize
            val free = availableBlocks * blockSize
            val used = total - free
            val percent = if (total > 0) ((used * 100) / total).toInt() else 0

            StorageSpaceInfo(
                title = "حافظه داخلی دستگاه",
                totalBytes = total,
                freeBytes = free,
                usedBytes = used,
                percentageUsed = percent,
                isMounted = true,
                path = path.absolutePath,
                isRemovable = false
            )
        } catch (e: Exception) {
            StorageSpaceInfo(
                title = "حافظه داخلی دستگاه",
                totalBytes = 0L,
                freeBytes = 0L,
                usedBytes = 0L,
                percentageUsed = 0,
                isMounted = false,
                path = "نامشخص",
                isRemovable = false
            )
        }
    }

    /**
     * Get SD Card (microSD / secondary external storage) info
     */
    fun getSdCardStorageInfo(context: Context): StorageSpaceInfo {
        try {
            // ContextCompat.getExternalFilesDirs returns internal external at index 0
            // and physical MicroSD card dirs at index 1+ if present.
            val externalDirs = ContextCompat.getExternalFilesDirs(context, null)
            for (i in externalDirs.indices) {
                val dir = externalDirs[i] ?: continue
                if (Environment.isExternalStorageRemovable(dir) || i > 0) {
                    val stat = StatFs(dir.path)
                    val blockSize = stat.blockSizeLong
                    val totalBlocks = stat.blockCountLong
                    val availableBlocks = stat.availableBlocksLong

                    val total = totalBlocks * blockSize
                    val free = availableBlocks * blockSize
                    val used = total - free
                    val percent = if (total > 0) ((used * 100) / total).toInt() else 0

                    if (total > 0) {
                        return StorageSpaceInfo(
                            title = "کارت حافظه جانبی (MicroSD)",
                            totalBytes = total,
                            freeBytes = free,
                            usedBytes = used,
                            percentageUsed = percent,
                            isMounted = true,
                            path = dir.absolutePath,
                            isRemovable = true
                        )
                    }
                }
            }

            // Fallback check: check secondary mount points common in Android 5 (Lollipop TouchWiz)
            val secondaryPaths = arrayOf(
                "/storage/extSdCard",
                "/storage/sdcard1",
                "/mnt/extSdCard",
                "/mnt/sdcard1",
                "/storage/MicroSD"
            )
            for (p in secondaryPaths) {
                val file = File(p)
                if (file.exists() && file.canRead()) {
                    val stat = StatFs(file.path)
                    val blockSize = stat.blockSizeLong
                    val total = stat.blockCountLong * blockSize
                    val free = stat.availableBlocksLong * blockSize
                    val used = total - free
                    if (total > 0) {
                        val percent = ((used * 100) / total).toInt()
                        return StorageSpaceInfo(
                            title = "کارت حافظه جانبی (MicroSD)",
                            totalBytes = total,
                            freeBytes = free,
                            usedBytes = used,
                            percentageUsed = percent,
                            isMounted = true,
                            path = file.absolutePath,
                            isRemovable = true
                        )
                    }
                }
            }
        } catch (_: Exception) {
        }

        return StorageSpaceInfo(
            title = "کارت حافظه جانبی (MicroSD)",
            totalBytes = 0L,
            freeBytes = 0L,
            usedBytes = 0L,
            percentageUsed = 0,
            isMounted = false,
            path = "شناسایی نشد",
            isRemovable = true
        )
    }

    /**
     * Get device RAM stats using ActivityManager
     */
    fun getRamInfo(context: Context): RamInfo {
        return try {
            val actManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            val memInfo = ActivityManager.MemoryInfo()
            actManager.getMemoryInfo(memInfo)

            val total = memInfo.totalMem
            val free = memInfo.availMem
            val used = total - free
            val percent = if (total > 0) ((used * 100) / total).toInt() else 0

            RamInfo(
                totalBytes = total,
                freeBytes = free,
                usedBytes = used,
                percentageUsed = percent
            )
        } catch (e: Exception) {
            RamInfo(0L, 0L, 0L, 0)
        }
    }

    /**
     * Scan installed packages and classify movable status for Android 5.0+
     */
    suspend fun getInstalledApps(context: Context): List<AppStorageInfo> = withContext(Dispatchers.IO) {
        val pm = context.packageManager
        val packages: List<PackageInfo> = try {
            pm.getInstalledPackages(PackageManager.GET_META_DATA)
        } catch (e: Exception) {
            emptyList()
        }

        val result = mutableListOf<AppStorageInfo>()

        for (pkg in packages) {
            val appInfo = pkg.applicationInfo ?: continue
            val isSystem = (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0
            val isOnSd = (appInfo.flags and ApplicationInfo.FLAG_EXTERNAL_STORAGE) != 0

            // installLocation constants:
            // 0 = INSTALL_LOCATION_AUTO
            // 1 = INSTALL_LOCATION_INTERNAL_ONLY
            // 2 = INSTALL_LOCATION_PREFER_EXTERNAL
            val location = pkg.installLocation

            // On Android 5.0 (Lollipop), non-system apps with location != 1 can be moved to SD.
            // Even if location is AUTO or unset, Samsung Galaxy J1's TouchWiz allows moving if not internal-only.
            val canMove = if (isSystem) {
                false
            } else if (isOnSd) {
                true // already movable and moved
            } else {
                location == PackageInfo.INSTALL_LOCATION_PREFER_EXTERNAL ||
                        location == PackageInfo.INSTALL_LOCATION_AUTO ||
                        location == 0 // Default unspecified in many legacy apps allows move in Android 5
            }

            val appName = try {
                appInfo.loadLabel(pm).toString()
            } catch (e: Exception) {
                pkg.packageName
            }

            val icon = try {
                appInfo.loadIcon(pm)
            } catch (e: Exception) {
                null
            }

            val apkSize = try {
                File(appInfo.sourceDir).length()
            } catch (e: Exception) {
                0L
            }

            result.add(
                AppStorageInfo(
                    packageName = pkg.packageName,
                    appName = appName,
                    icon = icon,
                    isSystemApp = isSystem,
                    isInstalledOnSd = isOnSd,
                    canBeMovedToSd = canMove,
                    installLocation = location,
                    apkSizeBytes = apkSize,
                    versionName = pkg.versionName ?: "1.0"
                )
            )
        }

        // Sort: movable apps first, then user apps, then largest size
        result.sortedWith(
            compareByDescending<AppStorageInfo> { it.canBeMovedToSd && !it.isInstalledOnSd }
                .thenByDescending { !it.isSystemApp }
                .thenByDescending { it.apkSizeBytes }
        )
    }

    /**
     * Opens the native Application Details screen (Samsung Application Manager)
     * where the "Move to SD Card" (انتقال به کارت SD) button is hosted.
     */
    fun openAppDetailsSettings(context: Context, packageName: String) {
        try {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.parse("package:$packageName")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            // Fallback to manage applications
            try {
                val intent = Intent(Settings.ACTION_MANAGE_APPLICATIONS_SETTINGS).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
            } catch (_: Exception) {}
        }
    }

    /**
     * Opens the system storage settings screen
     */
    fun openStorageSettings(context: Context) {
        try {
            val intent = Intent(Settings.ACTION_INTERNAL_STORAGE_SETTINGS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            try {
                val fallback = Intent(Settings.ACTION_SETTINGS).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(fallback)
            } catch (_: Exception) {}
        }
    }

    /**
     * Scans junk files (cache, temp files, large files > 20MB)
     */
    suspend fun scanJunkAndFiles(context: Context): JunkScanResult = withContext(Dispatchers.IO) {
        var cacheBytes = 0L
        var tempBytes = 0L
        var emptyFolders = 0
        val largeFilesList = mutableListOf<StorageFileItem>()
        val filesBySignature = mutableMapOf<String, StorageFileItem>()
        val duplicates = mutableListOf<StorageFileItem>()

        try {
            // 1. App's internal & external cache directories
            context.cacheDir?.let { cDir ->
                cacheBytes += getDirectorySize(cDir)
            }
            context.externalCacheDir?.let { ecDir ->
                cacheBytes += getDirectorySize(ecDir)
            }

            // 2. Scan public storage for temp, logs, backups, and large files
            val storageDirs = mutableListOf<File>()
            Environment.getExternalStorageDirectory()?.let { storageDirs.add(it) }

            val standardPublicDirs = arrayOf(
                Environment.DIRECTORY_DOWNLOADS,
                Environment.DIRECTORY_DCIM,
                Environment.DIRECTORY_PICTURES,
                Environment.DIRECTORY_MOVIES,
                Environment.DIRECTORY_MUSIC,
                Environment.DIRECTORY_DOCUMENTS
            )

            for (dirName in standardPublicDirs) {
                try {
                    val dir = Environment.getExternalStoragePublicDirectory(dirName)
                    if (dir != null && dir.exists()) {
                        storageDirs.add(dir)
                    }
                } catch (_: Exception) {}
            }

            val visitedPaths = mutableSetOf<String>()

            for (baseDir in storageDirs) {
                if (!baseDir.exists() || !baseDir.canRead()) continue
                scanDirectoryRecursive(
                    dir = baseDir,
                    visited = visitedPaths,
                    maxDepth = 4,
                    currentDepth = 0,
                    onFileFound = { file ->
                        val length = file.length()
                        val name = file.name.lowercase(Locale.ROOT)

                        // Check temp / junk files
                        if (name.endsWith(".tmp") || name.endsWith(".log") || name.endsWith(".bak") ||
                            name.startsWith(".thumb") || name.endsWith(".temp") || name.endsWith(".apk")
                        ) {
                            tempBytes += length
                        }

                        // Check large files (> 20 MB = 20 * 1024 * 1024)
                        if (length >= 20 * 1024 * 1024) {
                            val item = StorageFileItem(
                                name = file.name,
                                path = file.absolutePath,
                                sizeBytes = length,
                                fileType = detectFileType(file.name),
                                lastModified = file.lastModified()
                            )
                            largeFilesList.add(item)
                        }

                        // Detect duplicate candidate (same name and size > 1MB)
                        if (length > 1024 * 1024) {
                            val sig = "${file.name}_$length"
                            if (filesBySignature.containsKey(sig)) {
                                val original = filesBySignature[sig]!!
                                if (original.path != file.absolutePath) {
                                    duplicates.add(
                                        StorageFileItem(
                                            name = file.name,
                                            path = file.absolutePath,
                                            sizeBytes = length,
                                            fileType = detectFileType(file.name),
                                            lastModified = file.lastModified()
                                        )
                                    )
                                }
                            } else {
                                filesBySignature[sig] = StorageFileItem(
                                    name = file.name,
                                    path = file.absolutePath,
                                    sizeBytes = length,
                                    fileType = detectFileType(file.name),
                                    lastModified = file.lastModified()
                                )
                            }
                        }
                    },
                    onEmptyFolderFound = {
                        emptyFolders++
                    }
                )
            }
        } catch (_: Exception) {}

        // Sort large files descending
        largeFilesList.sortByDescending { it.sizeBytes }

        JunkScanResult(
            cacheBytes = cacheBytes,
            tempFilesBytes = tempBytes,
            emptyFoldersCount = emptyFolders,
            totalCleanableBytes = cacheBytes + tempBytes,
            largeFiles = largeFilesList.take(30),
            duplicateCandidates = duplicates.take(20),
            isScanned = true
        )
    }

    private fun scanDirectoryRecursive(
        dir: File,
        visited: MutableSet<String>,
        maxDepth: Int,
        currentDepth: Int,
        onFileFound: (File) -> Unit,
        onEmptyFolderFound: () -> Unit
    ) {
        if (currentDepth > maxDepth || !visited.add(dir.absolutePath)) return
        val files = dir.listFiles() ?: return
        if (files.isEmpty() && currentDepth > 0) {
            onEmptyFolderFound()
            return
        }

        for (f in files) {
            if (f.isDirectory) {
                // Skip system and hidden heavy app folders that shouldn't be touched
                if (!f.name.startsWith(".") || f.name.equals(".thumbnails", ignoreCase = true)) {
                    scanDirectoryRecursive(f, visited, maxDepth, currentDepth + 1, onFileFound, onEmptyFolderFound)
                }
            } else if (f.isFile) {
                onFileFound(f)
            }
        }
    }

    /**
     * Cleans temporary files and accessible cache
     */
    suspend fun cleanCacheAndTempFiles(context: Context): Long = withContext(Dispatchers.IO) {
        var bytesCleaned = 0L

        // Clean app cache
        context.cacheDir?.let { dir ->
            bytesCleaned += deleteDirectoryContents(dir)
        }
        context.externalCacheDir?.let { dir ->
            bytesCleaned += deleteDirectoryContents(dir)
        }

        // Clean temp files in public Downloads directory
        try {
            val downloads = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            if (downloads != null && downloads.exists()) {
                val files = downloads.listFiles()
                if (files != null) {
                    for (file in files) {
                        val name = file.name.lowercase(Locale.ROOT)
                        if (name.endsWith(".tmp") || name.endsWith(".temp") || name.endsWith(".log")) {
                            val len = file.length()
                            if (file.delete()) {
                                bytesCleaned += len
                            }
                        }
                    }
                }
            }
        } catch (_: Exception) {}

        bytesCleaned
    }

    private fun deleteDirectoryContents(dir: File): Long {
        var cleaned = 0L
        val files = dir.listFiles() ?: return 0L
        for (f in files) {
            if (f.isDirectory) {
                cleaned += deleteDirectoryContents(f)
                f.delete()
            } else {
                val len = f.length()
                if (f.delete()) {
                    cleaned += len
                }
            }
        }
        return cleaned
    }

    private fun getDirectorySize(dir: File): Long {
        var size = 0L
        val files = dir.listFiles() ?: return 0L
        for (f in files) {
            size += if (f.isDirectory) getDirectorySize(f) else f.length()
        }
        return size
    }

    private fun detectFileType(fileName: String): FileType {
        val lower = fileName.lowercase(Locale.ROOT)
        return when {
            lower.endsWith(".mp4") || lower.endsWith(".mkv") || lower.endsWith(".3gp") || lower.endsWith(".avi") -> FileType.VIDEO
            lower.endsWith(".jpg") || lower.endsWith(".jpeg") || lower.endsWith(".png") || lower.endsWith(".webp") -> FileType.IMAGE
            lower.endsWith(".mp3") || lower.endsWith(".m4a") || lower.endsWith(".wav") || lower.endsWith(".aac") -> FileType.AUDIO
            lower.endsWith(".apk") -> FileType.APK
            lower.endsWith(".pdf") || lower.endsWith(".docx") || lower.endsWith(".txt") -> FileType.DOCUMENT
            lower.endsWith(".zip") || lower.endsWith(".rar") || lower.endsWith(".tar") || lower.endsWith(".7z") -> FileType.ARCHIVE
            lower.endsWith(".tmp") || lower.endsWith(".log") || lower.endsWith(".bak") -> FileType.TEMP
            else -> FileType.OTHER
        }
    }
}
