package com.smartcall.guard.ui.screen

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.smartcall.guard.data.dao.NumberStat
import com.smartcall.guard.data.dao.ReasonStat
import com.smartcall.guard.data.entity.BlockReason

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BlockStatsScreen(
    viewModel: BlockStatsViewModel = hiltViewModel(),
    onBack: () -> Unit
) {
    val todayCount by viewModel.todayCount.collectAsState()
    val weekCount by viewModel.weekCount.collectAsState()
    val monthCount by viewModel.monthCount.collectAsState()
    val reasonStats by viewModel.reasonStats.collectAsState()
    val topNumbers by viewModel.topNumbers.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("拦截统计") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    StatCard(title = "今日", count = todayCount, modifier = Modifier.weight(1f))
                    Spacer(modifier = Modifier.width(12.dp))
                    StatCard(title = "本周", count = weekCount, modifier = Modifier.weight(1f))
                    Spacer(modifier = Modifier.width(12.dp))
                    StatCard(title = "本月", count = monthCount, modifier = Modifier.weight(1f))
                }
            }

            item {
                Text(
                    text = "拦截原因分布",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                if (reasonStats.isEmpty()) {
                    Text(
                        text = "暂无数据",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                } else {
                    val maxCount = reasonStats.maxOf { it.count }
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        reasonStats.forEach { stat ->
                            ReasonStatItem(
                                stat = stat,
                                maxCount = maxCount
                            )
                        }
                    }
                }
            }

            item {
                Text(
                    text = "最常拦截号码 TOP 10",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                if (topNumbers.isEmpty()) {
                    Text(
                        text = "暂无数据",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        topNumbers.forEachIndexed { index, stat ->
                            TopNumberItem(rank = index + 1, stat = stat)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StatCard(title: String, count: Int, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
            )
            Text(
                text = "$count",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

@Composable
fun ReasonStatItem(stat: ReasonStat, maxCount: Int) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = stat.blockReason.displayName(),
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "${stat.count}",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = if (maxCount > 0) stat.count.toFloat() / maxCount else 0f,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
fun TopNumberItem(rank: Int, stat: NumberStat) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "#$rank",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.width(32.dp)
            )
            Text(
                text = stat.phoneNumber,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = "${stat.count}次",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
    }
}
