package com.shonkware.droidmodloader.ui.archive

enum class ArchiveLibraryItemStatus {
    AVAILABLE,
    INSTALLED,
    MISSING_FILE
}

data class ArchiveLibraryUiItem(
    val archiveId: String,
    val displayName: String,
    val fileName: String,
    val archiveFormat: String,
    val sizeBytes: Long,
    val createdAtMillis: Long,
    val status: ArchiveLibraryItemStatus,
    val installedModName: String? = null,
    val version: String? = null,
    val sourceLabel: String? = null
)
