package com.shonkware.droidmodloader.engine.plugins

import com.shonkware.droidmodloader.engine.model.Mod
import com.shonkware.droidmodloader.engine.model.PluginEntry
import com.shonkware.droidmodloader.engine.util.PathUtils
import java.io.File

class ManagedPluginScanner {

    fun discoverPluginsFromEnabledMods(mods: List<Mod>): List<PluginEntry> {
        val enabledMods = mods
            .filter { it.enabled }
            .sortedBy { it.priority }

        val winningPlugins = linkedMapOf<String, PluginEntry>()

        for (mod in enabledMods) {
            val modRoot = File(mod.installPath)

            if (!modRoot.exists() || !modRoot.isDirectory) {
                continue
            }

            val pluginPaths = findPluginPaths(modRoot)

            for (normalizedPath in pluginPaths) {
                val pluginName = normalizedPath.substringAfterLast("/")

                winningPlugins[normalizedPath] = PluginEntry(
                    normalizedPath = normalizedPath,
                    pluginName = pluginName,
                    sourceModId = mod.id,
                    sourceModName = mod.name,
                    enabled = true,
                    priority = Int.MAX_VALUE,
                    pluginType = detectPluginType(pluginName),
                    sourceType = "managed_mod",
                    locked = false,
                    filePresentInDataFolder = false
                )
            }
        }

        return winningPlugins.values
            .sortedBy { it.normalizedPath.lowercase() }
            .mapIndexed { index, plugin ->
                plugin.copy(priority = index + 1)
            }
    }

    private fun findPluginPaths(modRoot: File): List<String> {
        val results = mutableListOf<String>()

        modRoot.walkTopDown()
            .onEnter { dir ->
                val name = dir.name.lowercase()
                name != "__macosx" && !name.startsWith(".")
            }
            .forEach { file ->
                if (!file.isFile) {
                    return@forEach
                }

                val relativePath = file.relativeTo(modRoot).path
                val normalizedPath = PathUtils.normalize(relativePath) ?: return@forEach

                if (isPluginPath(normalizedPath)) {
                    results.add(normalizedPath)
                }
            }

        return results
    }

    private fun isPluginPath(normalizedPath: String): Boolean {
        val lower = normalizedPath.lowercase()
        return lower.endsWith(".esp") ||
                lower.endsWith(".esm") ||
                lower.endsWith(".esl")
    }

    private fun detectPluginType(fileName: String): String {
        val lower = fileName.lowercase()

        return when {
            lower.endsWith(".esm") -> "ESM"
            lower.endsWith(".esl") -> "ESL"
            else -> "ESP"
        }
    }
}