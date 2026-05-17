package com.shonkware.droidmodloader.engine.rules

import com.shonkware.droidmodloader.engine.model.DeployScope

class DeployFileClassifier {

    fun classify(normalizedPath: String): DeployScope {
        val path = normalizedPath.lowercase()
        val fileName = path.substringAfterLast('/')

        return when {
            path.startsWith("fomod/") -> DeployScope.MANAGER_ONLY

            path.startsWith("src/") ||
                    path.startsWith("source/") ||
                    path.startsWith("sources/") ||
                    path.startsWith("docs/") ||
                    path.startsWith("documentation/") -> DeployScope.MANAGER_ONLY

            path == "plugins.txt" || path == "loadorder.txt" -> DeployScope.PROFILE_ONLY

            fileName == "readme.txt" ||
                    fileName == "readme.md" ||
                    fileName == "changelog.txt" ||
                    fileName == "license.txt" ||
                    fileName == "credits.txt" ||
                    fileName == "screenshot.jpg" ||
                    fileName == "screenshot.png" ||
                    fileName.endsWith(".pdf") -> DeployScope.MANAGER_ONLY

            fileName.endsWith(".cpp") ||
                    fileName.endsWith(".c") ||
                    fileName.endsWith(".h") ||
                    fileName.endsWith(".hpp") ||
                    fileName.endsWith(".sln") ||
                    fileName.endsWith(".vcxproj") ||
                    fileName.endsWith(".vcproj") ||
                    fileName.endsWith(".filters") ||
                    fileName.endsWith(".pdb") ||
                    fileName.endsWith(".lib") ||
                    fileName.endsWith(".exp") -> DeployScope.MANAGER_ONLY

            isProbableGameRootFile(path) -> DeployScope.GAME_ROOT

            else -> DeployScope.DATA
        }
    }

    private fun isProbableGameRootFile(path: String): Boolean {
        if (path.contains("/")) return false

        return path.endsWith(".dll") ||
                path.endsWith(".exe") ||
                path.endsWith(".asi") ||

                path == "d3d9.dll" ||
                path == "d3d11.dll" ||
                path == "dxgi.dll" ||
                path == "dinput8.dll" ||
                path == "xinput1_3.dll" ||

                path == "enblocal.ini" ||
                path == "enbseries.ini" ||

                path.startsWith("skse_loader") ||
                path.startsWith("nvse_loader") ||
                path.startsWith("obse_loader") ||
                path.startsWith("fose_loader") ||
                path.startsWith("f4se_loader")
    }

    fun isDataDeployable(scope: DeployScope): Boolean {
        return scope == DeployScope.DATA
    }

    fun isGameRootDeployable(scope: DeployScope): Boolean {
        return scope == DeployScope.GAME_ROOT
    }

    fun isDeployable(scope: DeployScope): Boolean {
        return scope == DeployScope.DATA || scope == DeployScope.GAME_ROOT
    }

}