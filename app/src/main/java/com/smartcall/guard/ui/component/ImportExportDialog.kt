package com.smartcall.guard.ui.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.smartcall.guard.data.entity.RuleType

@Composable
fun ImportExportDialog(
    onImport: (String, RuleType) -> Unit,
    onExport: (List<RuleType>?) -> Unit,
    onDismiss: () -> Unit
) {
    var mode by remember { mutableStateOf("import") }
    var importText by remember { mutableStateOf("") }
    var importType by remember { mutableStateOf(RuleType.BLACKLIST_EXACT) }
    var exportAll by remember { mutableStateOf(true) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row {
                FilterChip(
                    selected = mode == "import",
                    onClick = { mode = "import" },
                    label = { Text("导入") }
                )
                FilterChip(
                    selected = mode == "export",
                    onClick = { mode = "export" },
                    label = { Text("导出") }
                )
            }
        },
        text = {
            if (mode == "import") {
                Column {
                    Text(
                        text = "选择导入的规则类型",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row {
                        RuleType.values().forEach { type ->
                            FilterChip(
                                selected = importType == type,
                                onClick = { importType = type },
                                label = { Text(type.displayName()) }
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = importText,
                        onValueChange = { importText = it },
                        label = { Text("粘贴内容") },
                        placeholder = { Text("每行一个号码，或粘贴JSON格式") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(160.dp),
                        maxLines = 10
                    )
                }
            } else {
                Column {
                    Text(
                        text = "选择要导出的规则",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    FilterChip(
                        selected = exportAll,
                        onClick = { exportAll = true },
                        label = { Text("全部规则") }
                    )
                    FilterChip(
                        selected = !exportAll,
                        onClick = { exportAll = false },
                        label = { Text("仅黑名单") }
                    )
                }
            }
        },
        confirmButton = {
            if (mode == "import") {
                TextButton(
                    onClick = { onImport(importText, importType) },
                    enabled = importText.isNotBlank()
                ) { Text("导入") }
            } else {
                TextButton(
                    onClick = {
                        onExport(if (exportAll) null else listOf(
                            RuleType.BLACKLIST_EXACT,
                            RuleType.BLACKLIST_PREFIX,
                            RuleType.BLACKLIST_REGEX,
                            RuleType.BLACKLIST_SEGMENT
                        ))
                    }
                ) { Text("导出") }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("取消") }
        }
    )
}
