package com.shonkware.droidmodloader.ui.workflow

internal class DeploymentActionWorkflowController(
    private val runInBackground: (() -> Unit) -> Unit,
    private val runDeploy: () -> Unit,
    private val runForceFullRedeploy: () -> Unit,
    private val buildDeploymentPlan: () -> Unit,
    private val buildFullRedeployPlan: () -> Unit
) {

    fun deploy() {
        runInBackground {
            runDeploy()
        }
    }

    fun forceFullRedeploy() {
        runInBackground {
            runForceFullRedeploy()
        }
    }

    fun buildDeployPlan() {
        runInBackground {
            buildDeploymentPlan()
        }
    }

    fun buildFullRedeployPlan() {
        runInBackground {
            buildFullRedeployPlan()
        }
    }
}