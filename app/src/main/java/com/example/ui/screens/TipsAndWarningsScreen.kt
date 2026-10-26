package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.SdCardAlert
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.util.NotificationHelper

@Composable
fun TipsAndWarningsScreen(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var isReminderEnabled by remember {
        mutableStateOf(NotificationHelper.isDailyReminderEnabled(context))
    }
    var testNotifMessage by remember { mutableStateOf<String?>(null) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Daily Reminder Settings Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("daily_reminder_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.NotificationsActive,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "یادآوری روزانه حافظه گوشی",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "هشدار خودکار قبل از هنگ کردن Galaxy J1",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Switch(
                            checked = isReminderEnabled,
                            onCheckedChange = { checked ->
                                isReminderEnabled = checked
                                NotificationHelper.setDailyReminderEnabled(context, checked)
                            },
                            modifier = Modifier.testTag("switch_daily_reminder")
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = {
                            NotificationHelper.showStorageAlertNotification(
                                context,
                                "وضعیت حافظه Galaxy J1",
                                "تست یادآوری: فضای ذخیره‌سازی خود را بررسی کنید تا از کند شدن دستگاه جلوگیری شود."
                            )
                            testNotifMessage = "پیام هشدار تستی به بخش اعلانات ارسال شد!"
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .testTag("btn_test_notification"),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondary
                        )
                    ) {
                        Text(
                            text = "ارسال هشدار آزمایشی به نوار اعلان",
                            style = MaterialTheme.typography.labelLarge
                        )
                    }

                    testNotifMessage?.let { msg ->
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = msg,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                }
            }
        }

        // Warning 1: Why some apps cannot be moved
        item {
            WarningCard(
                icon = Icons.Default.HelpOutline,
                iconBg = MaterialTheme.colorScheme.primaryContainer,
                iconTint = MaterialTheme.colorScheme.primary,
                title = "۱. چرا همه برنامه‌ها به کارت SD منتقل نمی‌شوند؟",
                description = "در سیستم‌عامل اندروید، برنامه‌هایی که دارای ویژگی‌های زیر باشند قابلیت انتقال به کارت حافظه را ندارند:\n\n" +
                        "• برنامه‌های دارای ابزارک (Widget) روی صفحه اصلی\n" +
                        "• برنامه‌های بانکی و امنیتی که به رمزنگاری سخت‌افزاری نیاز دارند\n" +
                        "• برنامه‌هایی که هنگام روشن شدن گوشی باید بالا بیایند (مثل ساعت زنگ‌دار یا لانچرها)\n" +
                        "• برنامه‌هایی که سازنده آنها در کد برنامه گزینه android:installLocation را قفل کرده است (مانند واتساپ)\n\n" +
                        "برای این برنامه‌ها، بهترین کار پاک کردن دوره‌ای «داده‌های حافظه پنهان» (Clear Cache) است."
            )
        }

        // Warning 2: SD vs RAM
        item {
            WarningCard(
                icon = Icons.Default.Memory,
                iconBg = MaterialTheme.colorScheme.errorContainer,
                iconTint = MaterialTheme.colorScheme.error,
                title = "۲. آیا می‌توان از کارت SD به عنوان رم (RAM) استفاده کرد؟",
                description = "خیر، این یک باور نادرست است!\n\n" +
                        "رم (RAM) حافظه پردازشی فوق‌سریع گوشی است که سرعت آن به چندین گیگابایت در ثانیه می‌رسد. در حالی که حتی سریع‌ترین کارت‌های حافظه سرعتی در حدود ۲۰ تا ۴۰ مگابایت بر ثانیه دارند.\n\n" +
                        "استفاده از حافظه SD به عنوان رم مجازی (Swap) نه تنها باعث لگ شدید و فریز دستگاه می‌شود، بلکه به دلیل تعداد بالای خواندن و نوشتن، کارت حافظه شما را ظرف چند هفته می‌سوزاند."
            )
        }

        // Warning 3: SD Card Speed Class
        item {
            WarningCard(
                icon = Icons.Default.Speed,
                iconBg = MaterialTheme.colorScheme.tertiaryContainer,
                iconTint = MaterialTheme.colorScheme.tertiary,
                title = "۳. ضرورت استفاده از کارت حافظه Class 10 یا بالاتر",
                description = "اگر برنامه‌ها را به کارت حافظه منتقل کنید، سرعت اجرای آن برنامه مستقیماً به کیفیت و سرعت کارت SD بستگی دارد.\n\n" +
                        "• کارت‌های حافظه ارزان قیمت (Class 4 یا Class 6) باعث می‌شوند باز شدن برنامه‌ها و دوربین تا ۱۰ ثانیه طول بکشد.\n" +
                        "• حتماً از کارت حافظه با علامت Class 10 (عدد ۱۰ در دایره) یا علامت UHS-I (عدد ۱ درون U) از برندهای معتبر مانند SanDisk یا Samsung استفاده کنید."
            )
        }

        // Warning 4: Unmounting SD
        item {
            WarningCard(
                icon = Icons.Default.SdCardAlert,
                iconBg = MaterialTheme.colorScheme.surfaceVariant,
                iconTint = MaterialTheme.colorScheme.onSurfaceVariant,
                title = "۴. هشدار خارج کردن کارت حافظه",
                description = "هرگز هنگامی که گوشی روشن است کارت حافظه را بدون خارج کردن نرم‌افزاری بیرون نکشید!\n\n" +
                        "همیشه قبل از درآوردن فیزیکی کارت حافظه، ابتدا به منوی تنظیمات (Settings) > حافظه (Storage) رفته و گزینه «جدا کردن کارت حافظه» (Unmount SD card) را انتخاب کنید. در غیر این صورت فایل‌های برنامه‌های منتقل‌شده آسیب خواهند دید."
            )
        }

        item {
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
private fun WarningCard(
    icon: ImageVector,
    iconBg: Color,
    iconTint: Color,
    title: String,
    description: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(iconBg),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                lineHeight = 22.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
