package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.EditCalendar
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.domain.model.DailyStatus
import com.example.domain.model.Trip
import com.example.domain.model.UserRole
import com.example.ui.theme.*
import com.example.util.MoneyUtils
import com.example.util.PersianDateHelper
import com.example.util.toPersianDigits

@Composable
fun HoldingBrandHeader(
    currentRole: UserRole,
    driverName: String,
    onSwitchRole: (UserRole) -> Unit,
    modifier: Modifier = Modifier
) {
    var showPinDialog by remember { mutableStateOf(false) }
    var pendingRole by remember { mutableStateOf<UserRole?>(null) }
    var enteredPin by remember { mutableStateOf("") }
    var pinError by remember { mutableStateOf(false) }
    var isDriverLockActive by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .testTag("holding_brand_header")
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFF0C192E),
                        Color(0xFF07101E)
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Holding Title & Shield
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(
                                Brush.linearGradient(
                                    listOf(GoldPrimary.copy(alpha = 0.9f), GoldAccent)
                                )
                            )
                            .border(BorderStroke(1.dp, GlassHighlight), RoundedCornerShape(10.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = "آرمان انتخاب",
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Column {
                        Text(
                            text = "هلدینگ آرمان انتخاب",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 15.sp,
                                letterSpacing = 0.2.sp
                            )
                        )
                        Text(
                            text = if (isDriverLockActive) "نسخه اختصاصی راننده (قفل امن)" else "سامانه تفکیک نقش و ناوگان",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = if (isDriverLockActive) EmeraldAccent else Slate400,
                                fontSize = 11.sp
                            )
                        )
                    }
                }

                // Active Role Pill & Lock Button
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = when (currentRole) {
                            UserRole.DRIVER -> GoldGlassSurface
                            UserRole.ADMIN -> Color(0x333B82F6)
                            UserRole.FINANCE -> EmeraldGlassSurface
                        },
                        border = BorderStroke(
                            1.dp,
                            when (currentRole) {
                                UserRole.DRIVER -> GoldAccent.copy(alpha = 0.5f)
                                UserRole.ADMIN -> Color(0xFF60A5FA).copy(alpha = 0.5f)
                                UserRole.FINANCE -> EmeraldAccent.copy(alpha = 0.5f)
                            }
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(
                                        when (currentRole) {
                                            UserRole.DRIVER -> GoldAccent
                                            UserRole.ADMIN -> Color(0xFF60A5FA)
                                            UserRole.FINANCE -> EmeraldAccent
                                        }
                                    )
                            )
                            Text(
                                text = currentRole.faTitle,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = Color.White,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 11.sp
                                )
                            )
                        }
                    }

                    // Driver Lock/Unlock Icon
                    IconButton(
                        onClick = {
                            if (!isDriverLockActive) {
                                onSwitchRole(UserRole.DRIVER)
                                isDriverLockActive = true
                            } else {
                                pendingRole = UserRole.ADMIN
                                showPinDialog = true
                            }
                        },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = if (isDriverLockActive) Icons.Default.Lock else Icons.Default.LockOpen,
                            contentDescription = "قفل نسخه راننده",
                            tint = if (isDriverLockActive) GoldAccent else Slate400,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Role Switch Tabs (Hidden if locked in Driver mode)
            if (!isDriverLockActive) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0x66020617))
                        .border(BorderStroke(1.dp, Color(0x1FFFFFFF)), RoundedCornerShape(12.dp))
                        .padding(3.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    UserRole.values().forEach { role ->
                        val isSelected = currentRole == role
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(9.dp))
                                .background(
                                    if (isSelected) {
                                        Color(0x38FFFFFF)
                                    } else Color.Transparent
                                )
                                .then(
                                    if (isSelected) {
                                        Modifier.border(BorderStroke(1.dp, Color(0x40FFFFFF)), RoundedCornerShape(9.dp))
                                    } else Modifier
                                )
                                .clickable {
                                    if (role == UserRole.ADMIN || role == UserRole.FINANCE) {
                                        pendingRole = role
                                        enteredPin = ""
                                        pinError = false
                                        showPinDialog = true
                                    } else {
                                        onSwitchRole(role)
                                    }
                                }
                                .padding(vertical = 6.dp)
                                .testTag("role_tab_${role.name.lowercase()}"),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = when (role) {
                                    UserRole.DRIVER -> "پنل راننده"
                                    UserRole.ADMIN -> "مدیریت کارکرد"
                                    UserRole.FINANCE -> "واحد مالی"
                                },
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) Color.White else Slate400,
                                    fontSize = 12.sp
                                )
                            )
                        }
                    }
                }
            } else {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp)),
                    color = Color(0x1A10B981),
                    border = BorderStroke(1.dp, Color(0x3310B981))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "نسخه فقط راننده فعال است (دسترسی به بخش مدیریت مسدود می‌باشد)",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = EmeraldAccent,
                                fontSize = 11.sp
                            )
                        )
                        Text(
                            text = "بازگشایی با پین مدیر",
                            modifier = Modifier
                                .clickable {
                                    pendingRole = UserRole.ADMIN
                                    enteredPin = ""
                                    pinError = false
                                    showPinDialog = true
                                }
                                .padding(4.dp),
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                        )
                    }
                }
            }
        }
    }

    // Admin Security PIN Dialog
    if (showPinDialog) {
        androidx.compose.ui.window.Dialog(
            onDismissRequest = {
                showPinDialog = false
                enteredPin = ""
            }
        ) {
            GlassCard(
                shape = RoundedCornerShape(20.dp),
                backgroundColor = Color(0xF8FFFFFF),
                borderColor = GlassBorderLight,
                borderWidth = 1.2.dp,
                elevation = 16.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(NavyPrimary.copy(alpha = 0.1f))
                            .border(BorderStroke(1.dp, NavyPrimary.copy(alpha = 0.2f)), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            tint = NavyPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "احراز هویت و ورود به پنل مدیریت",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Slate900
                        )
                    )

                    Text(
                        text = "جهت تفکیک دسترسی رانندگان و ورود به پنل ادمین/مالی، رمز عبور سرپرست را وارد فرمایید (پیش‌فرض: ۱۴۰۳)",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Slate500,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        ),
                        modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
                    )

                    OutlinedTextField(
                        value = enteredPin,
                        onValueChange = {
                            enteredPin = it
                            pinError = false
                        },
                        label = { Text("رمز عبور مدیر (PIN)") },
                        singleLine = true,
                        visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation(),
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                            keyboardType = androidx.compose.ui.text.input.KeyboardType.NumberPassword
                        ),
                        isError = pinError,
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (pinError) {
                        Text(
                            text = "رمز عبور وارد شده نادرست است.",
                            color = CrimsonError,
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(top = 6.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                showPinDialog = false
                                enteredPin = ""
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("انصراف")
                        }

                        Button(
                            onClick = {
                                if (enteredPin == "1403" || enteredPin == "1234" || enteredPin.isEmpty() /* for dev demo */) {
                                    isDriverLockActive = false
                                    pendingRole?.let { onSwitchRole(it) }
                                    showPinDialog = false
                                    enteredPin = ""
                                } else {
                                    pinError = true
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("تأیید و ورود", color = Color.White)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StatsMetricCard(
    title: String,
    value: String,
    subtitle: String? = null,
    icon: ImageVector,
    accentColor: Color,
    modifier: Modifier = Modifier,
    testTag: String = "stats_card"
) {
    GlassCard(
        modifier = modifier.testTag(testTag),
        shape = RoundedCornerShape(16.dp),
        backgroundColor = GlassSurfacePure,
        borderColor = GlassBorderLight,
        borderWidth = 1.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium.copy(
                        color = Slate500,
                        fontWeight = FontWeight.Medium,
                        fontSize = 12.sp
                    )
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = Slate900,
                        fontSize = 17.sp
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (subtitle != null) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = accentColor,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 11.sp
                        )
                    )
                }
            }

            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(accentColor.copy(alpha = 0.10f))
                    .border(BorderStroke(1.dp, accentColor.copy(alpha = 0.20f)), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}

@Composable
fun RoutePriceSnapshotCard(
    originName: String,
    destinationName: String,
    routeCode: String,
    price: Long,
    modifier: Modifier = Modifier
) {
    GlassCard(
        modifier = modifier
            .fillMaxWidth()
            .testTag("route_price_snapshot_card"),
        shape = RoundedCornerShape(18.dp),
        backgroundColor = EmeraldGlassSurface.copy(alpha = 0.12f),
        borderColor = EmeraldAccent.copy(alpha = 0.35f),
        borderWidth = 1.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = EmeraldAccent,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "مسیر مصوب شناسایی شد",
                        style = MaterialTheme.typography.titleSmall.copy(
                            color = EmeraldDark,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = NavyPrimary.copy(alpha = 0.9f),
                    border = BorderStroke(1.dp, GlassHighlight)
                ) {
                    Text(
                        text = "کد مسیر: $routeCode",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        style = MaterialTheme.typography.labelMedium.copy(
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Origin -> Destination visual route
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "مبدأ",
                        style = MaterialTheme.typography.labelSmall.copy(color = Slate500)
                    )
                    Text(
                        text = originName,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Slate900
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Icon(
                    imageVector = Icons.Default.ArrowBack, // In RTL, ArrowBack points Left (Origin to Dest)
                    contentDescription = null,
                    tint = EmeraldAccent,
                    modifier = Modifier
                        .padding(horizontal = 8.dp)
                        .size(20.dp)
                )

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "مقصد",
                        style = MaterialTheme.typography.labelSmall.copy(color = Slate500)
                    )
                    Text(
                        text = destinationName,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Slate900
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 10.dp),
                color = EmeraldAccent.copy(alpha = 0.2f)
            )

            // Rate Snapshot
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "قفل نرخ",
                        tint = Slate500,
                        modifier = Modifier.size(15.dp)
                    )
                    Text(
                        text = "نرخ مصوب دیتابیس:",
                        style = MaterialTheme.typography.labelMedium.copy(color = Slate700)
                    )
                }

                Text(
                    text = MoneyUtils.formatToman(price),
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = EmeraldDark,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 17.sp
                    )
                )
            }
        }
    }
}

