package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.Driver
import com.example.domain.model.LocationItem
import com.example.domain.model.RouteItem
import com.example.ui.components.PersianDatePickerModal
import com.example.ui.components.RoutePriceSnapshotCard
import com.example.ui.theme.*
import com.example.util.MoneyUtils
import com.example.util.PersianDateHelper
import com.example.util.toPersianDigits

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterTripScreen(
    driver: Driver,
    initialDate: PersianDateHelper.JalaliDate,
    locations: List<LocationItem>,
    allRoutes: List<RouteItem>,
    onNavigateBack: () -> Unit,
    onSubmitTrip: (
        driverId: String,
        jalaliDate: String,
        routeId: String,
        startTime: String,
        endTime: String?,
        description: String
    ) -> Unit,
    modifier: Modifier = Modifier
) {
    var tripDate by remember { mutableStateOf(initialDate) }
    var showDatePicker by remember { mutableStateOf(false) }

    var startTime by remember { mutableStateOf(PersianDateHelper.getCurrentTimeString()) }
    var endTime by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

    var selectedOrigin by remember {
        mutableStateOf<LocationItem?>(
            locations.firstOrNull { it.name.contains("انبار مرکزی") || it.name.contains("مورچه خورت") }
                ?: locations.firstOrNull()
        )
    }
    var selectedDestination by remember { mutableStateOf<LocationItem?>(null) }

    var showOriginPicker by remember { mutableStateOf(false) }
    var showDestinationPicker by remember { mutableStateOf(false) }

    LaunchedEffect(locations) {
        if (selectedOrigin == null && locations.isNotEmpty()) {
            selectedOrigin = locations.firstOrNull { it.name.contains("انبار مرکزی") || it.name.contains("مورچه خورت") }
                ?: locations.firstOrNull()
        }
    }

    var showRouteRequestSuccessDialog by remember { mutableStateOf(false) }

    // Automatically check route matching in routes database
    val matchedRoute: RouteItem? = remember(selectedOrigin, selectedDestination, allRoutes) {
        val o = selectedOrigin
        val d = selectedDestination
        if (o != null && d != null) {
            allRoutes.find { it.originId == o.id && it.destinationId == d.id && it.isActive }
        } else {
            null
        }
    }

    val isRouteNotFound = selectedOrigin != null && selectedDestination != null && matchedRoute == null

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .testTag("register_trip_screen"),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Header
        item {
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IconButton(
                    onClick = onNavigateBack,
                    modifier = Modifier.testTag("back_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "بازگشت",
                        tint = NavyPrimary
                    )
                }

                Column {
                    Text(
                        text = "ثبت سفر و مأموریت جدید",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = NavyPrimary
                        )
                    )
                    Text(
                        text = "راننده: ${driver.fullName} (${driver.driverCode})",
                        style = MaterialTheme.typography.labelMedium.copy(color = Slate500)
                    )
                }
            }
        }

        // 1. Date and Time Matrix Card
        item {
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                backgroundColor = GlassSurfacePure,
                borderColor = GlassBorderLight
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "۱. زمان و تاریخ مأموریت",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = NavyPrimary
                        )
                    )

                    // Date Picker Button
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0x0A0F172A),
                        border = BorderStroke(1.dp, GlassBorderSubtle),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .clickable { showDatePicker = true }
                            .testTag("trip_date_selector")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CalendarMonth,
                                    contentDescription = null,
                                    tint = NavyPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    text = "تاریخ سفر: ${tripDate.formatWithDayName()}",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = Slate900
                                    )
                                )
                            }
                            Text(
                                text = "تغییر تاریخ",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = GoldPrimary,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    }

                    // Start Time & End Time
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = startTime,
                            onValueChange = { startTime = it },
                            label = { Text("ساعت شروع") },
                            placeholder = { Text("08:30") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Schedule,
                                    contentDescription = null,
                                    tint = NavyPrimary,
                                    modifier = Modifier.size(18.dp)
                                )
                            },
                            singleLine = true,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("input_start_time"),
                            shape = RoundedCornerShape(10.dp)
                        )

                        OutlinedTextField(
                            value = endTime,
                            onValueChange = { endTime = it },
                            label = { Text("ساعت پایان (اختیاری)") },
                            placeholder = { Text("09:45") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Schedule,
                                    contentDescription = null,
                                    tint = Slate500,
                                    modifier = Modifier.size(18.dp)
                                )
                            },
                            singleLine = true,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("input_end_time"),
                            shape = RoundedCornerShape(10.dp)
                        )
                    }
                }
            }
        }

        // 2. Origin & Destination Selector
        item {
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                backgroundColor = GlassSurfacePure,
                borderColor = GlassBorderLight
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "۲. انتخاب مبدأ و مقصد مصوب",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = NavyPrimary
                        )
                    )

                    // Origin Selector Button
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0x0A0F172A),
                        border = BorderStroke(1.dp, if (selectedOrigin != null) NavyPrimary.copy(alpha = 0.5f) else GlassBorderSubtle),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .clickable { showOriginPicker = true }
                            .testTag("select_origin_dropdown")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.LocationOn,
                                    contentDescription = null,
                                    tint = NavyPrimary,
                                    modifier = Modifier.size(22.dp)
                                )
                                Column {
                                    Text(
                                        text = "مبدأ مأموریت",
                                        style = MaterialTheme.typography.labelSmall.copy(color = Slate500)
                                    )
                                    Text(
                                        text = selectedOrigin?.name ?: "انتخاب مبدأ از میان مکان‌های مصوب...",
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontWeight = if (selectedOrigin != null) FontWeight.Bold else FontWeight.Normal,
                                            color = if (selectedOrigin != null) Slate900 else Slate400
                                        )
                                    )
                                }
                            }
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = NavyLight
                            ) {
                                Text(
                                    text = if (selectedOrigin != null) "تغییر" else "انتخاب",
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                    style = MaterialTheme.typography.labelSmall.copy(color = NavyPrimary, fontWeight = FontWeight.Bold)
                                )
                            }
                        }
                    }

                    // Destination Selector Button
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0x0A0F172A),
                        border = BorderStroke(1.dp, if (selectedDestination != null) GoldAccent.copy(alpha = 0.6f) else GlassBorderSubtle),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .clickable { showDestinationPicker = true }
                            .testTag("select_destination_dropdown")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.LocationOn,
                                    contentDescription = null,
                                    tint = GoldPrimary,
                                    modifier = Modifier.size(22.dp)
                                )
                                Column {
                                    Text(
                                        text = "مقصد مأموریت",
                                        style = MaterialTheme.typography.labelSmall.copy(color = Slate500)
                                    )
                                    Text(
                                        text = selectedDestination?.name ?: "انتخاب مقصد (جستجو در ۱۵۰+ مکان مصوب)...",
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontWeight = if (selectedDestination != null) FontWeight.Bold else FontWeight.Normal,
                                            color = if (selectedDestination != null) Slate900 else Slate400
                                        )
                                    )
                                }
                            }
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = GoldLight
                            ) {
                                Text(
                                    text = if (selectedDestination != null) "تغییر" else "انتخاب",
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                    style = MaterialTheme.typography.labelSmall.copy(color = GoldPrimary, fontWeight = FontWeight.Bold)
                                )
                            }
                        }
                    }

                    // Quick switch button
                    if (selectedOrigin != null && selectedDestination != null) {
                        TextButton(
                            onClick = {
                                val temp = selectedOrigin
                                selectedOrigin = selectedDestination
                                selectedDestination = temp
                            },
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        ) {
                            Text("⇅ جابجایی مبدأ و مقصد")
                        }
                    }
                }
            }
        }

        // 3. Database Route Code & Fixed Price Snapshot Result
        item {
            AnimatedVisibility(visible = matchedRoute != null) {
                if (matchedRoute != null && selectedOrigin != null && selectedDestination != null) {
                    RoutePriceSnapshotCard(
                        originName = selectedOrigin!!.name,
                        destinationName = selectedDestination!!.name,
                        routeCode = matchedRoute.routeCode,
                        price = matchedRoute.currentPrice
                    )
                }
            }

            // Warning if route not defined
            AnimatedVisibility(visible = isRouteNotFound) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("route_not_found_card"),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = CrimsonLight),
                    border = BorderStroke(1.dp, CrimsonError)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = CrimsonError,
                                modifier = Modifier.size(22.dp)
                            )
                            Text(
                                text = "این مسیر در سیستم تعریف نشده است!",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    color = CrimsonError,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "لطفاً با مسئول محاسبه کارکرد ناوگان تماس بگیرید یا درخواست ایجاد مسیر ارسال نمایید. راننده مجاز به تعیین قیمت دستی نمی‌باشد.",
                            style = MaterialTheme.typography.bodySmall.copy(color = Slate700)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Button(
                            onClick = { showRouteRequestSuccessDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = CrimsonError),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("ارسال درخواست تعریف مسیر به مدیریت")
                        }
                    }
                }
            }
        }

        // 4. Description (Optional)
        item {
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("توضیحات سفر (اختیاری)") },
                placeholder = { Text("مثلاً: جابجایی کارشناسان فنی یا ترانسفر مدیران...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("input_trip_description"),
                shape = RoundedCornerShape(10.dp),
                minLines = 2
            )
        }

        // 5. Submit Button
        item {
            Button(
                onClick = {
                    if (matchedRoute != null) {
                        onSubmitTrip(
                            driver.id,
                            tripDate.formatStandard(),
                            matchedRoute.id,
                            startTime.ifBlank { "08:00" },
                            endTime.ifBlank { null },
                            description
                        )
                    }
                },
                enabled = matchedRoute != null,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = EmeraldPrimary,
                    disabledContainerColor = Slate300
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("confirm_and_submit_trip_button")
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = Color.White
                    )
                    Text(
                        text = if (matchedRoute != null) "تأیید و ثبت نهایی سفر (${MoneyUtils.formatToman(matchedRoute.currentPrice)})" else "لطفاً مسیر معتبر را انتخاب کنید",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    )
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    if (showDatePicker) {
        PersianDatePickerModal(
            initialDate = tripDate,
            onDateSelected = { newDate ->
                tripDate = newDate
            },
            onDismiss = { showDatePicker = false }
        )
    }

    if (showOriginPicker) {
        SearchableLocationModal(
            title = "انتخاب مبدأ مأموریت",
            locations = locations,
            selectedLocation = selectedOrigin,
            onSelect = {
                selectedOrigin = it
                showOriginPicker = false
            },
            onDismiss = { showOriginPicker = false }
        )
    }

    if (showDestinationPicker) {
        SearchableLocationModal(
            title = "انتخاب مقصد مأموریت (تعرفه‌های مصوب هلدینگ)",
            locations = locations,
            selectedLocation = selectedDestination,
            onSelect = {
                selectedDestination = it
                showDestinationPicker = false
            },
            onDismiss = { showDestinationPicker = false }
        )
    }

    if (showRouteRequestSuccessDialog) {
        AlertDialog(
            onDismissRequest = { showRouteRequestSuccessDialog = false },
            title = {
                Text(
                    text = "درخواست ثبت مسیر ارسال شد",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            },
            text = {
                Text("درخواست تعریف مسیر بین «${selectedOrigin?.name}» و «${selectedDestination?.name}» برای مسئول ناوگان ارسال گردید و پس از تصویب نرخ در پنل فعال خواهد شد.")
            },
            confirmButton = {
                Button(
                    onClick = { showRouteRequestSuccessDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary)
                ) {
                    Text("متوجه شدم")
                }
            }
        )
    }
}

@Composable
fun SearchableLocationModal(
    title: String,
    locations: List<LocationItem>,
    selectedLocation: LocationItem?,
    onSelect: (LocationItem) -> Unit,
    onDismiss: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    val filtered = remember(searchQuery, locations) {
        if (searchQuery.isBlank()) {
            locations
        } else {
            locations.filter {
                it.name.contains(searchQuery, ignoreCase = true) ||
                        it.city.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = NavyPrimary
                    )
                )
                Text(
                    text = "تعداد کل مکان‌های موجود: ${locations.size.toPersianDigits()} مکان",
                    style = MaterialTheme.typography.labelSmall.copy(color = Slate500)
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(420.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("جستجوی نام مقصد، شهر یا شهرک صنعتی...") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = NavyPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("search_location_input"),
                    shape = RoundedCornerShape(10.dp)
                )

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (filtered.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "مکانی با این عنوان یافت نشد",
                                    style = MaterialTheme.typography.bodyMedium.copy(color = Slate500)
                                )
                            }
                        }
                    } else {
                        items(filtered, key = { it.id }) { loc ->
                            val isSelected = selectedLocation?.id == loc.id
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isSelected) NavyLight else Slate50,
                                border = BorderStroke(1.dp, if (isSelected) NavyPrimary else Slate200),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { onSelect(loc) }
                                    .testTag("location_item_${loc.id}")
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp, vertical = 10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = loc.name,
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                color = if (isSelected) NavyPrimary else Slate900
                                            )
                                        )
                                        Text(
                                            text = "محدوده / شهر: ${loc.city}",
                                            style = MaterialTheme.typography.labelSmall.copy(color = Slate500)
                                        )
                                    }

                                    if (isSelected) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = null,
                                            tint = NavyPrimary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("بستن")
            }
        }
    )
}
