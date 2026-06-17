package com.shonkware.droidmodloader.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.shonkware.droidmodloader.ui.archive.ArchiveLibraryItemStatus
import com.shonkware.droidmodloader.ui.archive.ArchiveLibraryUiItem
import com.shonkware.droidmodloader.ui.theme.DmlColors
import com.shonkware.droidmodloader.ui.theme.DmlDefaults
import java.text.DateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ArchiveInstallSourceDialog(
    onChooseFromDevice: () -> Unit,
    onOpenArchiveLibrary: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Install Mod")
        },
        text = {
            Text(
                "Choose an archive from your device, or install one that Droid Mod Loader already saved."
            )
        },
        confirmButton = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Button(
                    onClick = onChooseFromDevice,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Choose Archive From Device")
                }

                Button(
                    onClick = onOpenArchiveLibrary,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Install From Archive Library")
                }

                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Cancel")
                }
            }
        }
    )
}

@Composable
fun ArchiveLibraryPanelDialog(
    items: List<ArchiveLibraryUiItem>,
    message: String,
    operationInProgress: Boolean,
    onInstallArchive: (String) -> Unit,
    onClose: () -> Unit
) {
    var searchText by remember { mutableStateOf("") }
    val filteredItems = remember(items, searchText) {
        val query = searchText.trim()
        if (query.isBlank()) {
            items
        } else {
            items.filter { item ->
                item.displayName.contains(query, ignoreCase = true) ||
                    item.fileName.contains(query, ignoreCase = true) ||
                    item.sourceLabel?.contains(query, ignoreCase = true) == true
            }
        }
    }

    Dialog(
        onDismissRequest = onClose,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.97f)
                .fillMaxHeight(0.94f)
                .padding(8.dp),
            colors = DmlDefaults.panelCardColors(),
            border = BorderStroke(1.dp, DmlColors.BorderDim)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Archive Library",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = message,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                    Button(onClick = onClose) {
                        Text("Close")
                    }
                }

                OutlinedTextField(
                    value = searchText,
                    onValueChange = { searchText = it },
                    label = { Text("Search saved archives") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                if (filteredItems.isEmpty()) {
                    Text(
                        text = if (items.isEmpty()) {
                            "No archives are available."
                        } else {
                            "No saved archives match your search."
                        },
                        style = MaterialTheme.typography.bodyMedium
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(
                            items = filteredItems,
                            key = { item -> item.archiveId }
                        ) { item ->
                            ArchiveLibraryRow(
                                item = item,
                                operationInProgress = operationInProgress,
                                onInstallArchive = onInstallArchive
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ArchiveLibraryRow(
    item: ArchiveLibraryUiItem,
    operationInProgress: Boolean,
    onInstallArchive: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = DmlDefaults.panelCardColors(),
        border = BorderStroke(1.dp, DmlColors.BorderDim)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Text(
                    text = item.displayName,
                    fontWeight = FontWeight.Bold
                )

                if (item.fileName != item.displayName) {
                    Text(
                        text = item.fileName,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                Text(
                    text = buildArchiveDetails(item),
                    style = MaterialTheme.typography.bodySmall
                )

                item.version?.takeIf { it.isNotBlank() }?.let { version ->
                    Text(
                        text = "Version: $version",
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                item.sourceLabel?.let { source ->
                    Text(
                        text = source,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                Text(
                    text = buildArchiveStatusText(item),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }

            if (item.status == ArchiveLibraryItemStatus.AVAILABLE) {
                Button(
                    onClick = { onInstallArchive(item.archiveId) },
                    enabled = !operationInProgress
                ) {
                    Text("Install")
                }
            }
        }
    }
}

private fun buildArchiveDetails(item: ArchiveLibraryUiItem): String {
    val format = item.archiveFormat.ifBlank { "archive" }.uppercase(Locale.getDefault())
    val size = formatArchiveSize(item.sizeBytes)
    val imported = DateFormat.getDateInstance(DateFormat.MEDIUM)
        .format(Date(item.createdAtMillis))
    return "$format • $size • Imported $imported"
}

private fun buildArchiveStatusText(item: ArchiveLibraryUiItem): String {
    return when (item.status) {
        ArchiveLibraryItemStatus.AVAILABLE -> "Available to install"
        ArchiveLibraryItemStatus.INSTALLED -> {
            "Installed as: ${item.installedModName ?: "installed mod"}"
        }
        ArchiveLibraryItemStatus.MISSING_FILE -> "Archive file is missing"
    }
}

internal fun formatArchiveSize(sizeBytes: Long): String {
    if (sizeBytes < 1024L) return "$sizeBytes B"

    val kib = sizeBytes / 1024.0
    if (kib < 1024.0) return String.format(Locale.US, "%.1f KB", kib)

    val mib = kib / 1024.0
    if (mib < 1024.0) return String.format(Locale.US, "%.1f MB", mib)

    return String.format(Locale.US, "%.1f GB", mib / 1024.0)
}