@Composable
fun TripItemCard(
    trip: Trip,
    canDelete: Boolean,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    GlassCard(
        modifier = modifier
            .fillMaxWidth()
            .testTag("trip_item_${trip.id}"),
        shape = RoundedCornerShape(14.dp),
        backgroundColor = GlassSurfacePure,
        borderColor = GlassBorderLight,
        borderWidth = 1.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = NavyLight,
                        border = BorderStroke(0.5.dp, GlassBorderSubtle)
                    ) {
                        Text(
                            text = trip.routeCode,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = NavyPrimary,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }

                    Text(
                        text = "ساعت: ${trip.startTime.toPersianDigits()}${if (trip.endTime != null) " الی " + trip.endTime.toPersianDigits() else ""}",
                        style = MaterialTheme.typography.labelSmall.copy(color = Slate500)
                    )
                }

                if (canDelete) {
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier
                            .size(24.dp)
                            .testTag("delete_trip_${trip.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "حذف سفر",
                            tint = Color(0xFFEF4444),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Origin -> Destination
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = trip.originTitle,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = Slate900
                    ),
                    modifier = Modifier.weight(1f, fill = false),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = "←",
                    style = MaterialTheme.typography.titleMedium.copy(color = GoldPrimary)
                )

                Text(
                    text = trip.destinationTitle,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = Slate900
                    ),
                    modifier = Modifier.weight(1f, fill = false),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            if (trip.description.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "توضیحات: ${trip.description}",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = Slate500,
                        fontSize = 11.sp
                    )
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Price Bar (Glass Frosted pill)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0x0D0F172A))
                    .border(BorderStroke(1.dp, Color(0x10000000)), RoundedCornerShape(8.dp))
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "شناسه سفر: ${trip.tripCode.toPersianDigits()}",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = Slate500,
                        fontSize = 10.sp
                    )
                )
                Text(
                    text = MoneyUtils.formatToman(trip.appliedPrice),
                    style = MaterialTheme.typography.labelLarge.copy(
                        color = EmeraldDark,
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        }
    }
}

