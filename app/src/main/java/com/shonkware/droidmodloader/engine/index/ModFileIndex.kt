package com.shonkware.droidmodloader.engine.index

data class ModFileIndexEntry(
    val originalPath: String,
    val normalizedPath: String,
    val hash: String,
    val sizeBytes: Long,
    val modifiedEpochMillis: Long
)

data class ModFileIndexSnapshot(
    val schemaVersion: Int,
    val modId: String,
    val modName: String,
    val modRootPath: String,
    val createdAtEpochMillis: Long,
    val entries: List<ModFileIndexEntry>
)