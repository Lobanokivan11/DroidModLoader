package com.shonkware.droidmodloader.ui.workflow

import com.shonkware.droidmodloader.engine.ModEngine
import com.shonkware.droidmodloader.engine.download.DownloadedArchiveRecord
import com.shonkware.droidmodloader.engine.install.PreparedArchiveInstall
import com.shonkware.droidmodloader.engine.model.Mod
import com.shonkware.droidmodloader.ui.archive.ArchiveLibraryItemStatus
import com.shonkware.droidmodloader.ui.archive.ArchiveLibraryUiItem
import java.io.File

internal interface ArchiveLibraryEngine {
    fun getDownloadedArchives(): List<DownloadedArchiveRecord>
    fun getCurrentMods(): List<Mod>
    fun getInstalledModsFromFolders(): List<Mod>
    fun resolveDownloadedArchiveFile(record: DownloadedArchiveRecord): File
    fun prepareArchiveInstall(archiveFile: File): PreparedArchiveInstall
    fun finalizePreparedArchiveInstall(
        prepared: PreparedArchiveInstall,
        selectedOptionIds: Set<String>,
        priority: Int,
        sourceType: String
    ): Mod
    fun markDownloadedArchiveInstalled(archiveId: String, installedModId: String)
    fun saveCurrentMods(mods: List<Mod>)
    fun syncPlugins()
    fun appendInstalledModRoutingSummary(mod: Mod)
}

internal class ArchiveLibraryEngineAdapter(
    private val engine: ModEngine,
    private val syncPluginsAction: () -> Unit,
    private val appendRoutingSummary: (Mod) -> Unit
) : ArchiveLibraryEngine {
    override fun getDownloadedArchives(): List<DownloadedArchiveRecord> =
        engine.getDownloadedArchives()

    override fun getCurrentMods(): List<Mod> = engine.getCurrentMods()

    override fun getInstalledModsFromFolders(): List<Mod> =
        engine.getInstalledModsFromFolders()

    override fun resolveDownloadedArchiveFile(record: DownloadedArchiveRecord): File =
        engine.resolveDownloadedArchiveFile(record)

    override fun prepareArchiveInstall(archiveFile: File): PreparedArchiveInstall =
        engine.prepareArchiveInstall(archiveFile)

    override fun finalizePreparedArchiveInstall(
        prepared: PreparedArchiveInstall,
        selectedOptionIds: Set<String>,
        priority: Int,
        sourceType: String
    ): Mod = engine.finalizePreparedArchiveInstall(
        prepared = prepared,
        selectedOptionIds = selectedOptionIds,
        priority = priority,
        sourceType = sourceType
    )

    override fun markDownloadedArchiveInstalled(
        archiveId: String,
        installedModId: String
    ) {
        engine.markDownloadedArchiveInstalled(archiveId, installedModId)
    }

    override fun saveCurrentMods(mods: List<Mod>) {
        engine.saveCurrentMods(mods)
    }

    override fun syncPlugins() {
        syncPluginsAction()
    }

    override fun appendInstalledModRoutingSummary(mod: Mod) {
        appendRoutingSummary(mod)
    }
}

