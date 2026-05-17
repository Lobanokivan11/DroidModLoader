package com.shonkware.droidmodloader.engine.index

import com.shonkware.droidmodloader.engine.model.DeployScope

data class ModContentEntry(
    val originalPath: String,
    val normalizedPath: String,
    val category: ModContentCategory,
    val reason: String,
    val isDeployable: Boolean,
    val deployScope: DeployScope,
    val isOptionalCandidate: Boolean = false
)