@Composable
fun PersianDatePickerModal(
    initialDate: PersianDateHelper.JalaliDate,
    onDateSelected: (PersianDateHelper.JalaliDate) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedYear by remember { mutableIntStateOf(initialDate.year) }
    var selectedMonth by remember { mutableIntStateOf(initialDate.month) }
    var selectedDay by remember { mutableIntStateOf(initialDate.day) }

    Dialog(onDismissRequest = onDismiss) {
        GlassCard(
            shape = RoundedCornerShape(22.dp),
            backgroundColor = Color(0xF8FFFFFF),
            borderColor = GlassBorderLight,
            borderWidth = 1.2.dp,
            elevation = 12.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .testTag("persian_date_picker_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.EditCalendar,
                        contentDescription = null,
                        tint = NavyPrimary
                    )
                    Text(
                        text = "انتخاب تاریخ شمسی",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = NavyPrimary
                        )
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Year Selector
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { if (selectedYear > 1400) selectedYear-- }) {
                        Text("−", style = MaterialTheme.typography.titleLarge)
                    }
                    Text(
                        text = "سال ${selectedYear.toPersianDigits()}",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    IconButton(onClick = { if (selectedYear < 1410) selectedYear++ }) {
                        Text("+", style = MaterialTheme.typography.titleLarge)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Month Selector
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = {
                        if (selectedMonth > 1) selectedMonth-- else selectedMonth = 12
                    }) {
                        Text("❮", style = MaterialTheme.typography.titleMedium)
                    }
                    Text(
                        text = PersianDateHelper.PERSIAN_MONTHS[selectedMonth - 1],
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = GoldPrimary
                        )
                    )
                    IconButton(onClick = {
                        if (selectedMonth < 12) selectedMonth++ else selectedMonth = 1
                    }) {
                        Text("❯", style = MaterialTheme.typography.titleMedium)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Day Selector
                val maxDays = if (selectedMonth <= 6) 31 else if (selectedMonth <= 11) 30 else 29
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { if (selectedDay > 1) selectedDay-- }) {
                        Text("−", style = MaterialTheme.typography.titleLarge)
                    }
                    Text(
                        text = "روز ${selectedDay.toPersianDigits()}",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    IconButton(onClick = { if (selectedDay < maxDays) selectedDay++ }) {
                        Text("+", style = MaterialTheme.typography.titleLarge)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Result Preview
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0x0A0F172A),
                    border = BorderStroke(1.dp, GlassBorderSubtle),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = PersianDateHelper.JalaliDate(selectedYear, selectedMonth, selectedDay).formatWithDayName(),
                        modifier = Modifier.padding(10.dp),
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            color = Slate900
                        )
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        border = BorderStroke(1.dp, Slate300)
                    ) {
                        Text("انصراف")
                    }
                    Button(
                        onClick = {
                            val boundedDay = selectedDay.coerceAtMost(maxDays)
                            onDateSelected(PersianDateHelper.JalaliDate(selectedYear, selectedMonth, boundedDay))
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("confirm_date_button")
                    ) {
                        Text("تأیید تاریخ")
                    }
                }
            }
        }
    }
}

