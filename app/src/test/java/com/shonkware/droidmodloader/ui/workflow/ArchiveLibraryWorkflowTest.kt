package com.shonkware.droidmodloader.ui.workflow

import com.shonkware.droidmodloader.engine.download.DownloadedArchiveRecord
import com.shonkware.droidmodloader.engine.install.InstallerPlan
import com.shonkware.droidmodloader.engine.install.InstallerType
import com.shonkware.droidmodloader.engine.install.PreparedArchiveInstall
import com.shonkware.droidmodloader.engine.model.Mod
import com.shonkware.droidmodloader.engine.model.ModType
import com.shonkware.droidmodloader.ui.archive.ArchiveLibraryItemStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class ArchiveLibraryWorkflowTest {
    @Test
    fun buildItemsUsesCurrentModsAndFilePresenceForStatus() {
        val tempDir = createTempDir(prefix = "archive-library-items-")
        try {
            val availableFile = File(tempDir, "available.zip").apply { writeText("available") }
            val installedFile = File(tempDir, "installed.7z").apply { writeText("installed") }
            val records = listOf(
                archiveRecord(
                    id = "available",
                    fileName = availableFile.name,
                    installedModId = "deleted-mod",
                    createdAtMillis = 20L
                ),
                archiveRecord(
                    id = "installed",
                    fileName = installedFile.name,
                    installedModId = "installed-mod",
                    createdAtMillis = 30L
                ),
                archiveRecord(
                    id = "missing",
                    fileName = "missing.zip",
                    createdAtMillis = 10L
                )
            )

            val items = buildArchiveLibraryItems(
                records = records,
                currentMods = listOf(mod("installed-mod", "Installed Mod")),
                resolveArchiveFile = { record -> File(tempDir, record.relativePath) }
            )

            assertEquals(listOf("installed", "available", "missing"), items.map { it.archiveId })
            assertEquals(ArchiveLibraryItemStatus.INSTALLED, items[0].status)
            assertEquals("Installed Mod", items[0].installedModName)
            assertEquals(ArchiveLibraryItemStatus.AVAILABLE, items[1].status)
            assertEquals(ArchiveLibraryItemStatus.MISSING_FILE, items[2].status)
        } finally {
            tempDir.deleteRecursively()
        }
    }

    @Test
    fun installAvailableArchiveFinalizesAndRefreshesLibrary() {
        val archiveFile = File.createTempFile("saved-archive-", ".zip")
        try {
            val record = archiveRecord(
                id = "archive-1",
                fileName = archiveFile.name,
                createdAtMillis = 100L
            )
            val engine = FakeArchiveLibraryEngine(
                records = mutableListOf(record),
                archiveFile = archiveFile,
                prepared = prepared(InstallerType.SIMPLE),
                currentMods = mutableListOf(mod("existing", "Existing", priority = 1))
            )
            val logs = mutableListOf<String>()
            val finishes = mutableListOf<String>()
            var refreshCount = 0
            var shownItems = emptyList<String>()

            val workflow = workflow(
                engine = engine,
                logs = logs,
                finishes = finishes,
                refresh = { refreshCount++ },
                showLibrary = { items, _ -> shownItems = items.map { it.archiveId } }
            )

            workflow.installArchive("archive-1")

            assertEquals("imported_archive", engine.finalizeSourceType)
            assertEquals("archive-1", engine.markedArchiveId)
            assertEquals("installed-from-library", engine.markedModId)
            assertEquals(listOf("existing", "installed-from-library"), engine.savedMods.map { it.id })
            assertEquals(listOf(1, 2), engine.savedMods.map { it.priority })
            assertEquals(1, engine.syncCount)
            assertEquals(1, engine.routingSummaryCount)
            assertEquals(1, refreshCount)
            assertEquals(listOf("archive-1"), shownItems)
            assertTrue(logs.contains("RESULT: PASS"))
            assertEquals("Saved archive installed successfully.", finishes.last())
        } finally {
            archiveFile.delete()
        }
    }

    @Test
    fun installerChoiceArchiveClosesLibraryAndDefersFinalization() {
        val archiveFile = File.createTempFile("saved-choice-", ".zip")
        try {
            val record = archiveRecord(id = "choice", fileName = archiveFile.name)
            val prepared = prepared(InstallerType.FOMOD)
            val engine = FakeArchiveLibraryEngine(
                records = mutableListOf(record),
                archiveFile = archiveFile,
                prepared = prepared
            )
            var closeCount = 0
            var shownArchiveId: String? = null
            var shownPrepared: PreparedArchiveInstall? = null
            val finishes = mutableListOf<String>()

            val workflow = workflow(
                engine = engine,
                finishes = finishes,
                closeLibrary = { closeCount++ },
                showInstallerChoices = { value, archiveId ->
                    shownPrepared = value
                    shownArchiveId = archiveId
                }
            )

            workflow.installArchive("choice")

            assertEquals(1, closeCount)
            assertEquals("choice", shownArchiveId)
            assertEquals(prepared, shownPrepared)
            assertEquals(0, engine.finalizeCount)
            assertEquals("Choose installer options.", finishes.single())
        } finally {
            archiveFile.delete()
        }
    }

    @Test
    fun operationInProgressIgnoresInstallRequest() {
        val archiveFile = File.createTempFile("saved-busy-", ".zip")
        try {
            val engine = FakeArchiveLibraryEngine(
                records = mutableListOf(archiveRecord("busy", archiveFile.name)),
                archiveFile = archiveFile,
                prepared = prepared(InstallerType.SIMPLE)
            )
            val logs = mutableListOf<String>()
            var beginCount = 0

            val workflow = workflow(
                engine = engine,
                operationInProgress = true,
                logs = logs,
                beginOperation = { beginCount++ }
            )

            workflow.installArchive("busy")

            assertEquals(0, beginCount)
            assertEquals(0, engine.finalizeCount)
            assertTrue(logs.single().contains("operation already in progress"))
        } finally {
            archiveFile.delete()
        }
    }

    private fun workflow(
        engine: ArchiveLibraryEngine,
        operationInProgress: Boolean = false,
        logs: MutableList<String> = mutableListOf(),
        finishes: MutableList<String> = mutableListOf(),
        beginOperation: (String) -> Unit = {},
        refresh: () -> Unit = {},
        showLibrary: (List<com.shonkware.droidmodloader.ui.archive.ArchiveLibraryUiItem>, String) -> Unit = { _, _ -> },
        closeLibrary: () -> Unit = {},
        showInstallerChoices: (PreparedArchiveInstall, String) -> Unit = { _, _ -> }
    ): ArchiveLibraryWorkflow {
        return ArchiveLibraryWorkflow(
            isOperationInProgress = { operationInProgress },
            createEngine = { engine },
            showLibrary = showLibrary,
            closeLibrary = closeLibrary,
            showInstallerChoices = showInstallerChoices,
            beginOperation = beginOperation,
            finishOperation = { finishes += it },
            failOperation = { message, throwable ->
                throw AssertionError("Unexpected failure: $message", throwable)
            },
            appendLog = { logs += it },
            refreshDashboard = refresh
        )
    }

    private fun archiveRecord(
        id: String,
        fileName: String,
        installedModId: String? = null,
        createdAtMillis: Long = 1L
    ): DownloadedArchiveRecord {
        return DownloadedArchiveRecord(
            archiveId = id,
            displayName = fileName.substringBeforeLast('.'),
            fileName = fileName,
            archiveFormat = fileName.substringAfterLast('.', "archive"),
            relativePath = fileName,
            sizeBytes = 100L,
            modifiedAtMillis = 1L,
            fingerprint = "fingerprint-$id",
            installedModId = installedModId,
            createdAtMillis = createdAtMillis
        )
    }

    private fun mod(
        id: String,
        name: String,
        priority: Int = 1
    ): Mod {
        return Mod(
            id = id,
            name = name,
            installPath = "/mods/$id",
            enabled = true,
            priority = priority,
            modType = ModType.LOOSE
        )
    }

    private fun prepared(type: InstallerType): PreparedArchiveInstall {
        return PreparedArchiveInstall(
            archivePath = "/archive.zip",
            archiveName = "archive.zip",
            modName = "Installed From Library",
            sessionRootPath = "/session",
            extractedRootPath = "/session/extracted",
            installRootPath = "/session/extracted",
            plan = InstallerPlan(
                installerType = type,
                displayName = "Archive",
                rootPath = "/session/extracted",
                groups = emptyList()
            )
        )
    }

    private class FakeArchiveLibraryEngine(
        private val records: MutableList<DownloadedArchiveRecord>,
        private val archiveFile: File,
        private val prepared: PreparedArchiveInstall,
        private val currentMods: MutableList<Mod> = mutableListOf()
    ) : ArchiveLibraryEngine {
        var finalizeSourceType: String? = null
        var finalizeCount = 0
        var markedArchiveId: String? = null
        var markedModId: String? = null
        var savedMods: List<Mod> = emptyList()
        var syncCount = 0
        var routingSummaryCount = 0

        override fun getDownloadedArchives(): List<DownloadedArchiveRecord> = records.toList()

        override fun getCurrentMods(): List<Mod> = currentMods.toList()

        override fun getInstalledModsFromFolders(): List<Mod> = currentMods.toList()

        override fun resolveDownloadedArchiveFile(record: DownloadedArchiveRecord): File = archiveFile

        override fun prepareArchiveInstall(archiveFile: File): PreparedArchiveInstall = prepared

        override fun finalizePreparedArchiveInstall(
            prepared: PreparedArchiveInstall,
            selectedOptionIds: Set<String>,
            priority: Int,
            sourceType: String
        ): Mod {
            finalizeCount++
            finalizeSourceType = sourceType
            return Mod(
                id = "installed-from-library",
                name = "Installed From Library",
                installPath = "/mods/installed-from-library",
                enabled = true,
                priority = priority,
                modType = ModType.LOOSE
            )
        }

        override fun markDownloadedArchiveInstalled(
            archiveId: String,
            installedModId: String
        ) {
            markedArchiveId = archiveId
            markedModId = installedModId
            val index = records.indexOfFirst { it.archiveId == archiveId }
            if (index >= 0) {
                records[index] = records[index].copy(installedModId = installedModId)
            }
        }

        override fun saveCurrentMods(mods: List<Mod>) {
            savedMods = mods
            currentMods.clear()
            currentMods.addAll(mods)
        }

        override fun syncPlugins() {
            syncCount++
        }

        override fun appendInstalledModRoutingSummary(mod: Mod) {
            routingSummaryCount++
        }
    }
}
