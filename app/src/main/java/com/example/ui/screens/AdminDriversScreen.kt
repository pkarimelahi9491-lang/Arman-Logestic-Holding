package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.Driver
import com.example.ui.theme.EmeraldDark
import com.example.ui.theme.EmeraldLight
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.GoldLight
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.NavyPrimary
import com.example.ui.theme.Slate100
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate300
import com.example.ui.theme.Slate500
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate900
import com.example.ui.theme.GlassCard
import com.example.ui.theme.GlassSurfacePure
import com.example.ui.theme.GlassBorderLight
import com.example.util.PersianDateHelper
import com.example.util.toPersianDigits

@Composable
fun AdminDriversScreen(
    drivers: List<Driver>,
    onNavigateBack: () -> Unit,
    onSaveDriver: (Driver) -> Unit,
    onToggleActive: (driverId: String, currentStatus: Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var showAddDriverDialog by remember { mutableStateOf(false) }

    val filteredDrivers = drivers.filter {
        it.fullName.contains(searchQuery, ignoreCase = true) ||
                it.driverCode.contains(searchQuery, ignoreCase = true) ||
                it.personnelCode.contains(searchQuery, ignoreCase = true) ||
                it.carPlate.contains(searchQuery, ignoreCase = true)
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .testTag("admin_drivers_screen"),
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
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "بازگشت",
                            tint = NavyPrimary
                        )
                    }
                    Column {
                        Text(
                            text = "مدیریت رانندگان ناوگان",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = NavyPrimary
                            )
                        )
                        Text(
                            text = "ثبت اطلاعات هویتی، پلاک و وضعیت فعالیت",
                            style = MaterialTheme.typography.labelSmall.copy(color = Slate500)
                        )
                    }
                }

                Button(
                    onClick = { showAddDriverDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.testTag("add_driver_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.size(4.dp))
                    Text("راننده جدید")
                }
            }
        }

        // Search Bar
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("جستجوی نام، کد پرسنلی یا پلاک...") },
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
                    .testTag("search_drivers_input"),
                shape = RoundedCornerShape(12.dp)
            )
        }

        items(filteredDrivers, key = { it.id }) { driver ->
            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("driver_item_${driver.driverCode}"),
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
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (driver.isActive) NavyPrimary.copy(alpha = 0.1f) else Slate200
                                    )
                                    .border(BorderStroke(1.dp, if (driver.isActive) NavyPrimary.copy(alpha = 0.2f) else Slate300), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = null,
                                    tint = if (driver.isActive) NavyPrimary else Slate500,
                                    modifier = Modifier.size(22.dp)
                                )
                            }

                            Column {
                                Text(
                                    text = driver.fullName,
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = Slate900)
                                )
                                Text(
                                    text = "کد راننده: ${driver.driverCode} | پرسنلی: ${driver.personnelCode.toPersianDigits()}",
                                    style = MaterialTheme.typography.labelSmall.copy(color = Slate500)
                                )
                            }
                        }

                        // Active / Inactive Switch
                        Switch(
                            checked = driver.isActive,
                            onCheckedChange = { onToggleActive(driver.id, driver.isActive) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = EmeraldPrimary,
                                checkedTrackColor = EmeraldLight
                            )
                        )
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = Slate200.copy(alpha = 0.5f))

                    // Vehicle and phone info
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.DirectionsCar,
                                contentDescription = null,
                                tint = GoldPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "${driver.carModel} (${driver.carPlate.toPersianDigits()})",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold)
                            )
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Phone,
                                contentDescription = null,
                                tint = Slate500,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = driver.phoneNumber.toPersianDigits(),
                                style = MaterialTheme.typography.bodySmall.copy(color = Slate700)
                            )
                        }
                    }

                    if (driver.description.isNotBlank()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = driver.description,
                            style = MaterialTheme.typography.labelSmall.copy(color = Slate500)
                        )
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    if (showAddDriverDialog) {
        AddDriverModal(
            onDismiss = { showAddDriverDialog = false },
            onConfirm = { newDriver ->
                onSaveDriver(newDriver)
                showAddDriverDialog = false
            }
        )
    }
}

@Composable
fun AddDriverModal(
    onDismiss: () -> Unit,
    onConfirm: (Driver) -> Unit
) {
    var fullName by remember { mutableStateOf("") }
    var driverCode by remember { mutableStateOf("D-${(106..199).random()}") }
    var personnelCode by remember { mutableStateOf("AE-${(84070..84999).random()}") }
    var phoneNumber by remember { mutableStateOf("") }
    var nationalId by remember { mutableStateOf("") }
    var carModel by remember { mutableStateOf("") }
    var carPlate by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

    val isValid = fullName.isNotBlank() && phoneNumber.isNotBlank() && carModel.isNotBlank()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "ثبت راننده جدید در ناوگان",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = NavyPrimary)
            )
        },
        text = {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    OutlinedTextField(
                        value = fullName,
                        onValueChange = { fullName = it },
                        label = { Text("نام و نام خانوادگی") },
                        modifier = Modifier.fillMaxWidth().testTag("modal_driver_name_input")
                    )
                }
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = driverCode,
                            onValueChange = { driverCode = it },
                            label = { Text("کد راننده") },
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = personnelCode,
                            onValueChange = { personnelCode = it },
                            label = { Text("کد پرسنلی") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                item {
                    OutlinedTextField(
                        value = phoneNumber,
                        onValueChange = { phoneNumber = it },
                        label = { Text("شماره همراه") },
                        placeholder = { Text("09123456789") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    OutlinedTextField(
                        value = carModel,
                        onValueChange = { carModel = it },
                        label = { Text("مدل و رنگ خودرو") },
                        placeholder = { Text("مثلاً: دنا پلاس سفید") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    OutlinedTextField(
                        value = carPlate,
                        onValueChange = { carPlate = it },
                        label = { Text("شماره پلاک") },
                        placeholder = { Text("مثلاً: ۴۵ ط ۶۱۲ ایران ۳۳") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("توضیحات و حوزه مأموریت") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (isValid) {
                        val d = Driver(
                            id = "drv-${System.currentTimeMillis()}",
                            userId = "usr-${System.currentTimeMillis()}",
                            fullName = fullName.trim(),
                            driverCode = driverCode.trim(),
                            personnelCode = personnelCode.trim(),
                            phoneNumber = phoneNumber.trim(),
                            nationalId = nationalId.trim(),
                            carModel = carModel.trim(),
                            carPlate = carPlate.trim(),
                            joinDateJalali = PersianDateHelper.getTodayJalali().formatStandard(),
                            isActive = true,
                            description = description.trim()
                        )
                        onConfirm(d)
                    }
                },
                enabled = isValid,
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
            ) {
                Text("ثبت و اضافه به ناوگان")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("انصراف")
            }
        }
    )
}
