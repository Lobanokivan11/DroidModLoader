package com.shonkware.droidmodloader.engine.index

data class ModFilePreview(
    val modId: String,
    val modName: String,
    val entries: List<ModFilePreviewEntry>
) {
    val winningFiles: List<ModFilePreviewEntry>
        get() = entries.filter { it.status == ModFilePreviewStatus.WINNING }

    val overwrittenFiles: List<ModFilePreviewEntry>
        get() = entries.filter { it.status == ModFilePreviewStatus.OVERWRITTEN }

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

    val unknownFiles: List<ModFilePreviewEntry>
        get() = entries.filter { it.status == ModFilePreviewStatus.UNKNOWN }
}