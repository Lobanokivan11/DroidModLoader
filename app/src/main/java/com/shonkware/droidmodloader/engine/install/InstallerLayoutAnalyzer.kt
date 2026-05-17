package com.shonkware.droidmodloader.engine.install

import java.io.File

class InstallerLayoutAnalyzer {

    fun analyze(contentRoot: File, modName: String): InstallerPlan {
        val fomodConfig = findFomodConfig(contentRoot)
        if (fomodConfig != null) {
            return FomodInstallerParser().parse(contentRoot, fomodConfig, modName)
        }

        val bainPlan = createBainPlanIfApplicable(contentRoot, modName)
        if (bainPlan != null) {
            return bainPlan
        }

        return createSimplePlan(contentRoot, modName)
    }

    fun resolveContentRoot(rawExtractRoot: File): File {
        val directChildren = rawExtractRoot.listFiles()?.filter { it.exists() } ?: return rawExtractRoot

        val hasDirectUsefulContent =
            File(rawExtractRoot, "fomod").exists() ||
                    File(rawExtractRoot, "Data").exists() ||
                    directChildren.any { it.isFile && isLikelyDataRootFile(it.name) } ||
                    directChildren.any { it.isDirectory && isBainFolder(it.name) }

        if (hasDirectUsefulContent) {
            return rawExtractRoot
        }

        val directories = directChildren.filter { it.isDirectory }
        if (directories.size == 1) {
            val child = directories.first()
            val childChildren = child.listFiles()?.filter { it.exists() } ?: return child

            val childLooksUseful =
                File(child, "fomod").exists() ||
                        File(child, "Data").exists() ||
                        childChildren.any { it.isFile && isLikelyDataRootFile(it.name) } ||
                        childChildren.any { it.isDirectory && isBainFolder(it.name) }

            if (childLooksUseful) {
                return child
            }
        }

        return rawExtractRoot
    }

    private fun findFomodConfig(contentRoot: File): File? {
        val fomodDir = contentRoot.listFiles()
            ?.firstOrNull { it.isDirectory && it.name.equals("fomod", ignoreCase = true) }

        if (fomodDir != null) {
            val config = fomodDir.listFiles()
                ?.firstOrNull { it.isFile && it.name.equals("ModuleConfig.xml", ignoreCase = true) }

            if (config != null) return config
        }

        return null
    }

    private fun createBainPlanIfApplicable(contentRoot: File, modName: String): InstallerPlan? {
        val folders = contentRoot.listFiles()
            ?.filter { it.isDirectory && isBainFolder(it.name) }
            ?.sortedBy { it.name.lowercase() }
            ?: emptyList()

        if (folders.isEmpty()) return null

        val options = folders.mapIndexed { index, folder ->
            val core = isCoreFolder(folder.name)
            InstallerOption(
                id = "bain_$index",
                name = folder.name,
                description = if (core) "Core or required package folder" else "Optional package folder",
                sourcePath = folder.name,
                destinationPath = "",
                required = core,
                selectedByDefault = core
            )
        }

        val warnings = mutableListOf<String>()
        val optionalCount = options.count { !it.required }

        if (optionalCount > 0) {
            warnings.add("BAIN-style optional folders detected. Choose only the options you want installed.")
        }

        if (options.none { it.required }) {
            warnings.add("BAIN-style folders were found, but no obvious core folder was detected. Select at least one folder.")
        }

        return InstallerPlan(
            installerType = InstallerType.BAIN,
            displayName = modName,
            rootPath = contentRoot.absolutePath,
            groups = listOf(
                InstallerGroup(
                    id = "bain_main",
                    name = "BAIN Package Folders",
                    type = InstallerGroupType.SELECT_ANY,
                    options = options
                )
            ),
            warnings = warnings
        )
    }

    private fun createSimplePlan(contentRoot: File, modName: String): InstallerPlan {
        val hasDataFolder = File(contentRoot, "Data").exists()
        val hasRootLevelGameRootFiles = hasRootLevelGameRootContent(contentRoot)

        val source = when {
            hasDataFolder && !hasRootLevelGameRootFiles -> "Data"
            else -> "."
        }

        return InstallerPlan(
            installerType = InstallerType.SIMPLE,
            displayName = modName,
            rootPath = contentRoot.absolutePath,
            groups = listOf(
                InstallerGroup(
                    id = "simple",
                    name = "Simple Install",
                    type = InstallerGroupType.SELECT_ANY,
                    options = listOf(
                        InstallerOption(
                            id = "simple_all",
                            name = "Install detected files",
                            sourcePath = source,
                            destinationPath = "",
                            required = true,
                            selectedByDefault = true
                        )
                    )
                )
            )
        )
    }

    private fun hasRootLevelGameRootContent(contentRoot: File): Boolean {
        val children = contentRoot.listFiles() ?: return false

        return children.any { child ->
            child.isFile && isLikelyGameRootFile(child.name)
        }
    }

    private fun isLikelyGameRootFile(fileName: String): Boolean {
        val lower = fileName.lowercase()

        return lower.endsWith(".exe") ||
                lower.endsWith(".dll") ||
                lower.endsWith(".asi") ||

                lower == "d3d9.dll" ||
                lower == "d3d11.dll" ||
                lower == "dxgi.dll" ||
                lower == "dinput8.dll" ||
                lower == "xinput1_3.dll" ||

                lower == "enblocal.ini" ||
                lower == "enbseries.ini" ||

                lower.startsWith("skse_loader") ||
                lower.startsWith("nvse_loader") ||
                lower.startsWith("obse_loader") ||
                lower.startsWith("fose_loader") ||
                lower.startsWith("f4se_loader")
    }
    private fun isBainFolder(name: String): Boolean {
        val lower = name.lowercase()
        return lower.matches(Regex("""^\d{2,3}\s+.+""")) ||
                lower.startsWith("optional") ||
                lower.startsWith("options") ||
                lower.startsWith("patches") ||
                lower.startsWith("patch")
    }

    private fun isCoreFolder(name: String): Boolean {
        val lower = name.lowercase()
        return lower.contains("core") ||
                lower.contains("main") ||
                lower.contains("required") ||
                lower.startsWith("00")
    }

    private fun isLikelyDataRootFile(fileName: String): Boolean {
        val lower = fileName.lowercase()

        return lower.endsWith(".esp") ||
                lower.endsWith(".esm") ||
                lower.endsWith(".esl") ||
                lower.endsWith(".bsa") ||
                lower.endsWith(".ba2") ||
                lower.endsWith(".ini") ||

                lower.endsWith(".exe") ||
                lower.endsWith(".dll") ||
                lower.endsWith(".asi") ||

                lower == "d3d9.dll" ||
                lower == "d3d11.dll" ||
                lower == "dxgi.dll" ||
                lower == "dinput8.dll" ||
                lower == "xinput1_3.dll" ||

                lower == "enblocal.ini" ||
                lower == "enbseries.ini" ||

                lower.contains("skse_loader") ||
                lower.contains("nvse_loader") ||
                lower.contains("obse_loader") ||
                lower.contains("fose_loader") ||
                lower.contains("f4se_loader")
    }
}