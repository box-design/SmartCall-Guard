package com.smartcall.guard.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DockedSearchBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.smartcall.guard.data.entity.RuleEntity
import com.smartcall.guard.data.entity.RuleType

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun BlacklistManageScreen(
    viewModel: BlacklistManageViewModel = hiltViewModel(),
    onBack: () -> Unit
) {
    val rules by viewModel.rules.collectAsState()
    val selectedIds by viewModel.selectedIds.collectAsState()
    val typeFilter by viewModel.typeFilter.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var showAddDialog by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    val filteredRules = viewModel.getFilteredRules()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("黑名单管理") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    if (selectedIds.isNotEmpty()) {
                        IconButton(onClick = { showDeleteConfirm = true }) {
                            Icon(Icons.Filled.Delete, contentDescription = "删除选中")
                        }
                    }
                    IconButton(onClick = { viewModel.selectAll() }) {
                        Icon(Icons.Filled.SelectAll, contentDescription = "全选")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            DockedSearchBar(
                query = searchQuery,
                onQueryChange = {
                    searchQuery = it
                    viewModel.search(it)
                },
                onSearch = { viewModel.search(it) },
                active = false,
                onActiveChange = {},
                placeholder = { Text("搜索黑名单...") },
                modifier = Modifier.fillMaxWidth()
            ) {}

            Spacer(modifier = Modifier.height(8.dp))

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                FilterChip(
                    selected = typeFilter == null,
                    onClick = { viewModel.filterByType(null) },
                    label = { Text("全部 (${rules.size})") }
                )
                RuleType.values().filter { it.isBlacklist() }.forEach { type ->
                    FilterChip(
                        selected = typeFilter == type,
                        onClick = { viewModel.filterByType(type) },
                        label = { Text("${type.displayName()} (${viewModel.getRuleCountByType(type)})") }
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (filteredRules.isEmpty()) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("暂无黑名单规则", style = MaterialTheme.typography.bodyLarge)
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(filteredRules, key = { it.id }) { rule ->
                        BlacklistRuleItem(
                            rule = rule,
                            isSelected = rule.id in selectedIds,
                            onSelect = { viewModel.toggleSelection(rule.id) },
                            onToggleActive = { viewModel.toggleActive(rule) },
                            onDelete = { viewModel.deleteRule(rule.id) }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (selectedIds.isNotEmpty()) {
                    FilledTonalButton(
                        onClick = { showDeleteConfirm = true },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("删除选中 (${selectedIds.size})")
                    }
                    FilledTonalButton(
                        onClick = { viewModel.clearSelection() },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("取消选择")
                    }
                } else {
                    FilledTonalButton(
                        onClick = { showAddDialog = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("添加黑名单规则")
                    }
                }
            }
        }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("批量删除") },
            text = { Text("确定要删除选中的 ${selectedIds.size} 条规则吗？") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteSelected()
                    showDeleteConfirm = false
                }) { Text("删除") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) { Text("取消") }
            }
        )
    }

    if (showAddDialog) {
        AddBlacklistRuleDialog(
            onDismiss = { showAddDialog = false },
            onSave = { type, value, note, tag ->
                viewModel.saveRule(type, value, note, tag)
                showAddDialog = false
            }
        )
    }
}

@Composable
fun BlacklistRuleItem(
    rule: RuleEntity,
    isSelected: Boolean,
    onSelect: () -> Unit,
    onToggleActive: (RuleEntity) -> Unit,
    onDelete: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = if (rule.isActive)
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            else
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = isSelected,
                onCheckedChange = { onSelect() }
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = rule.value,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                    AssistChip(
                        onClick = {},
                        label = {
                            Text(
                                text = rule.type.displayName(),
                                style = MaterialTheme.typography.labelSmall
                            )
                        },
                        modifier = Modifier.height(24.dp)
                    )
                }
                if (!rule.tag.isNullOrBlank() || !rule.note.isNullOrBlank()) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (!rule.tag.isNullOrBlank()) {
                            Text(
                                text = rule.tag!!,
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        if (!rule.note.isNullOrBlank()) {
                            Text(
                                text = rule.note!!,
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                    }
                }
            }
            Switch(
                checked = rule.isActive,
                onCheckedChange = { onToggleActive(rule) }
            )
            IconButton(onClick = { onDelete(rule.id) }) {
                Icon(
                    Icons.Filled.Delete,
                    contentDescription = "删除",
                    tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AddBlacklistRuleDialog(
    onDismiss: () -> Unit,
    onSave: (RuleType, String, String?, String?) -> Unit
) {
    var selectedType by remember { mutableStateOf(RuleType.BLACKLIST_EXACT) }
    var value by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var tag by remember { mutableStateOf("") }

    androidx.compose.material3.ModalBottomSheet(
        onDismissRequest = onDismiss,
        shape = com.smartcall.guard.ui.theme.BottomSheetShape
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
                .padding(bottom = 32.dp)
        ) {
            Text("添加黑名单规则", style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(16.dp))

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                RuleType.values().filter { it.isBlacklist() }.forEach { type ->
                    FilterChip(
                        selected = selectedType == type,
                        onClick = { selectedType = type },
                        label = { Text(type.displayName()) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = value,
                onValueChange = { value = it },
                label = { Text(selectedType.displayName()) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = tag,
                onValueChange = { tag = it },
                label = { Text("标签（可选）") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                label = { Text("备注（可选）") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(24.dp))

            FilledTonalButton(
                onClick = {
                    if (value.isNotBlank()) {
                        onSave(selectedType, value, note.ifBlank { null }, tag.ifBlank { null })
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = value.isNotBlank()
            ) { Text("添加") }
        }
    }
}
