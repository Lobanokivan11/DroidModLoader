package com.shonkware.droidmodloader.engine.index

import com.shonkware.droidmodloader.engine.model.DeployScope

data class ModContentIndex(
    val modId: String,
    val modName: String,
    val entries: List<ModContentEntry>
) {
    val deployableFiles: List<ModContentEntry>
        get() = entries.filter { it.isDeployable }

    val dataFiles: List<ModContentEntry>
        get() = entries.filter {
            it.isDeployable && it.deployScope == DeployScope.DATA
        }

    val gameRootFiles: List<ModContentEntry>
        get() = entries.filter {
            it.isDeployable && it.deployScope == DeployScope.GAME_ROOT
        }

    val managerOnlyFiles: List<ModContentEntry>
        get() = entries.filter {
            it.deployScope == DeployScope.MANAGER_ONLY
        }

    val profileOnlyFiles: List<ModContentEntry>
        get() = entries.filter {
            it.deployScope == DeployScope.PROFILE_ONLY
        }

    val plugins: List<ModContentEntry>
        get() = entries.filter { it.category == ModContentCategory.PLUGIN }

    val archives: List<ModContentEntry>
        get() = entries.filter { it.category == ModContentCategory.ARCHIVE }

    val configs: List<ModContentEntry>
        get() = entries.filter { it.category == ModContentCategory.CONFIG }

    val setupOnlyFiles: List<ModContentEntry>
        get() = entries.filter { it.category == ModContentCategory.SETUP_ONLY }

    val documentationFiles: List<ModContentEntry>
        get() = entries.filter { it.category == ModContentCategory.DOCUMENTATION }

    val optionalModules: List<ModContentEntry>
        get() = entries.filter { it.category == ModContentCategory.OPTIONAL_MODULE || it.isOptionalCandidate }

    val ignoredFiles: List<ModContentEntry>
        get() = entries.filter { it.category == ModContentCategory.IGNORED }

    val unknownFiles: List<ModContentEntry>
        get() = entries.filter { it.category == ModContentCategory.UNKNOWN }

    val hasGameRootFiles: Boolean
        get() = gameRootFiles.isNotEmpty()
}