internal class ArchiveLibraryWorkflow(
    private val isOperationInProgress: () -> Boolean,
    private val createEngine: () -> ArchiveLibraryEngine?,
    private val showLibrary: (List<ArchiveLibraryUiItem>, String) -> Unit,
    private val closeLibrary: () -> Unit,
    private val showInstallerChoices: (PreparedArchiveInstall, String) -> Unit,
    private val beginOperation: (String) -> Unit,
    private val finishOperation: (String) -> Unit,
    private val failOperation: (String, Throwable?) -> Unit,
    private val appendLog: (String) -> Unit,
    private val refreshDashboard: () -> Unit
) {
    fun openLibrary() {
        val engine = createEngine()
        if (engine == null) {
            showLibrary(emptyList(), "Archive library is unavailable.")
            return
        }

        val items = buildArchiveLibraryItems(
            records = engine.getDownloadedArchives(),
            currentMods = engine.getCurrentMods(),
            resolveArchiveFile = engine::resolveDownloadedArchiveFile
        )

        val message = if (items.isEmpty()) {
            "No saved archives yet. Choose an archive from your device first."
        } else {
            "${items.size} saved archive${if (items.size == 1) "" else "s"}."
        }

        showLibrary(items, message)
    }

    fun installArchive(archiveId: String) {
        if (isOperationInProgress()) {
            appendLog("Ignoring archive install request: operation already in progress.")
            return
        }

        beginOperation("Preparing saved archive...")

        val engine = createEngine()
        if (engine == null) {
            failOperation("Saved archive install failed: engine could not be created.", null)
            return
        }

        try {
            val record = engine.getDownloadedArchives()
                .firstOrNull { it.archiveId == archiveId }
                ?: throw IllegalStateException("Saved archive record was not found.")

            val archiveFile = engine.resolveDownloadedArchiveFile(record)
            if (!archiveFile.isFile) {
                throw IllegalStateException("Saved archive file is missing: ${record.fileName}")
            }

            val currentMods = engine.getCurrentMods()
            if (record.installedModId != null && currentMods.any { it.id == record.installedModId }) {
                finishOperation("Archive is already installed.")
                openLibrary()
                return
            }

            val prepared = engine.prepareArchiveInstall(archiveFile)
            val nextPriority = calculateNextLibraryInstallPriority(engine.getInstalledModsFromFolders())

            if (prepared.plan.requiresUserChoice) {
                closeLibrary()
                showInstallerChoices(prepared, record.archiveId)
                appendLog("Installer choices required: ${prepared.plan.installerType}")
                appendLog("Pending installer archive record ID: ${record.archiveId}")
                prepared.plan.warnings.forEach { warning ->
                    appendLog("INSTALLER WARNING: $warning")
                }
                finishOperation("Choose installer options.")
                return
            }

            val installedMod = engine.finalizePreparedArchiveInstall(
                prepared = prepared,
                selectedOptionIds = prepared.plan.defaultSelectedOptionIds,
                priority = nextPriority,
                sourceType = "imported_archive"
            )

            engine.markDownloadedArchiveInstalled(
                archiveId = record.archiveId,
                installedModId = installedMod.id
            )

            val remainingMods = currentMods
                .filterNot { it.id == installedMod.id }
                .sortedBy { it.priority }
            val updatedMods = remainingMods + installedMod.copy(priority = remainingMods.size + 1)

            engine.saveCurrentMods(updatedMods)
            engine.syncPlugins()

            appendLog("Installed saved archive: ${record.displayName}")
            appendLog("Archive record marked installed: ${record.archiveId}")
            engine.appendInstalledModRoutingSummary(installedMod)
            appendLog("Saved installed mod count after archive install: ${updatedMods.size}")
            appendLog("Plugins refreshed automatically.")
            appendLog("RESULT: PASS")
            finishOperation("Saved archive installed successfully.")
        } catch (t: Throwable) {
            appendLog("CRASH TYPE: ${t::class.java.name}")
            appendLog("RESULT: FAIL")
            failOperation("Saved archive install failed: ${t.message}", t)
        }

        refreshDashboard()
        openLibrary()
        appendLog("----- Saved Archive Install Workflow End -----")
    }
}

internal fun buildArchiveLibraryItems(
    records: List<DownloadedArchiveRecord>,
    currentMods: List<Mod>,
    resolveArchiveFile: (DownloadedArchiveRecord) -> File
): List<ArchiveLibraryUiItem> {
    val modsById = currentMods.associateBy { it.id }

    return records
        .sortedByDescending { it.createdAtMillis }
        .map { record ->
            val installedMod = record.installedModId?.let(modsById::get)
            val archiveFileExists = resolveArchiveFile(record).isFile
            val status = when {
                installedMod != null -> ArchiveLibraryItemStatus.INSTALLED
                !archiveFileExists -> ArchiveLibraryItemStatus.MISSING_FILE
                else -> ArchiveLibraryItemStatus.AVAILABLE
            }

            ArchiveLibraryUiItem(
                archiveId = record.archiveId,
                displayName = record.displayName.ifBlank { record.fileName },
                fileName = record.fileName,
                archiveFormat = record.archiveFormat,
                sizeBytes = record.sizeBytes,
                createdAtMillis = record.createdAtMillis,
                status = status,
                installedModName = installedMod?.name,
                version = record.version,
                sourceLabel = buildSourceLabel(record)
            )
        }
}

private fun buildSourceLabel(record: DownloadedArchiveRecord): String? {
    if (!record.nexusGameDomain.isNullOrBlank() && record.nexusModId != null) {
        val fileSuffix = record.nexusFileId?.let { " / file $it" }.orEmpty()
        return "Nexus: ${record.nexusGameDomain} / mod ${record.nexusModId}$fileSuffix"
    }

    return record.sourceUrl?.takeIf { it.isNotBlank() }
}

private fun calculateNextLibraryInstallPriority(existingMods: List<Mod>): Int {
    return if (existingMods.isEmpty()) {
        1
    } else {
        existingMods.maxOf { it.priority } + 1
    }
}
