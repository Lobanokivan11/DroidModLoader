package com.shonkware.droidmodloader.engine.deploy

data class ScopedDeploymentResult(
    val dataResult: DeploymentResult,
    val rootResult: DeploymentResult
) {
    val addCount: Int
        get() = dataResult.addCount + rootResult.addCount

    val removeCount: Int
        get() = dataResult.removeCount + rootResult.removeCount

    val updateCount: Int
        get() = dataResult.updateCount + rootResult.updateCount

    val finalRecordCount: Int
        get() = dataResult.finalRecordCount + rootResult.finalRecordCount

    val rootChanged: Boolean
        get() = rootResult.addCount > 0 ||
                rootResult.removeCount > 0 ||
                rootResult.updateCount > 0 ||
                rootResult.finalRecordCount > 0

    val dataChanged: Boolean
        get() = dataResult.addCount > 0 ||
                dataResult.removeCount > 0 ||
                dataResult.updateCount > 0 ||
                dataResult.finalRecordCount > 0
}