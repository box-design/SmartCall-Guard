package com.smartcall.guard.ui.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Nightlight
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhoneForwarded
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.smartcall.guard.data.entity.BlockMode
import com.smartcall.guard.data.entity.LocationRule

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel(),
    navController: NavController
) {
    val settings by viewModel.settings.collectAsState()
    val importResult by viewModel.importResult.collectAsState()

    var showPrivacyDialog by remember { mutableStateOf(false) }
    var showBlockModeDialog by remember { mutableStateOf(false) }
    var showRepeatCallDialog by remember { mutableStateOf(false) }
    var showNightTimeDialog by remember { mutableStateOf(false) }
    var showImportExportDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("设置") })
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
        ) {
            val currentSettings = settings ?: return@Column

            SettingsSectionHeader("拦截模式")

            SettingsClickItem(
                icon = Icons.Filled.Shield,
                title = "拦截模式",
                subtitle = currentSettings.blockMode.displayName() + " - " + currentSettings.blockMode.displayDesc(),
                onClick = { showBlockModeDialog = true }
            )

            Divider(modifier = Modifier.padding(horizontal = 16.dp))
            Spacer(modifier = Modifier.height(8.dp))

            SettingsSectionHeader("拦截设置")

            SettingsSwitchItem(
                icon = Icons.Filled.Lock,
                title = "启用通讯录白名单",
                subtitle = "通讯录中的号码不会被拦截",
                checked = currentSettings.whitelistContacts,
                onCheckedChange = { viewModel.updateSettings(currentSettings.copy(whitelistContacts = it)) }
            )

            SettingsSwitchItem(
                icon = Icons.Filled.Lock,
                title = "拦截未知归属地",
                subtitle = "归属地库中找不到的号码将被拦截",
                checked = currentSettings.blockUnknownSegment,
                onCheckedChange = { viewModel.updateSettings(currentSettings.copy(blockUnknownSegment = it)) }
            )

            SettingsClickItem(
                icon = Icons.Filled.PhoneForwarded,
                title = "重复来电放行",
                subtitle = if (currentSettings.repeatCallPassEnabled)
                    "已开启，${currentSettings.repeatCallPassMinutes}分钟内重复来电自动放行"
                else
                    "未开启",
                onClick = { showRepeatCallDialog = true }
            )

            SettingsClickItem(
                icon = Icons.Filled.Folder,
                title = "号段库更新",
                subtitle = "检查更新",
                onClick = { }
            )

            Divider(modifier = Modifier.padding(horizontal = 16.dp))
            Spacer(modifier = Modifier.height(8.dp))

            SettingsSectionHeader("夜间模式")

            SettingsSwitchItem(
                icon = Icons.Filled.Nightlight,
                title = "启用夜间模式",
                subtitle = "在设定时段内仅允许白名单来电",
                checked = currentSettings.nightModeEnabled,
                onCheckedChange = { viewModel.updateSettings(currentSettings.copy(nightModeEnabled = it)) }
            )

            SettingsClickItem(
                icon = Icons.Filled.Nightlight,
                title = "夜间时段",
                subtitle = "${currentSettings.nightModeStart} - ${currentSettings.nightModeEnd}",
                onClick = { showNightTimeDialog = true }
            )

            Divider(modifier = Modifier.padding(horizontal = 16.dp))
            Spacer(modifier = Modifier.height(8.dp))

            SettingsSectionHeader("归属地规则")

            LocationRuleSelector(
                currentRule = currentSettings.locationRule,
                onRuleSelected = { viewModel.updateSettings(currentSettings.copy(locationRule = it)) }
            )

            Divider(modifier = Modifier.padding(horizontal = 16.dp))
            Spacer(modifier = Modifier.height(8.dp))

            SettingsSectionHeader("黑名单与白名单")

            SettingsClickItem(
                icon = Icons.Filled.Block,
                title = "黑名单管理",
                subtitle = "管理所有黑名单规则",
                onClick = { navController.navigate("blacklist_manage") }
            )

            SettingsClickItem(
                icon = Icons.Filled.List,
                title = "白名单管理",
                subtitle = "管理所有白名单规则",
                onClick = { navController.navigate("whitelist_manage") }
            )

            SettingsClickItem(
                icon = Icons.Filled.Person,
                title = "导入通讯录",
                subtitle = importResult?.let { "已导入 ${it.importedCount} 个" } ?: "将通讯录号码加入白名单",
                onClick = { viewModel.importContacts() }
            )

            SettingsClickItem(
                icon = Icons.Filled.Folder,
                title = "批量导入规则",
                subtitle = "从文件导入号码规则",
                onClick = { showImportExportDialog = true }
            )

            SettingsClickItem(
                icon = Icons.Filled.Folder,
                title = "导出规则",
                subtitle = "将规则导出为文件",
                onClick = { showImportExportDialog = true }
            )

            Divider(modifier = Modifier.padding(horizontal = 16.dp))
            Spacer(modifier = Modifier.height(8.dp))

            SettingsSectionHeader("关于")

            SettingsClickItem(
                icon = Icons.Filled.Info,
                title = "隐私政策",
                subtitle = "查看隐私政策",
                onClick = { showPrivacyDialog = true }
            )

            SettingsInfoItem(
                icon = Icons.Filled.Info,
                title = "版本",
                subtitle = "v1.1.0"
            )

            Spacer(modifier = Modifier.height(32.dp))
        }
    }

    if (showPrivacyDialog) {
        AlertDialog(
            onDismissRequest = { showPrivacyDialog = false },
            title = { Text("隐私政策") },
            text = {
                Column {
                    Text("智能来电守护重视您的隐私保护：")
                    Text("• 通讯录仅用于本地白名单，不上传服务器")
                    Text("• 定位仅用于本地归属地匹配，不上传服务器")
                    Text("• 拦截记录仅存储于本地")
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.acceptPrivacy()
                    showPrivacyDialog = false
                }) { Text("同意") }
            },
            dismissButton = {
                TextButton(onClick = { showPrivacyDialog = false }) { Text("关闭") }
            }
        )
    }

    if (showBlockModeDialog) {
        val currentSettings = settings
        if (currentSettings != null) {
            BlockModeSelectorDialog(
                currentMode = currentSettings.blockMode,
                onModeSelected = { mode ->
                    viewModel.updateSettings(currentSettings.copy(blockMode = mode))
                    showBlockModeDialog = false
                },
                onDismiss = { showBlockModeDialog = false }
            )
        }
    }

    if (showRepeatCallDialog) {
        val currentSettings = settings
        if (currentSettings != null) {
            RepeatCallPassDialog(
                enabled = currentSettings.repeatCallPassEnabled,
                minutes = currentSettings.repeatCallPassMinutes,
                onSave = { enabled, minutes ->
                    viewModel.updateSettings(currentSettings.copy(
                        repeatCallPassEnabled = enabled,
                        repeatCallPassMinutes = minutes
                    ))
                    showRepeatCallDialog = false
                },
                onDismiss = { showRepeatCallDialog = false }
            )
        }
    }

    if (showNightTimeDialog) {
        val currentSettings = settings
        if (currentSettings != null) {
            NightTimeDialog(
                startTime = currentSettings.nightModeStart,
                endTime = currentSettings.nightModeEnd,
                onSave = { start, end ->
                    viewModel.updateSettings(currentSettings.copy(
                        nightModeStart = start,
                        nightModeEnd = end
                    ))
                    showNightTimeDialog = false
                },
                onDismiss = { showNightTimeDialog = false }
            )
        }
    }

    if (showImportExportDialog) {
        com.smartcall.guard.ui.component.ImportExportDialog(
            onImport = { text, type ->
                showImportExportDialog = false
            },
            onExport = { types ->
                showImportExportDialog = false
            },
            onDismiss = { showImportExportDialog = false }
        )
    }
}

