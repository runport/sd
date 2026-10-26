package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.FolderShared
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.SdCard
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.util.StorageManagerUtil

data class GuideStep(
    val stepNumber: Int,
    val title: String,
    val description: String
)

data class GuideTopic(
    val id: String,
    val icon: ImageVector,
    val title: String,
    val subtitle: String,
    val steps: List<GuideStep>,
    val proTip: String? = null,
    val hasAction: Boolean = false
)

@Composable
fun SamsungJ1GuideScreen(
    onOpenAppSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val topics = remember {
        listOf(
            GuideTopic(
                id = "topic_apps",
                icon = Icons.Default.SdCard,
                title = "۱. انتقال برنامه‌ها به کارت SD در TouchWiz",
                subtitle = "مراحل گام‌به‌گام در منوی تنظیمات سامسونگ Galaxy J1",
                steps = listOf(
                    GuideStep(1, "ورود به تنظیمات", "وارد برنامه «تنظیمات» (Settings) گوشی سامسونگ خود شوید."),
                    GuideStep(2, "مدیریت برنامه‌ها", "گزینه «مدیریت برنامه‌ها» (Application Manager) را لمس کنید."),
                    GuideStep(3, "برگه برنامه‌ها", "صفحه را به سمت چپ بکشید تا برگه «بارگیری شده» (Downloaded) یا «کارت SD» دیده شود."),
                    GuideStep(4, "انتخاب برنامه", "روی برنامه مورد نظری که می‌خواهید منتقل کنید ضربه بزنید."),
                    GuideStep(5, "دکمه انتقال", "دکمه «انتقال به کارت SD» یا «Move to SD Card» را فشار دهید."),
                    GuideStep(6, "اتمام فرایند", "چند ثانیه صبر کنید تا انتقال تکمیل شود. پس از انتقال موفق، نام دکمه به «انتقال به حافظه دستگاه» تغییر می‌یابد.")
                ),
                proTip = "نکته مهم: اگر دکمه انتقال خاکستری است، یعنی سازنده برنامه (مثل واتساپ یا اینستاگرام) اجازه انتقال مستقیم کد اصلی را نداده است.",
                hasAction = true
            ),
            GuideTopic(
                id = "topic_camera",
                icon = Icons.Default.CameraAlt,
                title = "۲. تنظیم دوربین برای ذخیره مستقیم روی کارت SD",
                subtitle = "جلوگیری از پر شدن حافظه داخلی توسط عکس‌ها و فیلم‌ها",
                steps = listOf(
                    GuideStep(1, "باز کردن دوربین", "برنامه دوربین پیش‌فرض سامسونگ (Camera) را باز کنید."),
                    GuideStep(2, "آیکون تنظیمات", "روی آیکون چرخ‌دنده تنظیمات در گوشه صفحه ضربه بزنید."),
                    GuideStep(3, "محل ذخیره‌سازی", "در منوی تنظیمات دوربین، گزینه «محل ذخیره» (Storage location) را پیدا کنید."),
                    GuideStep(4, "انتخاب کارت حافظه", "گزینه «کارت حافظه» (Memory card) را انتخاب کنید.")
                ),
                proTip = "تأثیر فوق‌العاده: با این کار، ویدیوها و عکس‌های جدید بدون اشغال حتی یک بایت از حافظه داخلی، مستقیماً روی کارت SD ذخیره می‌شوند."
            ),
            GuideTopic(
                id = "topic_files",
                icon = Icons.Default.FolderShared,
                title = "۳. انتقال عکس‌ها و آهنگ‌های موجود به SD",
                subtitle = "با استفاده از برنامه رسمی «فایل‌های من» سامسونگ (My Files)",
                steps = listOf(
                    GuideStep(1, "برنامه فایل‌های من", "برنامه زرد‌رنگ «فایل‌های من» (My Files) را از منوی گوشی باز کنید."),
                    GuideStep(2, "حافظه دستگاه", "گزینه «حافظه دستگاه» (Device storage) را انتخاب کنید."),
                    GuideStep(3, "پوشه‌های حجیم", "پوشه DCIM (عکس‌های دوربین)، Music (آهنگ‌ها) یا Download را باز کنید."),
                    GuideStep(4, "انتخاب فایل‌ها", "روی یکی از فایل‌ها انگشت خود را نگه دارید تا حالت انتخاب فعال شود، سپس گزینه «انتخاب همه» را بزنید."),
                    GuideStep(5, "گزینه انتقال", "دکمه منو (سه نقطه) را لمس کرده و «انتقال» (Move) را انتخاب نمایید."),
                    GuideStep(6, "کارت SD", "وارد «کارت حافظه SD» شده و گزینه «انتقال به اینجا» (Move here) را لمس کنید.")
                ),
                proTip = "توصیه: فایل‌های ضبط صدای شخصی و آهنگ‌ها بیشترین حجم را می‌گیرند. انتقال یکجای آنها بیش از ۱ گیگابایت فضا آزاد می‌کند."
            ),
            GuideTopic(
                id = "topic_disable_bloat",
                icon = Icons.Default.Block,
                title = "۴. غیرفعال‌سازی برنامه‌های پیش‌فرض سنگین",
                subtitle = "آزادسازی رم (RAM) و حافظه داخلی در Galaxy J1",
                steps = listOf(
                    GuideStep(1, "شناسایی برنامه‌ها", "گوشی Galaxy J1 برنامه‌های پیش‌فرضی دارد که شاید هرگز استفاده نکنید (مانند Play Movies, Play Music, Hangouts, ChatON, Google+)."),
                    GuideStep(2, "تنظیمات برنامه", "به Settings > Application Manager رفته و صفحه را به برگه «همه» (ALL) ببرید."),
                    GuideStep(3, "پاک کردن داده", "روی برنامه مورد نظر زده و ابتدا دکمه «پاک کردن داده‌ها» (Clear data) را بزنید."),
                    GuideStep(4, "حذف بروزرسانی‌ها", "دکمه «حذف نصب به‌روزرسانی‌ها» (Uninstall updates) را بزنید تا حجم برنامه به حالت اولیه کارخانه بازگردد."),
                    GuideStep(5, "غیرفعال کردن", "دکمه «غیرفعال کردن» (Disable / Turn off) را لمس کنید.")
                ),
                proTip = "نتیجه: این کار علاوه بر آزادسازی صدها مگابایت حافظه داخلی، رم ۵۱۲ مگابایتی J1 را سبک کرده و لگ گوشی را به طور چشمگیر کاهش می‌دهد.",
                hasAction = true
            ),
            GuideTopic(
                id = "topic_daily_tips",
                icon = Icons.Default.Speed,
                title = "۵. تکنیک‌های روزمره ضد هنگ مخصوص Galaxy J1",
                subtitle = "روش‌های طلایی نگهداری از گوشی‌های سبک با اندروید ۵",
                steps = listOf(
                    GuideStep(1, "تنظیم کش پیام‌رسان‌ها", "در تلگرام یا ایتا، به تنظیمات داده‌ها و ذخیره‌سازی رفته و پاکسازی خودکار رسانه‌ها را روی ۳ روز قرار دهید."),
                    GuideStep(2, "استفاده از نسخه‌های Lite", "به جای برنامه‌های حجیم اینستاگرام یا فیس‌بوک، از مرورگر یا نسخه Lite آنها استفاده کنید."),
                    GuideStep(3, "خالی نگه داشتن ۳۰۰ مگابایت", "همواره حداقل ۳۰۰ تا ۵۰۰ مگابایت حافظه داخلی را خالی نگه دارید تا سیستم‌عامل بتواند فایل‌های موقت خود را بسازد."),
                    GuideStep(4, "ریستارت هفتگی", "هفته‌ای دو بار گوشی را خاموش و روشن کنید تا حافظه موقت رم کاملاً تازه شود.")
                )
            )
        )
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "راهنمای تخصصی Samsung Galaxy J1",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "طراحی شده ویژه اندروید ۵.۰ (Lollipop) و رابط کاربری سامسونگ TouchWiz",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }

        items(topics, key = { it.id }) { topic ->
            TopicCard(topic = topic, onActionClick = onOpenAppSettings)
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun TopicCard(
    topic: GuideTopic,
    onActionClick: () -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("guide_card_${topic.id}")
            .clickable { isExpanded = !isExpanded },
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
                        .size(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = topic.icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(26.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = topic.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = topic.subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Icon(
                    imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = if (isExpanded) "بستن" else "باز کردن",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            AnimatedVisibility(visible = isExpanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp)
                ) {
                    // Steps List
                    topic.steps.forEach { step ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(26.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = StorageManagerUtil.toPersianDigits(step.stepNumber.toString()),
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Spacer(modifier = Modifier.width(10.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = step.title,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = step.description,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    // Pro Tip Box
                    topic.proTip?.let { tip ->
                        Spacer(modifier = Modifier.height(10.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.secondaryContainer)
                                .padding(12.dp)
                        ) {
                            Text(
                                text = tip,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    }

                    // Action button if applicable
                    if (topic.hasAction) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = onActionClick,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.Settings, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("ورود به مدیریت برنامه‌های گوشی")
                        }
                    }
                }
            }
        }
    }
}
