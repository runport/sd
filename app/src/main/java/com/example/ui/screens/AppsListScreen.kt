package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AppStorageInfo
import com.example.ui.components.AppItemCard
import com.example.util.StorageManagerUtil

enum class AppFilter {
    ALL, MOVABLE, ON_SD, USER_ONLY
}

@Composable
fun AppsListScreen(
    apps: List<AppStorageInfo>,
    isLoading: Boolean,
    onMoveApp: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf(AppFilter.MOVABLE) }
    var selectedAppForDialog by remember { mutableStateOf<AppStorageInfo?>(null) }

    // Filter logic
    val filteredApps = remember(apps, searchQuery, selectedFilter) {
        apps.filter { app ->
            val matchesSearch = searchQuery.isBlank() ||
                    app.appName.contains(searchQuery, ignoreCase = true) ||
                    app.packageName.contains(searchQuery, ignoreCase = true)

            val matchesFilter = when (selectedFilter) {
                AppFilter.ALL -> true
                AppFilter.MOVABLE -> app.canBeMovedToSd && !app.isInstalledOnSd
                AppFilter.ON_SD -> app.isInstalledOnSd
                AppFilter.USER_ONLY -> !app.isSystemApp
            }

            matchesSearch && matchesFilter
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(12.dp))

        // Search Bar with large touch target
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("app_search_field"),
            shape = RoundedCornerShape(12.dp),
            placeholder = { Text("جستجوی نام یا پکیج برنامه…") },
            leadingIcon = {
                Icon(Icons.Default.Search, contentDescription = "جستجو")
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(Icons.Default.Clear, contentDescription = "پاک کردن متن")
                    }
                }
            },
            singleLine = true
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Filter chips row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = selectedFilter == AppFilter.MOVABLE,
                onClick = { selectedFilter = AppFilter.MOVABLE },
                label = { Text("قابل انتقال به SD") },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                modifier = Modifier.testTag("filter_movable")
            )

            FilterChip(
                selected = selectedFilter == AppFilter.ON_SD,
                onClick = { selectedFilter = AppFilter.ON_SD },
                label = { Text("روی کارت SD") },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                    selectedLabelColor = MaterialTheme.colorScheme.onSecondaryContainer
                ),
                modifier = Modifier.testTag("filter_on_sd")
            )

            FilterChip(
                selected = selectedFilter == AppFilter.USER_ONLY,
                onClick = { selectedFilter = AppFilter.USER_ONLY },
                label = { Text("برنامه‌های من (کاربر)") },
                modifier = Modifier.testTag("filter_user_only")
            )

            FilterChip(
                selected = selectedFilter == AppFilter.ALL,
                onClick = { selectedFilter = AppFilter.ALL },
                label = { Text("همه (${StorageManagerUtil.toPersianDigits(apps.size.toString())})") },
                modifier = Modifier.testTag("filter_all")
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Header info count
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "تعداد یافته‌ها: ${StorageManagerUtil.toPersianDigits(filteredApps.size.toString())} برنامه",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "در حال تحلیل پکیج‌ها و محل نصب برنامه‌ها…",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        } else if (filteredApps.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        modifier = Modifier.size(56.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "برنامه‌ای با این فیلتر یافت نشد.",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "فیلتر «همه» را انتخاب کرده یا عبارت جستجو را تغییر دهید.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("apps_list"),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredApps, key = { it.packageName }) { app ->
                    AppItemCard(
                        app = app,
                        onMoveClick = { selectedAppForDialog = it }
                    )
                }
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }

    // Step-by-step Dialog when user taps Move to SD
    selectedAppForDialog?.let { targetApp ->
        AlertDialog(
            onDismissRequest = { selectedAppForDialog = null },
            icon = {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(32.dp)
                )
            },
            title = {
                Text(
                    text = "انتقال «${targetApp.appName}» به SD",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "طبق استانداردهای اندروید ۵ و سامسونگ Galaxy J1، مراحل انتقال به صورت زیر است:",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "۱. در صفحه‌ای که باز می‌شود، دکمه «انتقال به کارت SD» یا «Move to SD Card» را لمس کنید.",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "۲. چند ثانیه تا اتمام انتقال صبر کنید (در پایان، دکمه به «انتقال به حافظه دستگاه» تبدیل می‌شود).",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "توجه: در صورتی که دکمه خاکستری و غیرقابل لمس باشد، توسعه‌دهنده برنامه این قابلیت را قفل کرده است.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val pkg = targetApp.packageName
                        selectedAppForDialog = null
                        onMoveApp(pkg)
                    },
                    modifier = Modifier.testTag("dialog_confirm_open_settings")
                ) {
                    Text("باز کردن تنظیمات برنامه")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { selectedAppForDialog = null }) {
                    Text("انصراف")
                }
            }
        )
    }
}