@Composable
fun BlockModeSelectorDialog(
    currentMode: BlockMode,
    onModeSelected: (BlockMode) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("拦截模式") },
        text = {
            Column {
                BlockMode.values().forEach { mode ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onModeSelected(mode) }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = mode.displayName(),
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (currentMode == mode) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = mode.displayDesc(),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                        if (currentMode == mode) {
                            Icon(
                                imageVector = Icons.Filled.KeyboardArrowRight,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("关闭") }
        }
    )
}

@Composable
fun RepeatCallPassDialog(
    enabled: Boolean,
    minutes: Int,
    onSave: (Boolean, Int) -> Unit,
    onDismiss: () -> Unit
) {
    var isEnabled by remember { mutableStateOf(enabled) }
    var mins by remember { mutableStateOf(minutes) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("重复来电放行") },
        text = {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("启用重复来电放行")
                        Text(
                            text = "同一号码短时间内再次来电时自动放行",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                    Switch(checked = isEnabled, onCheckedChange = { isEnabled = it })
                }
                Spacer(modifier = Modifier.height(16.dp))
                if (isEnabled) {
                    Text("放行时间间隔（分钟）")
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(onClick = { if (mins > 1) mins-- }) { Text("-") }
                        Text("$mins", style = MaterialTheme.typography.titleLarge)
                        TextButton(onClick = { if (mins < 30) mins++ }) { Text("+") }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onSave(isEnabled, mins) }) { Text("确定") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("取消") }
        }
    )
}

@Composable
fun NightTimeDialog(
    startTime: String,
    endTime: String,
    onSave: (String, String) -> Unit,
    onDismiss: () -> Unit
) {
    var start by remember { mutableStateOf(startTime) }
    var end by remember { mutableStateOf(endTime) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("夜间时段设置") },
        text = {
            Column {
                Text(
                    text = "在设定时段内，仅允许白名单来电",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = start,
                    onValueChange = { start = it },
                    label = { Text("开始时间") },
                    placeholder = { Text("如：22:00") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = end,
                    onValueChange = { end = it },
                    label = { Text("结束时间") },
                    placeholder = { Text("如：07:00") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onSave(start, end) }) { Text("确定") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("取消") }
        }
    )
}

@Composable
fun SettingsSectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 4.dp)
    )
}

@Composable
fun SettingsSwitchItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = MaterialTheme.typography.bodyMedium)
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
fun SettingsClickItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = MaterialTheme.typography.bodyMedium)
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
        Icon(
            imageVector = Icons.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
fun SettingsInfoItem(
    icon: ImageVector,
    title: String,
    subtitle: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = MaterialTheme.typography.bodyMedium)
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
    }
}

@Composable
fun LocationRuleSelector(
    currentRule: LocationRule,
    onRuleSelected: (LocationRule) -> Unit
) {
    val rules = listOf(
        LocationRule.OFF to "关闭",
        LocationRule.SAME_CITY to "仅同城",
        LocationRule.SAME_PROVINCE to "仅同省",
        LocationRule.BLOCK_OVERSEAS to "拦截海外"
    )

    Column {
        rules.forEach { (rule, label) ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onRuleSelected(rule) }
                    .padding(horizontal = 32.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = label,
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (currentRule == rule) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurface
                )
                if (currentRule == rule) {
                    Icon(
                        imageVector = Icons.Filled.KeyboardArrowRight,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}
