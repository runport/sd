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
            val used = (total - free).coerceAtLeast(0L)
            val percent = if (total > 0) ((used * 100) / total).toInt().coerceIn(0, 100) else 0

            AppLog.i("StorageManagerUtil", "Internal storage query OK: total=$total, free=$free, path=${path.path}")
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
        } catch (t: Throwable) {
            AppLog.e("StorageManagerUtil", "Failed to query internal storage", t)
            StorageSpaceInfo.EMPTY_INTERNAL
        }
    }

    /**
     * Get SD Card (microSD / secondary external storage) info with defensive handling for Android 5.1.1
     */
    fun getSdCardStorageInfo(context: Context): StorageSpaceInfo {
        try {
            // Check ContextCompat.getExternalFilesDirs
            val externalDirs = try {
                ContextCompat.getExternalFilesDirs(context, null)
            } catch (t: Throwable) {
                AppLog.w("StorageManagerUtil", "getExternalFilesDirs failed", t)
                emptyArray<File?>()
            }

            for (i in externalDirs.indices) {
                val dir = externalDirs[i] ?: continue
                try {
                    val isRemovable = try {
                        Environment.isExternalStorageRemovable(dir)
                    } catch (t: Throwable) {
                        i > 0
                    }

                    if (isRemovable || i > 0) {
                        if (dir.exists() && dir.canRead()) {
                            val stat = StatFs(dir.path)
                            val blockSize = stat.blockSizeLong
                            val totalBlocks = stat.blockCountLong
                            val availableBlocks = stat.availableBlocksLong

                            val total = totalBlocks * blockSize
                            val free = availableBlocks * blockSize
                            val used = (total - free).coerceAtLeast(0L)
                            val percent = if (total > 0) ((used * 100) / total).toInt().coerceIn(0, 100) else 0

                            if (total > 0) {
                                AppLog.i("StorageManagerUtil", "SD card detected via getExternalFilesDirs[$i]: ${dir.absolutePath}")
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
                } catch (t: Throwable) {
                    AppLog.w("StorageManagerUtil", "Error inspecting external dir at index $i", t)
                }
            }

            // Fallback check: check secondary mount points common in Android 5 (Lollipop TouchWiz on J1)
            val secondaryPaths = arrayOf(
                "/storage/extSdCard",
                "/storage/sdcard1",
                "/mnt/extSdCard",
                "/mnt/sdcard1",
                "/storage/MicroSD"
            )
            for (p in secondaryPaths) {
                try {
                    val file = File(p)
                    if (file.exists() && file.canRead()) {
                        val stat = StatFs(file.path)
                        val blockSize = stat.blockSizeLong
                        val total = stat.blockCountLong * blockSize
                        val free = stat.availableBlocksLong * blockSize
                        val used = (total - free).coerceAtLeast(0L)
                        if (total > 0) {
                            val percent = ((used * 100) / total).toInt().coerceIn(0, 100)
                            AppLog.i("StorageManagerUtil", "SD card detected at legacy path $p")
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
                } catch (t: Throwable) {
                    AppLog.w("StorageManagerUtil", "Error probing mount path $p", t)
                }
            }
        } catch (t: Throwable) {
            AppLog.e("StorageManagerUtil", "Unexpected error in getSdCardStorageInfo", t)
        }

        AppLog.i("StorageManagerUtil", "No secondary SD card detected or mounted")
        return StorageSpaceInfo.EMPTY_SD
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
            val used = (total - free).coerceAtLeast(0L)
            val percent = if (total > 0) ((used * 100) / total).toInt().coerceIn(0, 100) else 0

            AppLog.i("StorageManagerUtil", "RAM query OK: total=$total, free=$free, percent=$percent")
            RamInfo(
                totalBytes = total,
                freeBytes = free,
                usedBytes = used,
                percentageUsed = percent
            )
        } catch (t: Throwable) {
            AppLog.e("StorageManagerUtil", "Failed to query RAM info", t)
            RamInfo.EMPTY
        }
    }

    /**
     * Scan installed packages and classify movable status for Android 5.0+.
     * Note: Avoids pre-loading all Bitmaps/Drawables to prevent OOM on 512MB RAM devices.
     */
    suspend fun getInstalledApps(context: Context): List<AppStorageInfo> = withContext(Dispatchers.IO) {
        AppLog.i("StorageManagerUtil", "Starting getInstalledApps scan")
        val pm = context.packageManager
        val packages: List<PackageInfo> = try {
            pm.getInstalledPackages(PackageManager.GET_META_DATA)
        } catch (t: Throwable) {
            AppLog.e("StorageManagerUtil", "Failed to query installed packages", t)
            emptyList()
        }

        val result = mutableListOf<AppStorageInfo>()

        for (pkg in packages) {
            try {
                val appInfo = pkg.applicationInfo ?: continue
                val isSystem = (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0
                val isOnSd = (appInfo.flags and ApplicationInfo.FLAG_EXTERNAL_STORAGE) != 0

                val location = pkg.installLocation

                val canMove = if (isSystem) {
                    false
                } else if (isOnSd) {
                    true
                } else {
                    location == PackageInfo.INSTALL_LOCATION_PREFER_EXTERNAL ||
                            location == PackageInfo.INSTALL_LOCATION_AUTO ||
                            location == 0
                }

                val appName = try {
                    appInfo.loadLabel(pm).toString()
                } catch (t: Throwable) {
                    pkg.packageName
                }

                val apkSize = try {
                    File(appInfo.sourceDir).length()
                } catch (t: Throwable) {
                    0L
                }

                result.add(
                    AppStorageInfo(
                        packageName = pkg.packageName,
                        appName = appName,
                        isSystemApp = isSystem,
                        isInstalledOnSd = isOnSd,
                        canBeMovedToSd = canMove,
                        installLocation = location,
                        apkSizeBytes = apkSize,
                        versionName = pkg.versionName ?: "1.0"
                    )
                )
            } catch (t: Throwable) {
                AppLog.w("StorageManagerUtil", "Error parsing package info for ${pkg.packageName}", t)
            }
        }

        AppLog.i("StorageManagerUtil", "Finished scan: ${result.size} packages enumerated successfully")
        result.sortedWith(
            compareByDescending<AppStorageInfo> { it.canBeMovedToSd && !it.isInstalledOnSd }
                .thenByDescending { !it.isSystemApp }
                .thenByDescending { it.apkSizeBytes }
        )
    }

    fun openAppDetailsSettings(context: Context, packageName: String) {
        AppLog.i("StorageManagerUtil", "Opening app details for $packageName")
        try {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.parse("package:$packageName")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (t: Throwable) {
            AppLog.w("StorageManagerUtil", "ACTION_APPLICATION_DETAILS_SETTINGS failed, trying fallback", t)
            try {
                val intent = Intent(Settings.ACTION_MANAGE_APPLICATIONS_SETTINGS).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
            } catch (t2: Throwable) {
                AppLog.e("StorageManagerUtil", "Failed to open application settings", t2)
            }
        }
    }

    fun openStorageSettings(context: Context) {
        AppLog.i("StorageManagerUtil", "Opening system storage settings")
        try {
            val intent = Intent(Settings.ACTION_INTERNAL_STORAGE_SETTINGS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (t: Throwable) {
            AppLog.w("StorageManagerUtil", "ACTION_INTERNAL_STORAGE_SETTINGS failed, opening settings root", t)
            try {
                val fallback = Intent(Settings.ACTION_SETTINGS).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(fallback)
            } catch (t2: Throwable) {
                AppLog.e("StorageManagerUtil", "Failed to open settings", t2)
            }
        }
    }

    suspend fun scanJunkAndFiles(context: Context): JunkScanResult = withContext(Dispatchers.IO) {
        AppLog.i("StorageManagerUtil", "Starting scanJunkAndFiles")
        var cacheBytes = 0L
        var tempBytes = 0L
        var emptyFolders = 0
        val largeFilesList = mutableListOf<StorageFileItem>()
        val filesBySignature = mutableMapOf<String, StorageFileItem>()
        val duplicates = mutableListOf<StorageFileItem>()

        try {
            context.cacheDir?.let { cDir ->
                cacheBytes += getDirectorySize(cDir)
            }
            context.externalCacheDir?.let { ecDir ->
                cacheBytes += getDirectorySize(ecDir)
            }

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
                } catch (t: Throwable) {
                    AppLog.w("StorageManagerUtil", "Could not access public dir: $dirName", t)
                }
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

                        if (name.endsWith(".tmp") || name.endsWith(".log") || name.endsWith(".bak") ||
                            name.startsWith(".thumb") || name.endsWith(".temp") || name.endsWith(".apk")
                        ) {
                            tempBytes += length
                        }

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
        } catch (t: Throwable) {
            AppLog.e("StorageManagerUtil", "Error scanning junk and files", t)
        }

        largeFilesList.sortByDescending { it.sizeBytes }

        AppLog.i("StorageManagerUtil", "scanJunkAndFiles finished: cache=$cacheBytes, temp=$tempBytes, large=${largeFilesList.size}")
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
                if (!f.name.startsWith(".") || f.name.equals(".thumbnails", ignoreCase = true)) {
                    scanDirectoryRecursive(f, visited, maxDepth, currentDepth + 1, onFileFound, onEmptyFolderFound)
                }
            } else if (f.isFile) {
                onFileFound(f)
            }
        }
    }

    suspend fun cleanCacheAndTempFiles(context: Context): Long = withContext(Dispatchers.IO) {
        AppLog.i("StorageManagerUtil", "cleanCacheAndTempFiles invoked")
        var bytesCleaned = 0L

        context.cacheDir?.let { dir ->
            bytesCleaned += deleteDirectoryContents(dir)
        }
        context.externalCacheDir?.let { dir ->
            bytesCleaned += deleteDirectoryContents(dir)
        }

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
        } catch (t: Throwable) {
            AppLog.w("StorageManagerUtil", "Error cleaning temporary downloads", t)
        }

        AppLog.i("StorageManagerUtil", "cleanCacheAndTempFiles completed: freed $bytesCleaned bytes")
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