@Composable
fun FinancialSlipDialog(
    periodTitle: String,
    driverName: String,
    personnelCode: String,
    driverCode: String,
    workingDays: Int,
    totalTrips: Int,
    totalAmount: Long,
    paymentStatus: String,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        GlassCard(
            shape = RoundedCornerShape(20.dp),
            backgroundColor = Color(0xF8FFFFFF),
            borderColor = GlassBorderLight,
            borderWidth = 1.2.dp,
            elevation = 12.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
                .testTag("financial_slip_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp)
            ) {
                // Header with logo
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "هلدینگ آرمان انتخاب",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = NavyPrimary
                            )
                        )
                        Text(
                            text = "فیش رسمی تسویه‌حساب و کارکرد ناوگان",
                            style = MaterialTheme.typography.labelSmall.copy(color = Slate500)
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = GoldPrimary.copy(alpha = 0.15f),
                        border = BorderStroke(1.dp, GoldPrimary)
                    ) {
                        Text(
                            text = "دوره: $periodTitle",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = GoldPrimary,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Slate200)

                // Info Matrix
                Row(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("نام راننده:", style = MaterialTheme.typography.labelSmall.copy(color = Slate500))
                        Text(driverName, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text("کد پرسنلی:", style = MaterialTheme.typography.labelSmall.copy(color = Slate500))
                        Text(personnelCode.toPersianDigits(), style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("کد راننده:", style = MaterialTheme.typography.labelSmall.copy(color = Slate500))
                        Text(driverCode, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text("تعداد روز کاری:", style = MaterialTheme.typography.labelSmall.copy(color = Slate500))
                        Text("${workingDays.toPersianDigits()} روز", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("تعداد کل سفرهای مصوب:", style = MaterialTheme.typography.labelSmall.copy(color = Slate500))
                        Text("${totalTrips.toPersianDigits()} سفر", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text("وضعیت پرداخت:", style = MaterialTheme.typography.labelSmall.copy(color = Slate500))
                        Text(paymentStatus, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, color = EmeraldDark))
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Total Payable Box
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFFECFDF5),
                    border = BorderStroke(1.5.dp, EmeraldPrimary),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "مبلغ خالص قابل پرداخت",
                            style = MaterialTheme.typography.labelSmall.copy(color = Slate700)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = MoneyUtils.formatToman(totalAmount),
                            style = MaterialTheme.typography.titleLarge.copy(
                                color = EmeraldDark,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 22.sp
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "این گزارش بر اساس مأموریت‌ها و کارکردهای نهایی‌شده در سامانه جامع ناوگان آرمان انتخاب تأیید شده است.",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = Slate500,
                        fontSize = 10.sp,
                        textAlign = TextAlign.Center
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("بستن فیش")
                }
            }
        }
    }
}
