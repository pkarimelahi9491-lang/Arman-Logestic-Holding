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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AltRoute
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.material3.Surface
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.LocationItem
import com.example.domain.model.RouteItem
import com.example.ui.theme.EmeraldDark
import com.example.ui.theme.EmeraldLight
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.NavyPrimary
import com.example.ui.theme.Slate100
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate500
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate900
import com.example.ui.theme.GlassCard
import com.example.ui.theme.GlassSurfacePure
import com.example.ui.theme.GlassBorderLight
import com.example.ui.theme.GlassHighlight
import com.example.util.MoneyUtils
import com.example.util.PersianDateHelper
import com.example.util.toPersianDigits

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminRoutesScreen(
    routes: List<RouteItem>,
    locations: List<LocationItem>,
    onNavigateBack: () -> Unit,
    onSaveRoute: (
        routeId: String?,
        routeCode: String,
        originId: Long,
        originName: String,
        destinationId: Long,
        destinationName: String,
        price: Long,
        description: String
    ) -> Unit,
    onSyncCsv: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var showAddRouteDialog by remember { mutableStateOf(false) }
    var showCsvImportDialog by remember { mutableStateOf(false) }
    var selectedRouteForPriceEdit by remember { mutableStateOf<RouteItem?>(null) }
    var selectedRouteForHistory by remember { mutableStateOf<RouteItem?>(null) }

    val filteredRoutes = routes.filter {
        it.routeCode.contains(searchQuery, ignoreCase = true) ||
                it.originName.contains(searchQuery, ignoreCase = true) ||
                it.destinationName.contains(searchQuery, ignoreCase = true) ||
                it.description.contains(searchQuery, ignoreCase = true)
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .testTag("admin_routes_screen"),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Header
        item {
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("routes_back_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "بازگشت",
                            tint = NavyPrimary
                        )
                    }
                    Column {
                        Text(
                            text = "مدیریت مسیرها و تعرفه‌های مصوب",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = NavyPrimary
                            )
                        )
                        Text(
                            text = "تعریف کدهای اختصاصی و همگام‌سازی اکسل تعرفه هلدینگ",
                            style = MaterialTheme.typography.labelSmall.copy(color = Slate500)
                        )
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = { showCsvImportDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("import_csv_routes_btn")
                    ) {
                        Text("📥 بروزرسانی اکسل/CSV")
                    }

                    Button(
                        onClick = { showAddRouteDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("add_new_route_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.size(4.dp))
                        Text("مسیر دستی")
                    }
                }
            }
        }

        // Search Bar
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("جستجوی کد مسیر، مبدأ یا مقصد...") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        tint = Slate500
                    )
                },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("search_routes_input"),
                shape = RoundedCornerShape(12.dp)
            )
        }

        // Section Title
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "لیست مسیرهای تعریف‌شده (${filteredRoutes.size.toPersianDigits()} مسیر)",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = Slate900
                    )
                )
                Text(
                    text = "قیمت‌ها بر اساس تومان",
                    style = MaterialTheme.typography.labelSmall.copy(color = Slate500)
                )
            }
        }

        items(filteredRoutes, key = { it.id }) { route ->
            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("route_card_${route.routeCode}"),
                shape = RoundedCornerShape(14.dp),
                backgroundColor = GlassSurfacePure,
                borderColor = GlassBorderLight
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
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = NavyPrimary.copy(alpha = 0.9f),
                            border = BorderStroke(0.5.dp, GlassHighlight)
                        ) {
                            Text(
                                text = "کد: ${route.routeCode}",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                style = MaterialTheme.typography.labelMedium.copy(
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                )
                            )
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            // Edit price button
                            OutlinedButton(
                                onClick = { selectedRouteForPriceEdit = route },
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .height(32.dp)
                                    .testTag("edit_route_price_${route.routeCode}")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.size(4.dp))
                                Text("تغییر نرخ", style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Route Path
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = route.originName,
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            modifier = Modifier.weight(1f, fill = false),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = " ← ",
                            style = MaterialTheme.typography.titleMedium.copy(color = GoldPrimary),
                            modifier = Modifier.padding(horizontal = 4.dp)
                        )
                        Text(
                            text = route.destinationName,
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            modifier = Modifier.weight(1f, fill = false),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    if (route.description.isNotBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = route.description,
                            style = MaterialTheme.typography.bodySmall.copy(color = Slate500)
                        )
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = Slate200)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "نرخ مصوب سیستم:",
                            style = MaterialTheme.typography.labelMedium.copy(color = Slate700)
                        )
                        Text(
                            text = MoneyUtils.formatToman(route.currentPrice),
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = EmeraldDark
                            )
                        )
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // Csv Import Dialog
    if (showCsvImportDialog) {
        CsvImportModal(
            onDismiss = { showCsvImportDialog = false },
            onImport = { csvContent ->
                onSyncCsv(csvContent)
                showCsvImportDialog = false
            }
        )
    }

    // Add Route Dialog
    if (showAddRouteDialog) {
        AddRouteModal(
            locations = locations,
            onDismiss = { showAddRouteDialog = false },
            onConfirm = { routeCode, origin, dest, price, desc ->
                onSaveRoute(null, routeCode, origin.id, origin.name, dest.id, dest.name, price, desc)
                showAddRouteDialog = false
            }
        )
    }

    // Edit Price Dialog
    if (selectedRouteForPriceEdit != null) {
        val targetRoute = selectedRouteForPriceEdit!!
        var newPriceInput by remember { mutableStateOf(targetRoute.currentPrice.toString()) }

        AlertDialog(
            onDismissRequest = { selectedRouteForPriceEdit = null },
            title = {
                Text(
                    text = "تغییر نرخ مصوب مسیر ${targetRoute.routeCode}",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "${targetRoute.originName} ← ${targetRoute.destinationName}",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = "نرخ فعلی: ${MoneyUtils.formatToman(targetRoute.currentPrice)}",
                        style = MaterialTheme.typography.bodySmall.copy(color = Slate500)
                    )

                    OutlinedTextField(
                        value = newPriceInput,
                        onValueChange = { newPriceInput = it.filter { ch -> ch.isDigit() } },
                        label = { Text("نرخ مصوب جدید (تومان)") },
                        placeholder = { Text("مثلاً: 320000") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("input_new_route_price"),
                        shape = RoundedCornerShape(10.dp)
                    )

                    Text(
                        text = "⚠️ تغییر قیمت تنها بر سفرهای آینده اعمال می‌شود و سفرهای ثبت‌شده قبلی با نرخ Snapshot خود دست‌نخورده باقی می‌مانند.",
                        style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFFB45309))
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val parsed = newPriceInput.toLongOrNull() ?: targetRoute.currentPrice
                        onSaveRoute(
                            targetRoute.id,
                            targetRoute.routeCode,
                            targetRoute.originId,
                            targetRoute.originName,
                            targetRoute.destinationId,
                            targetRoute.destinationName,
                            parsed,
                            targetRoute.description
                        )
                        selectedRouteForPriceEdit = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                    modifier = Modifier.testTag("confirm_update_price_btn")
                ) {
                    Text("ثبت و اعمال نرخ")
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedRouteForPriceEdit = null }) {
                    Text("انصراف")
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddRouteModal(
    locations: List<LocationItem>,
    onDismiss: () -> Unit,
    onConfirm: (routeCode: String, origin: LocationItem, destination: LocationItem, price: Long, description: String) -> Unit
) {
    var routeCode by remember { mutableStateOf("AR-${(100..999).random()}") }
    var selectedOrigin by remember { mutableStateOf<LocationItem?>(null) }
    var selectedDestination by remember { mutableStateOf<LocationItem?>(null) }
    var priceInput by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

    var originExpanded by remember { mutableStateOf(false) }
    var destinationExpanded by remember { mutableStateOf(false) }

    val isValid = routeCode.isNotBlank() && selectedOrigin != null && selectedDestination != null && priceInput.isNotBlank()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "تعریف مسیر مصوب جدید",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = NavyPrimary)
            )
        },
        text = {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    OutlinedTextField(
                        value = routeCode,
                        onValueChange = { routeCode = it },
                        label = { Text("کد اختصاصی مسیر") },
                        placeholder = { Text("AR-009") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("modal_route_code_input")
                    )
                }

                item {
                    // Origin dropdown
                    ExposedDropdownMenuBox(
                        expanded = originExpanded,
                        onExpandedChange = { originExpanded = it },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = selectedOrigin?.name ?: "",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("مبدأ مصوب") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = originExpanded) },
                            modifier = Modifier.menuAnchor().fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = originExpanded,
                            onDismissRequest = { originExpanded = false }
                        ) {
                            locations.forEach { loc ->
                                DropdownMenuItem(
                                    text = { Text(loc.name) },
                                    onClick = {
                                        selectedOrigin = loc
                                        originExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                item {
                    // Destination dropdown
                    ExposedDropdownMenuBox(
                        expanded = destinationExpanded,
                        onExpandedChange = { destinationExpanded = it },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = selectedDestination?.name ?: "",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("مقصد مصوب") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = destinationExpanded) },
                            modifier = Modifier.menuAnchor().fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = destinationExpanded,
                            onDismissRequest = { destinationExpanded = false }
                        ) {
                            locations.forEach { loc ->
                                DropdownMenuItem(
                                    text = { Text(loc.name) },
                                    onClick = {
                                        selectedDestination = loc
                                        destinationExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                item {
                    OutlinedTextField(
                        value = priceInput,
                        onValueChange = { priceInput = it.filter { ch -> ch.isDigit() } },
                        label = { Text("نرخ مصوب مأموریت (تومان)") },
                        placeholder = { Text("مثلاً: 350000") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("modal_route_price_input")
                    )
                }

                item {
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("توضیحات مسیر") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (isValid) {
                        val parsed = priceInput.toLongOrNull() ?: 0
                        onConfirm(routeCode, selectedOrigin!!, selectedDestination!!, parsed, description)
                    }
                },
                enabled = isValid,
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                modifier = Modifier.testTag("modal_save_route_btn")
            ) {
                Text("ذخیره و فعال‌سازی")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("انصراف")
            }
        }
    )
}

@Composable
fun CsvImportModal(
    onDismiss: () -> Unit,
    onImport: (String) -> Unit
) {
    var rawText by remember { mutableStateOf(com.example.data.local.CsvFleetData.INITIAL_CSV_RAW) }
    val detectedTiers = remember(rawText) {
        com.example.data.local.CsvFleetData.parseCsvTiers(rawText)
    }
    val totalDestinationsCount = remember(detectedTiers) {
        detectedTiers.sumOf { it.destinations.size }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text(
                    text = "📥 همگام‌سازی و ورود اطلاعات اکسل / CSV",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = NavyPrimary
                    )
                )
                Text(
                    text = "۱۵ ردیف مسافتی مصوب و مقاصد هلدینگ انتخاب",
                    style = MaterialTheme.typography.labelSmall.copy(color = Slate500)
                )
            }
        },
        text = {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(380.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    Surface(
                        color = Slate100,
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, Slate200),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(
                                text = "وضعیت فایل اکسل شناسایی‌شده:",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, color = NavyPrimary)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "• تعداد رده‌های مسافتی: ${detectedTiers.size.toPersianDigits()} رده (۳۰ تا ۱۰۰ کیلومتر)",
                                style = MaterialTheme.typography.bodySmall.copy(color = Slate700)
                            )
                            Text(
                                text = "• تعداد کل مقاصد استخراج‌شده: ${totalDestinationsCount.toPersianDigits()} مقصد مصوب",
                                style = MaterialTheme.typography.bodySmall.copy(color = EmeraldDark, fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "• نرخ پایه هر کیلومتر: ۹۰,۰۰۰ ریال (۹,۰۰۰ تومان)",
                                style = MaterialTheme.typography.bodySmall.copy(color = Slate700)
                            )
                        }
                    }
                }

                item {
                    Text(
                        text = "محتوای جدول CSV یا متن کپی‌شده از اکسل:",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold, color = Slate700)
                    )
                }

                item {
                    OutlinedTextField(
                        value = rawText,
                        onValueChange = { rawText = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .testTag("csv_raw_input"),
                        shape = RoundedCornerShape(8.dp),
                        textStyle = MaterialTheme.typography.bodySmall
                    )
                }

                item {
                    Text(
                        text = "راهنما: با کلیک روی دکمه زیر، تمامی مبدأها، مقاصد و مسیرهای تعریف شده در این متن با پایگاه داده برنامه همگام و ذخیره می‌شوند.",
                        style = MaterialTheme.typography.labelSmall.copy(color = Slate500)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onImport(rawText) },
                enabled = detectedTiers.isNotEmpty(),
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                modifier = Modifier.testTag("confirm_import_csv_btn")
            ) {
                Text("همگام‌سازی و ذخیره در سیستم (${detectedTiers.size.toPersianDigits()} رده)")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("انصراف")
            }
        }
    )
}
