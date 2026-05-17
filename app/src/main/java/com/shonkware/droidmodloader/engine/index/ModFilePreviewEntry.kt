package com.shonkware.droidmodloader.engine.index

import com.shonkware.droidmodloader.engine.model.DeployScope

data class ModFilePreviewEntry(
    val normalizedPath: String,
    val originalPath: String,
    val status: ModFilePreviewStatus,
    val reason: String,
    val deployScope: DeployScope,
    val isDeployable: Boolean,
    val winningModName: String? = null
)