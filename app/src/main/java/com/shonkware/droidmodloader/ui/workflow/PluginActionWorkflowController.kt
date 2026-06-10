package com.shonkware.droidmodloader.ui.workflow

internal class PluginActionWorkflowController(
    private val runInBackground: (() -> Unit) -> Unit,
    private val writeLoadOrderFiles: () -> Unit,
    private val togglePluginEnabled: (String) -> Unit,
    private val movePluginUp: (String) -> Unit,
    private val movePluginDown: (String) -> Unit,
    private val applyPluginOrder: (List<String>) -> Unit
) {

    fun writeLoadOrderFiles() {
        runInBackground {
            writeLoadOrderFiles.invoke()
        }
    }

    fun togglePlugin(normalizedPath: String) {
        runInBackground {
            togglePluginEnabled(normalizedPath)
        }
    }

    fun movePluginUp(normalizedPath: String) {
        runInBackground {
            movePluginUp.invoke(normalizedPath)
        }
    }

    fun movePluginDown(normalizedPath: String) {
        runInBackground {
            movePluginDown.invoke(normalizedPath)
        }
    }

    fun applyPluginOrder(orderedPluginPaths: List<String>) {
        runInBackground {
            applyPluginOrder(orderedPluginPaths)
        }
    }
}