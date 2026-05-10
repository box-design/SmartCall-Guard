package com.smartcall.guard.ui.screen

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DockedSearchBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.smartcall.guard.data.entity.RuleEntity
import com.smartcall.guard.data.entity.RuleType
import com.smartcall.guard.ui.component.ConfirmDeleteDialog
import com.smartcall.guard.ui.component.SimpleEmptyState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RulesScreen(viewModel: RulesViewModel = hiltViewModel()) {
    val rules by viewModel.rules.collectAsState()
    val filteredRules by viewModel.filteredRules.collectAsState()
    val selectedFilter by viewModel.selectedFilter.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    var searchQuery by remember { mutableStateOf("") }
    var showAddDialog by remember { mutableStateOf(false) }
    var editingRule by remember { mutableStateOf<RuleEntity?>(null) }
    var deleteConfirmRule by remember { mutableStateOf<RuleEntity?>(null) }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
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
                placeholder = { Text("搜索规则...") },
                modifier = Modifier.fillMaxWidth()
            ) {}

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = selectedFilter == null,
                    onClick = { viewModel.filterByType(null) },
                    label = { Text("全部") }
                )
                FilterChip(
                    selected = selectedFilter == RuleType.BLACKLIST_EXACT,
                    onClick = { viewModel.filterByType(RuleType.BLACKLIST_EXACT) },
                    label = { Text("黑名单") }
                )
                FilterChip(
                    selected = selectedFilter == RuleType.WHITELIST,
                    onClick = { viewModel.filterByType(RuleType.WHITELIST) },
                    label = { Text("白名单") }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (filteredRules.isEmpty()) {
                SimpleEmptyState(
                    title = "暂无规则",
                    description = "点击下方按钮添加拦截规则",
                    actionLabel = "添加规则",
                    onAction = { showAddDialog = true },
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filteredRules, key = { it.id }) { rule ->
                        RuleItem(
                            rule = rule,
                            onEdit = { editingRule = it },
                            onDelete = { deleteConfirmRule = it }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            FilledTonalButton(
                onClick = { showAddDialog = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("添加规则")
            }
        }
    }

    if (showAddDialog || editingRule != null) {
        AddEditRuleDialog(
            rule = editingRule,
            onDismiss = {
                showAddDialog = false
                editingRule = null
            },
            onSave = { type, value, note ->
                viewModel.saveRule(type, value, note, editingRule?.id)
                showAddDialog = false
                editingRule = null
            }
        )
    }

    if (deleteConfirmRule != null) {
        ConfirmDeleteDialog(
            title = "删除规则",
            message = "确定要删除规则 \"${deleteConfirmRule!!.value}\" 吗？",
            onConfirm = {
                viewModel.deleteRule(deleteConfirmRule!!.id)
                deleteConfirmRule = null
            },
            onDismiss = { deleteConfirmRule = null }
        )
    }
}

@Composable
fun RuleItem(
    rule: RuleEntity,
    onEdit: (RuleEntity) -> Unit,
    onDelete: (RuleEntity) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Filled.Phone,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = rule.value,
                    style = MaterialTheme.typography.bodyMedium
                )
                if (!rule.note.isNullOrBlank()) {
                    Text(
                        text = rule.note!!,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }
            IconButton(onClick = { onDelete(rule) }) {
                Icon(
                    imageVector = Icons.Filled.Delete,
                    contentDescription = "删除",
                    tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditRuleDialog(
    rule: RuleEntity?,
    onDismiss: () -> Unit,
    onSave: (RuleType, String, String?) -> Unit
) {
    var selectedType by remember { mutableStateOf(rule?.type ?: RuleType.BLACKLIST_EXACT) }
    var phoneNumber by remember { mutableStateOf(rule?.value ?: "") }
    var note by remember { mutableStateOf(rule?.note ?: "") }

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
            Text(
                text = if (rule != null) "编辑规则" else "添加规则",
                style = MaterialTheme.typography.titleLarge
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = selectedType == RuleType.BLACKLIST_EXACT,
                    onClick = { selectedType = RuleType.BLACKLIST_EXACT },
                    label = { Text("黑名单") }
                )
                FilterChip(
                    selected = selectedType == RuleType.WHITELIST,
                    onClick = { selectedType = RuleType.WHITELIST },
                    label = { Text("白名单") }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = phoneNumber,
                onValueChange = { phoneNumber = it },
                label = { Text("号码") },
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
                    if (phoneNumber.isNotBlank()) {
                        onSave(selectedType, phoneNumber, note.ifBlank { null })
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = phoneNumber.isNotBlank()
            ) {
                Text(if (rule != null) "保存" else "添加")
            }
        }
    }
}
