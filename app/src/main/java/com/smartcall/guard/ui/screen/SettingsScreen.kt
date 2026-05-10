package com.smartcall.guard.ui.screen

import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import com.smartcall.guard.data.entity.LocationRule

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(viewModel: SettingsViewModel = hiltViewModel()) {
    val settings by viewModel.settings.collectAsState()
    val importResult by viewModel.importResult.collectAsState()
    val privacyConsent by viewModel.privacyConsent.collectAsState()

    var showPrivacyDialog by remember { mutableStateOf(false) }

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
            SettingsSectionHeader("拦截设置")

            val currentSettings = settings ?: return@Column

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

            Divider(modifier = Modifier.padding(horizontal = 16.dp))
            Spacer(modifier = Modifier.height(8.dp))

            SettingsSectionHeader("归属地规则")

            LocationRuleSelector(
                currentRule = currentSettings.locationRule,
                onRuleSelected = { viewModel.updateSettings(currentSettings.copy(locationRule = it)) }
            )

            Divider(modifier = Modifier.padding(horizontal = 16.dp))
            Spacer(modifier = Modifier.height(8.dp))

            SettingsSectionHeader("数据管理")

            SettingsClickItem(
                icon = Icons.Filled.Person,
                title = "导入通讯录",
                subtitle = importResult?.let { "已导入 ${it.importedCount} 个" } ?: "将通讯录号码加入白名单",
                onClick = { viewModel.importContacts() }
            )

            SettingsClickItem(
                icon = Icons.Filled.Folder,
                title = "号段库更新",
                subtitle = "检查更新",
                onClick = { }
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
                subtitle = "v1.0.0"
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
