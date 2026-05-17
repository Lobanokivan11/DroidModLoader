package com.shonkware.droidmodloader.engine.index

import com.shonkware.droidmodloader.engine.model.DeployScope

data class ModFilePreview(
    val modId: String,
    val modName: String,
    val entries: List<ModFilePreviewEntry>,
    val folderSummaries: List<ModFileFolderSummary> = emptyList()
) {
    val winningFiles: List<ModFilePreviewEntry>
        get() = entries.filter { it.status == ModFilePreviewStatus.WINNING }

    val overwrittenFiles: List<ModFilePreviewEntry>
        get() = entries.filter { it.status == ModFilePreviewStatus.OVERWRITTEN }

    val dataFiles: List<ModFilePreviewEntry>
        get() = entries.filter {
            it.isDeployable && it.deployScope == DeployScope.DATA
        }

    val gameRootFiles: List<ModFilePreviewEntry>
        get() = entries.filter {
            it.isDeployable && it.deployScope == DeployScope.GAME_ROOT
        }

    val pluginFiles: List<ModFilePreviewEntry>
        get() = entries.filter { it.status == ModFilePreviewStatus.PLUGIN }

    val archiveFiles: List<ModFilePreviewEntry>
        get() = entries.filter { it.status == ModFilePreviewStatus.ARCHIVE }

    val configFiles: List<ModFilePreviewEntry>
        get() = entries.filter { it.status == ModFilePreviewStatus.CONFIG }

    val setupFiles: List<ModFilePreviewEntry>
        get() = entries.filter { it.status == ModFilePreviewStatus.SETUP_ONLY }

    val documentationFiles: List<ModFilePreviewEntry>
        get() = entries.filter { it.status == ModFilePreviewStatus.DOCUMENTATION }

    val optionalFiles: List<ModFilePreviewEntry>
        get() = entries.filter { it.status == ModFilePreviewStatus.OPTIONAL }

    val ignoredFiles: List<ModFilePreviewEntry>
        get() = entries.filter { it.status == ModFilePreviewStatus.IGNORED }

    val unknownFiles: List<ModFilePreviewEntry>
        get() = entries.filter { it.status == ModFilePreviewStatus.UNKNOWN }
}