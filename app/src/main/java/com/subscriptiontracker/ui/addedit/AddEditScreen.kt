package com.subscriptiontracker.ui.addedit

import android.app.DatePickerDialog
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.subscriptiontracker.domain.model.BillingCycle
import com.subscriptiontracker.domain.model.Intention
import com.subscriptiontracker.domain.model.SubscriptionStatus
import com.subscriptiontracker.ui.components.PresetLogo
import com.subscriptiontracker.ui.components.presetLogos
import com.subscriptiontracker.ui.theme.CardWhite
import com.subscriptiontracker.ui.theme.Primary
import com.subscriptiontracker.ui.theme.TextMuted
import com.subscriptiontracker.ui.theme.TextSecondary
import com.subscriptiontracker.util.formatFull
import java.io.File
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditScreen(
    subscriptionId: Long = 0,
    onNavigateBack: () -> Unit,
    viewModel: AddEditViewModel = viewModel()
) {
    val state by viewModel.formState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(subscriptionId) {
        viewModel.loadSubscription(subscriptionId)
    }

    LaunchedEffect(state.saveSuccess) {
        if (state.saveSuccess) onNavigateBack()
    }

    val isEditing = subscriptionId != 0L

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEditing) "编辑订阅" else "新增订阅") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            OutlinedTextField(
                value = state.name,
                onValueChange = { viewModel.updateName(it) },
                label = { Text("订阅名称") },
                isError = state.nameError != null,
                supportingText = state.nameError?.let { { Text(it) } },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = state.amount,
                onValueChange = { viewModel.updateAmount(it) },
                label = { Text("金额") },
                isError = state.amountError != null,
                supportingText = state.amountError?.let { { Text(it) } },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            BillingCycleDropdown(
                selected = state.billingCycle,
                onSelected = { viewModel.updateBillingCycle(it) },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = state.startDateText,
                onValueChange = { viewModel.updateStartDateText(it) },
                label = { Text("开始日期") },
                isError = state.startDateError != null,
                supportingText = state.startDateError?.let { { Text(it) } },
                singleLine = true,
                trailingIcon = {
                    IconButton(onClick = {
                        DatePickerDialog(
                            context,
                            { _, year, month, dayOfMonth ->
                                viewModel.updateStartDate(LocalDate.of(year, month + 1, dayOfMonth))
                            },
                            state.startDate.year,
                            state.startDate.monthValue - 1,
                            state.startDate.dayOfMonth
                        ).show()
                    }) {
                        Icon(Icons.Default.CalendarMonth, contentDescription = "选择日期")
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 到期日期 (自动推算，可手动覆盖)
            val isOneTime = state.billingCycle == BillingCycle.ONE_TIME
            OutlinedTextField(
                value = state.deadlineDateText + if (state.deadlineOverridden) " (已手动修改)" else "",
                onValueChange = { viewModel.updateDeadlineDateText(it) },
                label = { Text("到期日期" + if (state.deadlineOverridden) " (手动)" else " (自动推算)") },
                isError = state.deadlineDateError != null,
                supportingText = state.deadlineDateError?.let { { Text(it) } },
                enabled = !isOneTime,
                singleLine = true,
                trailingIcon = {
                    if (!isOneTime) {
                        IconButton(onClick = {
                            DatePickerDialog(
                                context,
                                { _, year, month, dayOfMonth ->
                                    viewModel.updateDeadlineDate(LocalDate.of(year, month + 1, dayOfMonth))
                                },
                                state.deadlineDate.year,
                                state.deadlineDate.monthValue - 1,
                                state.deadlineDate.dayOfMonth
                            ).show()
                        }) {
                            Icon(Icons.Default.CalendarMonth, contentDescription = "选择日期")
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            StatusDropdown(
                selected = state.status,
                onSelected = { viewModel.updateStatus(it) },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = state.autoRenew,
                    onCheckedChange = { viewModel.updateAutoRenew(it) }
                )
                Text("自动续费（开启后为连续包月/季/年）")
            }

            Spacer(modifier = Modifier.height(12.dp))

            IntentionDropdown(
                selected = state.intention,
                onSelected = { viewModel.updateIntention(it) },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(20.dp))

            LogoPickerSection(
                selectedUri = state.logoUri,
                onPresetSelected = { viewModel.onPresetSelected(it) },
                onCustomImage = { viewModel.updateLogoUri(it) },
                onClear = { viewModel.updateLogoUri(null); viewModel.updateCategory("") }
            )

            if (state.logoUri == "preset:other") {
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = state.customCategory,
                    onValueChange = { viewModel.updateCustomCategory(it) },
                    label = { Text("自定义分类名称") },
                    isError = state.categoryError != null,
                    supportingText = state.categoryError?.let { { Text(it) } },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            WallpaperPickerSection(
                wallpaperUri = state.wallpaperUri,
                onSelect = { viewModel.updateWallpaperUri(it) },
                onClear = { viewModel.updateWallpaperUri(null) }
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = state.notes,
                onValueChange = { viewModel.updateNotes(it) },
                label = { Text("备注（可选）") },
                maxLines = 3,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = { viewModel.save() },
                enabled = !state.isSaving,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (isEditing) "保存修改" else "添加订阅")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BillingCycleDropdown(
    selected: BillingCycle,
    onSelected: (BillingCycle) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = selected.displayName,
            onValueChange = {},
            readOnly = true,
            label = { Text("续费周期") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            BillingCycle.entries.forEach { cycle ->
                DropdownMenuItem(
                    text = { Text(cycle.displayName) },
                    onClick = {
                        onSelected(cycle)
                        expanded = false
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StatusDropdown(
    selected: SubscriptionStatus,
    onSelected: (SubscriptionStatus) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = selected.displayName,
            onValueChange = {},
            readOnly = true,
            label = { Text("状态") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            SubscriptionStatus.entries.forEach { status ->
                DropdownMenuItem(
                    text = { Text(status.displayName) },
                    onClick = {
                        onSelected(status)
                        expanded = false
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun IntentionDropdown(
    selected: Intention,
    onSelected: (Intention) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = selected.displayName,
            onValueChange = {},
            readOnly = true,
            label = { Text("意向") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            Intention.entries.forEach { intention ->
                DropdownMenuItem(
                    text = { Text(intention.displayName) },
                    onClick = {
                        onSelected(intention)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
private fun LogoPickerSection(
    selectedUri: String?,
    onPresetSelected: (PresetLogo) -> Unit,
    onCustomImage: (String) -> Unit,
    onClear: () -> Unit
) {
    val context = LocalContext.current
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { contentUri ->
            val localPath = copyToInternalStorage(context, contentUri, "logo")
            if (localPath != null) onCustomImage(localPath)
        }
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "订阅图标",
            style = MaterialTheme.typography.titleSmall,
            color = TextSecondary,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(presetLogos) { preset ->
                val isSelected = selectedUri == "preset:${preset.id}"
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .then(
                            if (isSelected) Modifier.border(2.dp, Primary, RoundedCornerShape(12.dp))
                            else Modifier
                        )
                        .clickable { onPresetSelected(preset) }
                        .padding(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(preset.bgColor),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = preset.icon,
                            fontSize = 20.sp,
                            color = Color.White
                        )
                    }
                    Text(
                        text = preset.label,
                        fontSize = 11.sp,
                        color = TextMuted,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }

            item {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .border(1.dp, TextMuted.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                        .clickable { imagePickerLauncher.launch("image/*") }
                        .padding(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(CardWhite),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "选择图片",
                            tint = TextMuted,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Text(
                        text = "自定义",
                        fontSize = 11.sp,
                        color = TextMuted,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }

        // Preview of selected custom image
        if (selectedUri != null && !selectedUri.startsWith("preset:")) {
            Row(
                modifier = Modifier
                    .padding(top = 8.dp)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(CardWhite)
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(if (selectedUri.startsWith("content://")) selectedUri else File(selectedUri))
                        .build(),
                    contentDescription = "已选图标",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(8.dp))
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "已选择自定义图标",
                    fontSize = 13.sp,
                    color = TextSecondary,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = onClear, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Default.Close, contentDescription = "清除", tint = TextMuted, modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}

@Composable
private fun WallpaperPickerSection(
    wallpaperUri: String?,
    onSelect: (String) -> Unit,
    onClear: () -> Unit
) {
    val context = LocalContext.current
    val wallpaperPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { contentUri ->
            val localPath = copyToInternalStorage(context, contentUri, "wallpaper")
            if (localPath != null) onSelect(localPath)
        }
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "详情页壁纸",
            style = MaterialTheme.typography.titleSmall,
            color = TextSecondary,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        if (wallpaperUri != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .clip(RoundedCornerShape(12.dp))
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(if (wallpaperUri.startsWith("content://")) wallpaperUri else File(wallpaperUri))
                        .build(),
                    contentDescription = "壁纸预览",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxWidth().height(100.dp)
                )
                IconButton(
                    onClick = {
                        // Clean up local file
                        val file = File(wallpaperUri)
                        if (file.exists()) file.delete()
                        onClear()
                    },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(4.dp)
                        .size(24.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.Black.copy(alpha = 0.5f))
                ) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "清除壁纸",
                        tint = Color.White,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        } else {
            OutlinedButton(
                onClick = { wallpaperPickerLauncher.launch("image/*") },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Image, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("选择壁纸图片")
            }
        }
    }
}

private fun copyToInternalStorage(context: android.content.Context, contentUri: Uri, prefix: String): String? {
    return try {
        val dir = File(context.filesDir, "images")
        if (!dir.exists()) dir.mkdirs()
        val file = File(dir, "${prefix}_${System.currentTimeMillis()}.jpg")
        context.contentResolver.openInputStream(contentUri)?.use { input ->
            file.outputStream().use { output ->
                input.copyTo(output)
            }
        }
        file.absolutePath
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}
