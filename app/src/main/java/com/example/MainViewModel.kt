package com.example

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.model.AppStorageInfo
import com.example.model.JunkScanResult
import com.example.model.RamInfo
import com.example.model.StorageSpaceInfo
import com.example.util.AppLog
import com.example.util.StorageManagerUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val _internalStorage = MutableStateFlow(StorageSpaceInfo.EMPTY_INTERNAL)
    val internalStorage: StateFlow<StorageSpaceInfo> = _internalStorage.asStateFlow()

    private val _sdCardStorage = MutableStateFlow(StorageSpaceInfo.EMPTY_SD)
    val sdCardStorage: StateFlow<StorageSpaceInfo> = _sdCardStorage.asStateFlow()

    private val _ramInfo = MutableStateFlow(RamInfo.EMPTY)
    val ramInfo: StateFlow<RamInfo> = _ramInfo.asStateFlow()

    private val _installedApps = MutableStateFlow<List<AppStorageInfo>>(emptyList())
    val installedApps: StateFlow<List<AppStorageInfo>> = _installedApps.asStateFlow()

    private val _isLoadingApps = MutableStateFlow(false)
    val isLoadingApps: StateFlow<Boolean> = _isLoadingApps.asStateFlow()

    private val _junkResult = MutableStateFlow(JunkScanResult())
    val junkResult: StateFlow<JunkScanResult> = _junkResult.asStateFlow()

    private val _isScanningJunk = MutableStateFlow(false)
    val isScanningJunk: StateFlow<Boolean> = _isScanningJunk.asStateFlow()

    private val _isCleaningJunk = MutableStateFlow(false)
    val isCleaningJunk: StateFlow<Boolean> = _isCleaningJunk.asStateFlow()

    private val _cleanedBytes = MutableStateFlow<Long?>(null)
    val cleanedBytes: StateFlow<Long?> = _cleanedBytes.asStateFlow()

    init {
        AppLog.i("MainViewModel", "MainViewModel initialized. Launching initial storage refresh and app scan.")
        refreshStorage()
        loadApps()
    }

    fun refreshStorage() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val app = getApplication<Application>()
                val internal = StorageManagerUtil.getInternalStorageInfo()
                val sd = StorageManagerUtil.getSdCardStorageInfo(app)
                val ram = StorageManagerUtil.getRamInfo(app)

                _internalStorage.value = internal
                _sdCardStorage.value = sd
                _ramInfo.value = ram
                AppLog.d("MainViewModel", "Storage refresh complete")
            } catch (t: Throwable) {
                AppLog.e("MainViewModel", "Exception during refreshStorage", t)
            }
        }
    }

    fun loadApps() {
        viewModelScope.launch {
            _isLoadingApps.value = true
            try {
                val apps = StorageManagerUtil.getInstalledApps(getApplication())
                _installedApps.value = apps
                AppLog.i("MainViewModel", "loadApps completed with ${apps.size} apps")
            } catch (t: Throwable) {
                AppLog.e("MainViewModel", "Failed to load apps", t)
            } finally {
                _isLoadingApps.value = false
            }
        }
    }

    fun scanJunk() {
        viewModelScope.launch {
            _isScanningJunk.value = true
            _cleanedBytes.value = null
            try {
                val result = StorageManagerUtil.scanJunkAndFiles(getApplication())
                _junkResult.value = result
                AppLog.i("MainViewModel", "scanJunk completed: cleanable=${result.totalCleanableBytes} bytes")
            } catch (t: Throwable) {
                AppLog.e("MainViewModel", "Failed to scan junk", t)
            } finally {
                _isScanningJunk.value = false
            }
        }
    }

    fun cleanJunk() {
        viewModelScope.launch {
            _isCleaningJunk.value = true
            try {
                val cleaned = StorageManagerUtil.cleanCacheAndTempFiles(getApplication())
                _cleanedBytes.value = cleaned
                AppLog.i("MainViewModel", "cleanJunk completed: cleaned $cleaned bytes")
                refreshStorage()
                _junkResult.value = _junkResult.value.copy(
                    tempFilesBytes = 0L,
                    cacheBytes = 0L,
                    totalCleanableBytes = 0L
                )
            } catch (t: Throwable) {
                AppLog.e("MainViewModel", "Failed to clean junk", t)
            } finally {
                _isCleaningJunk.value = false
            }
        }
    }

    fun openAppDetails(context: Context, packageName: String) {
        StorageManagerUtil.openAppDetailsSettings(context, packageName)
    }

    fun openSystemStorage(context: Context) {
        StorageManagerUtil.openStorageSettings(context)
    }
}
