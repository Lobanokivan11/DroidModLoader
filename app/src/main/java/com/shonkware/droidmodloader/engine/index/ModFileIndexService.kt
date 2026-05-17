package com.shonkware.droidmodloader.engine.index

import com.shonkware.droidmodloader.engine.model.Mod
import com.shonkware.droidmodloader.engine.util.PathUtils
import java.io.File
import java.io.FileInputStream
import java.security.MessageDigest

class ModFileIndexService(
    private val repository: ModFileIndexRepository
) {

    private data class FileSignature(
        val originalPath: String,
        val normalizedPath: String,
        val sizeBytes: Long,
        val modifiedEpochMillis: Long
    )

    fun getOrBuildIndex(mod: Mod): ModFileIndexSnapshot {
        val existing = repository.load(mod)

        if (existing != null && !isDirty(mod, existing)) {
            return existing
        }

        return rebuildIndex(mod)
    }

    fun rebuildIndex(mod: Mod): ModFileIndexSnapshot {
        val root = File(mod.installPath)

        val entries = if (root.exists() && root.isDirectory) {
            scanSignatures(root).values
                .map { signature ->
                    ModFileIndexEntry(
                        originalPath = signature.originalPath,
                        normalizedPath = signature.normalizedPath,
                        hash = computeHash(File(root, signature.originalPath)),
                        sizeBytes = signature.sizeBytes,
                        modifiedEpochMillis = signature.modifiedEpochMillis
                    )
                }
                .sortedBy { it.normalizedPath }
        } else {
            emptyList()
        }

        val snapshot = ModFileIndexSnapshot(
            schemaVersion = ModFileIndexRepository.CURRENT_SCHEMA_VERSION,
            modId = mod.id,
            modName = mod.name,
            modRootPath = mod.installPath,
            createdAtEpochMillis = System.currentTimeMillis(),
            entries = entries
        )

        repository.save(snapshot)
        return snapshot
    }

    fun deleteIndex(mod: Mod) {
        repository.delete(mod)
    }

    private fun isDirty(
        mod: Mod,
        snapshot: ModFileIndexSnapshot
    ): Boolean {
        if (snapshot.schemaVersion != ModFileIndexRepository.CURRENT_SCHEMA_VERSION) {
            return true
        }

        if (snapshot.modId != mod.id) {
            return true
        }

        if (snapshot.modRootPath != mod.installPath) {
            return true
        }

        val root = File(mod.installPath)
        if (!root.exists() || !root.isDirectory) {
            return true
        }

        val current = scanSignatures(root)
        val indexed = snapshot.entries.associateBy { it.normalizedPath }

        if (current.size != indexed.size) {
            return true
        }

        for ((path, currentSignature) in current) {
            val indexedEntry = indexed[path] ?: return true

            if (indexedEntry.sizeBytes != currentSignature.sizeBytes) {
                return true
            }

            if (indexedEntry.modifiedEpochMillis != currentSignature.modifiedEpochMillis) {
                return true
            }

            if (indexedEntry.originalPath != currentSignature.originalPath) {
                return true
            }
        }

        return false
    }

    private fun scanSignatures(root: File): Map<String, FileSignature> {
        val result = linkedMapOf<String, FileSignature>()

        root.walkTopDown()
            .onEnter { dir ->
                val name = dir.name.lowercase()
                name != "__macosx" && !name.startsWith(".")
            }
            .forEach { file ->
                if (!file.isFile) {
                    return@forEach
                }

                val originalPath = file
                    .relativeTo(root)
                    .path
                    .replace("\\", "/")

                val normalizedPath = PathUtils.normalize(originalPath) ?: return@forEach

                result[normalizedPath] = FileSignature(
                    originalPath = originalPath,
                    normalizedPath = normalizedPath,
                    sizeBytes = file.length(),
                    modifiedEpochMillis = file.lastModified()
                )
            }

        return result
    }

    private fun computeHash(file: File): String {
        val buffer = ByteArray(1024 * 64)
        val digest = MessageDigest.getInstance("SHA-256")

        FileInputStream(file).use { input ->
            while (true) {
                val read = input.read(buffer)
                if (read < 0) break
                if (read == 0) continue
                digest.update(buffer, 0, read)
            }
        }

        return digest.digest().joinToString("") { "%02x".format(it) }
    }
}