package com.shonkware.droidmodloader.ui.workflow

import com.shonkware.droidmodloader.engine.ModEngine

internal class PluginSyncWorkflowController(
    private val createEngine: () -> ModEngine?,
    private val syncPluginsFromCurrentState: (ModEngine) -> Unit,
    private val refreshDashboard: () -> Unit
) {

    fun syncWithNewEngineThenRefresh() {
        val engine = createEngine()

        if (engine != null) {
            syncPluginsFromCurrentState(engine)
        }

        refreshDashboard()
    }

    fun syncWithExistingEngineThenRefresh(engine: ModEngine?) {
        if (engine != null) {
            syncPluginsFromCurrentState(engine)
        }

        refreshDashboard()
    }
}