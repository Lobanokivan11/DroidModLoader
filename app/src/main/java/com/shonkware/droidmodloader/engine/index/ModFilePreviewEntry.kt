package com.shonkware.droidmodloader.engine.index

data class ModFilePreviewEntry(
    val normalizedPath: String,
    val originalPath: String,
    val status: ModFilePreviewStatus,
    val reason: String,
    val winningModName: String? = null
)