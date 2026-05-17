package com.shonkware.droidmodloader.engine.model

data class DeploymentRecord(
    val normalizedPath: String,
    val winningModId: String,
    val sourceFilePath: String,
    val winningModName: String,
    val hash: String,

    val hadPreExistingTargetFile: Boolean = false,
    val backupFilePath: String? = null,
    val backupCreatedAtEpochMillis: Long? = null
)