package com.shonkware.droidmodloader.engine.model

data class DeploymentRecord(
    val normalizedPath: String,
    val winningModId: String,
    val sourceFilePath: String,
    val winningModName : String,
    val hash: String,
)