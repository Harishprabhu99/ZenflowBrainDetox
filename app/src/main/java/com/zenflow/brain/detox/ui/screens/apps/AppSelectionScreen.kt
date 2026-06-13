package com.zenflow.brain.detox.ui.screens.apps

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.zenflow.brain.detox.di.appViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.zenflow.brain.detox.domain.model.BlockedApp
import com.zenflow.brain.detox.ui.theme.PrimaryBlue
import com.zenflow.brain.detox.util.TimeFormatter

@Composable
fun AppSelectionScreen(
    onNavigateToTimer: (String) -> Unit,
    viewModel: AppSelectionViewModel = appViewModel { AppSelectionViewModel(it) },
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
    ) {
        Text(
            text = "Select Apps to Block",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = uiState.searchQuery,
            onValueChange = viewModel::onSearchQueryChanged,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Search apps...") },
            singleLine = true,
        )
        Spacer(modifier = Modifier.height(16.dp))
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(uiState.filteredApps, key = { it.packageName }) { app ->
                AppSelectionCard(
                    app = app,
                    onToggle = { viewModel.toggleApp(app.packageName, it) },
                    onConfigure = { onNavigateToTimer(app.packageName) },
                )
            }
        }
    }
}

@Composable
private fun AppSelectionCard(
    app: BlockedApp,
    onToggle: (Boolean) -> Unit,
    onConfigure: () -> Unit,
) {
    Card(
        onClick = onConfigure,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (app.isEnabled) {
                PrimaryBlue.copy(alpha = 0.08f)
            } else {
                MaterialTheme.colorScheme.surface
            },
        ),
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    app.displayName,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.weight(1f),
                )
                Switch(
                    checked = app.isEnabled,
                    onCheckedChange = onToggle,
                )
            }
            if (app.isEnabled) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "Limit: ${TimeFormatter.formatMinutes(app.dailyLimitMinutes)}/day",
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }
    }
}
