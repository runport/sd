package com.example

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SdCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.screens.AppsListScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.QuickCleanerScreen
import com.example.ui.screens.SamsungJ1GuideScreen
import com.example.ui.screens.TipsAndWarningsScreen

enum class MainTab(val title: String) {
    DASHBOARD("داشبورد"),
    APPS("برنامه‌ها"),
    CLEANER("پاکسازی"),
    GUIDE("راهنمای J1"),
    TIPS("هشدارها")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var currentTab by remember { mutableStateOf(MainTab.DASHBOARD) }

    val internalStorage by viewModel.internalStorage.collectAsStateWithLifecycle()
    val sdCardStorage by viewModel.sdCardStorage.collectAsStateWithLifecycle()
    val ramInfo by viewModel.ramInfo.collectAsStateWithLifecycle()
    val installedApps by viewModel.installedApps.collectAsStateWithLifecycle()
    val isLoadingApps by viewModel.isLoadingApps.collectAsStateWithLifecycle()
    val junkResult by viewModel.junkResult.collectAsStateWithLifecycle()
    val isScanningJunk by viewModel.isScanningJunk.collectAsStateWithLifecycle()
    val isCleaningJunk by viewModel.isCleaningJunk.collectAsStateWithLifecycle()
    val cleanedBytes by viewModel.cleanedBytes.collectAsStateWithLifecycle()

    // Handle back button to return to dashboard first
    BackHandler(enabled = currentTab != MainTab.DASHBOARD) {
        currentTab = MainTab.DASHBOARD
    }

    // Force RTL layout direction for Persian UI
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = "مدیر حافظه و کارت SD",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    },
                    actions = {
                        IconButton(
                            onClick = {
                                viewModel.refreshStorage()
                                if (currentTab == MainTab.APPS) {
                                    viewModel.loadApps()
                                }
                            },
                            modifier = Modifier.testTag("topbar_refresh_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "بروزرسانی",
                                tint = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                )
            },
            bottomBar = {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 8.dp,
                    modifier = Modifier.testTag("main_bottom_nav")
                ) {
                    NavigationBarItem(
                        selected = currentTab == MainTab.DASHBOARD,
                        onClick = { currentTab = MainTab.DASHBOARD },
                        icon = {
                            Icon(
                                Icons.Default.Dashboard,
                                contentDescription = "داشبورد",
                                modifier = Modifier.size(24.dp)
                            )
                        },
                        label = { Text("داشبورد", fontSize = 12.sp, fontWeight = FontWeight.SemiBold) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary
                        ),
                        modifier = Modifier.testTag("nav_item_dashboard")
                    )

                    NavigationBarItem(
                        selected = currentTab == MainTab.APPS,
                        onClick = {
                            currentTab = MainTab.APPS
                            if (installedApps.isEmpty()) {
                                viewModel.loadApps()
                            }
                        },
                        icon = {
                            Icon(
                                Icons.Default.SdCard,
                                contentDescription = "برنامه‌ها",
                                modifier = Modifier.size(24.dp)
                            )
                        },
                        label = { Text("برنامه‌ها", fontSize = 12.sp, fontWeight = FontWeight.SemiBold) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary
                        ),
                        modifier = Modifier.testTag("nav_item_apps")
                    )

                    NavigationBarItem(
                        selected = currentTab == MainTab.CLEANER,
                        onClick = {
                            currentTab = MainTab.CLEANER
                            if (!junkResult.isScanned && !isScanningJunk) {
                                viewModel.scanJunk()
                            }
                        },
                        icon = {
                            Icon(
                                Icons.Default.CleaningServices,
                                contentDescription = "پاکسازی",
                                modifier = Modifier.size(24.dp)
                            )
                        },
                        label = { Text("پاکسازی", fontSize = 12.sp, fontWeight = FontWeight.SemiBold) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.secondary,
                            selectedTextColor = MaterialTheme.colorScheme.secondary
                        ),
                        modifier = Modifier.testTag("nav_item_cleaner")
                    )

                    NavigationBarItem(
                        selected = currentTab == MainTab.GUIDE,
                        onClick = { currentTab = MainTab.GUIDE },
                        icon = {
                            Icon(
                                Icons.AutoMirrored.Filled.MenuBook,
                                contentDescription = "راهنما",
                                modifier = Modifier.size(24.dp)
                            )
                        },
                        label = { Text("راهنما", fontSize = 12.sp, fontWeight = FontWeight.SemiBold) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary
                        ),
                        modifier = Modifier.testTag("nav_item_guide")
                    )

                    NavigationBarItem(
                        selected = currentTab == MainTab.TIPS,
                        onClick = { currentTab = MainTab.TIPS },
                        icon = {
                            Icon(
                                Icons.AutoMirrored.Filled.HelpOutline,
                                contentDescription = "هشدارها",
                                modifier = Modifier.size(24.dp)
                            )
                        },
                        label = { Text("هشدارها", fontSize = 12.sp, fontWeight = FontWeight.SemiBold) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.tertiary,
                            selectedTextColor = MaterialTheme.colorScheme.tertiary
                        ),
                        modifier = Modifier.testTag("nav_item_tips")
                    )
                }
            },
            modifier = modifier.fillMaxSize()
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                when (currentTab) {
                    MainTab.DASHBOARD -> DashboardScreen(
                        internalStorage = internalStorage,
                        sdCardStorage = sdCardStorage,
                        ramInfo = ramInfo,
                        onRefresh = { viewModel.refreshStorage() },
                        onOpenStorageSettings = { viewModel.openSystemStorage(context) },
                        onNavigateToApps = {
                            currentTab = MainTab.APPS
                            if (installedApps.isEmpty()) viewModel.loadApps()
                        },
                        onNavigateToCleaner = {
                            currentTab = MainTab.CLEANER
                            if (!junkResult.isScanned && !isScanningJunk) viewModel.scanJunk()
                        }
                    )

                    MainTab.APPS -> AppsListScreen(
                        apps = installedApps,
                        isLoading = isLoadingApps,
                        onMoveApp = { pkg -> viewModel.openAppDetails(context, pkg) }
                    )

                    MainTab.CLEANER -> QuickCleanerScreen(
                        junkResult = junkResult,
                        isScanning = isScanningJunk,
                        isCleaning = isCleaningJunk,
                        cleanedBytes = cleanedBytes,
                        onScan = { viewModel.scanJunk() },
                        onCleanNow = { viewModel.cleanJunk() },
                        onOpenSystemStorageSettings = { viewModel.openSystemStorage(context) }
                    )

                    MainTab.GUIDE -> SamsungJ1GuideScreen(
                        onOpenAppSettings = { viewModel.openSystemStorage(context) }
                    )

                    MainTab.TIPS -> TipsAndWarningsScreen()
                }
            }
        }
    }
}
