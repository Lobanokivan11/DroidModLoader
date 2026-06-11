package com.shonkware.droidmodloader.ui.workflow

internal class FullscreenPanelActionWorkflowController(
    private val openModsPanel: () -> Unit,
    private val openPluginsPanel: () -> Unit,
    private val closePanel: () -> Unit,
    private val applyModOrder: (List<String>) -> Unit,
    private val applyPluginOrder: (List<String>) -> Unit
) {

    fun openModsFullscreen() {
        openModsPanel()
    }

    fun openPluginsFullscreen() {
        openPluginsPanel()
    }

    fun closeFullscreenPanel() {
        closePanel()
    }

    fun applyModOrder(orderedModIds: List<String>) {
        applyModOrder.invoke(orderedModIds)
    }

    fun applyPluginOrder(orderedPluginPaths: List<String>) {
        applyPluginOrder.invoke(orderedPluginPaths)
    }
}