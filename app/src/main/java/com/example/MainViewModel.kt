package com.example

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.model.AppStorageInfo
import com.example.model.JunkScanResult
import com.example.model.RamInfo
import com.example.model.StorageSpaceInfo
import com.example.util.StorageManagerUtil
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val _internalStorage = MutableStateFlow(StorageManagerUtil.getInternalStorageInfo())
    val internalStorage: StateFlow<StorageSpaceInfo> = _internalStorage.asStateFlow()

    private val _sdCardStorage = MutableStateFlow(StorageManagerUtil.getSdCardStorageInfo(application))
    val sdCardStorage: StateFlow<StorageSpaceInfo> = _sdCardStorage.asStateFlow()

    private val _ramInfo = MutableStateFlow(StorageManagerUtil.getRamInfo(application))
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
        refreshStorage()
        loadApps()
    }

    fun refreshStorage() {
        val app = getApplication<Application>()
        _internalStorage.value = StorageManagerUtil.getInternalStorageInfo()
        _sdCardStorage.value = StorageManagerUtil.getSdCardStorageInfo(app)
        _ramInfo.value = StorageManagerUtil.getRamInfo(app)
    }

    fun loadApps() {
        viewModelScope.launch {
            _isLoadingApps.value = true
            val apps = StorageManagerUtil.getInstalledApps(getApplication())
            _installedApps.value = apps
            _isLoadingApps.value = false
        }
    }

    fun scanJunk() {
        viewModelScope.launch {
            _isScanningJunk.value = true
            _cleanedBytes.value = null
            val result = StorageManagerUtil.scanJunkAndFiles(getApplication())
            _junkResult.value = result
            _isScanningJunk.value = false
        }
    }

    fun cleanJunk() {
        viewModelScope.launch {
            _isCleaningJunk.value = true
            val cleaned = StorageManagerUtil.cleanCacheAndTempFiles(getApplication())
            _cleanedBytes.value = cleaned
            _isCleaningJunk.value = false
            // Refresh storage and scan result
            refreshStorage()
            _junkResult.value = _junkResult.value.copy(
                tempFilesBytes = 0L,
                cacheBytes = 0L,
                totalCleanableBytes = 0L
            )
        }
    }

    fun openAppDetails(context: Context, packageName: String) {
        StorageManagerUtil.openAppDetailsSettings(context, packageName)
    }

    fun openSystemStorage(context: Context) {
        StorageManagerUtil.openStorageSettings(context)
    }
}
