package com.shonkware.droidmodloader.ui.workflow

import com.shonkware.droidmodloader.engine.deploy.DeploymentResult
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

internal object OperationLogFormatter {

    fun timestampNow(): String {
        return SimpleDateFormat("HH:mm:ss.SSS", Locale.US).format(Date())
    }

    fun formatLogLine(message: String): String {
        return "[${timestampNow()}] $message"
    }

    fun formatOperationDuration(startedAtMillis: Long): String {
        if (startedAtMillis <= 0L) {
            return "0 ms"
        }

        val elapsedMillis = System.currentTimeMillis() - startedAtMillis
        return "${elapsedMillis.coerceAtLeast(0L)} ms"
    }

    fun deploymentResultBlockLines(
        title: String,
        result: DeploymentResult
    ): List<String> {
        return listOf(
            "$title:",
            "  Adds: ${result.addCount}",
            "  Removes: ${result.removeCount}",
            "  Updates: ${result.updateCount}",
            "  Backups created: ${result.backupCount}",
            "  Backups restored: ${result.restoreCount}",
            "  Protected conflicts: ${result.protectedConflictCount}",
            "  Final file count: ${result.finalRecordCount}"
        )
    }
}