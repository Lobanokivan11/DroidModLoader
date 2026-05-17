package com.shonkware.droidmodloader.engine.deploy

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import com.shonkware.droidmodloader.engine.model.FileRecord
import java.io.File
import java.io.IOException
import com.shonkware.droidmodloader.engine.model.DeploymentRecord


class TreeUriDeploymentManager(
    private val context: Context,
    private val contentResolver: ContentResolver,
    private val treeUri: Uri

) {


    private val root: DocumentFile by lazy {
        DocumentFile.fromTreeUri(context, treeUri)
            ?: throw IOException("Could not open selected Tree URI target folder.")
    }

    private val directoryCache = mutableMapOf<String, DocumentFile>()
    private val fileCache = mutableMapOf<String, DocumentFile?>()

    fun deploy(
        oldManifest: List<DeploymentRecord>,
        newWinningRecords: List<FileRecord>
    ): Pair<List<DeploymentRecord>, DeploymentResult> {
        directoryCache.clear()
        fileCache.clear()
        directoryCache[""] = root

        val oldByPath = oldManifest.associateBy { it.normalizedPath }
        val newByPath = newWinningRecords.associateBy { it.normalizedPath }

        val removes = oldByPath
            .filterKeys { path -> path !in newByPath }
            .values
            .sortedByDescending { it.normalizedPath.count { c -> c == '/' } }

        val adds = newByPath
            .filterKeys { path -> path !in oldByPath }
            .values
            .sortedBy { it.normalizedPath }

        val updates = newByPath
            .filter { (path, newRecord) ->
                val oldRecord = oldByPath[path]
                oldRecord != null && oldRecord.hash != newRecord.hash
            }
            .values
            .sortedBy { it.normalizedPath }

        for (record in removes) {
            deleteFileIfPresent(record.normalizedPath)
        }

        for (record in updates) {
            deleteFileIfPresent(record.normalizedPath)
            copyRecordToTarget(record)
        }

        for (record in adds) {
            copyRecordToTarget(record)
        }

        val result = DeploymentResult(
            addCount = adds.size,
            removeCount = removes.size,
            updateCount = updates.size,
            finalRecordCount = newWinningRecords.size
        )

        val newManifest = newWinningRecords
            .map { it.toDeploymentRecord() }
            .sortedBy { it.normalizedPath }

        return Pair(newManifest, result)
    }

    private fun copyRecordToTarget(record: FileRecord) {
        val sourceFile = File(record.sourceFilePath)

        if (!sourceFile.exists() || !sourceFile.isFile) {
            throw IOException("Source file missing during Tree URI deploy: ${record.sourceFilePath}")
        }

        val parentDir = getOrCreateParentDirectory(record.normalizedPath)
        val fileName = record.normalizedPath.substringAfterLast("/")

        val existing = findCachedFile(record.normalizedPath, parentDir, fileName)
        existing?.delete()
        fileCache.remove(record.normalizedPath)

        val targetFile = parentDir.createFile(
            guessMimeType(fileName),
            fileName
        ) ?: throw IOException("Could not create target file through Tree URI: ${record.normalizedPath}")

        sourceFile.inputStream().use { input ->
            contentResolver.openOutputStream(targetFile.uri, "w").use { output ->
                if (output == null) {
                    throw IOException("Could not open target output stream: ${record.normalizedPath}")
                }

                input.copyTo(output)
            }
        }

        fileCache[record.normalizedPath] = targetFile
    }

    private fun deleteFileIfPresent(normalizedPath: String) {
        val parentPath = normalizedPath.substringBeforeLast("/", missingDelimiterValue = "")
        val fileName = normalizedPath.substringAfterLast("/")

        val parentDir = getExistingDirectory(parentPath) ?: return
        val file = findCachedFile(normalizedPath, parentDir, fileName) ?: return

        file.delete()
        fileCache.remove(normalizedPath)
    }

    private fun getOrCreateParentDirectory(normalizedPath: String): DocumentFile {
        val parentPath = normalizedPath.substringBeforeLast("/", missingDelimiterValue = "")

        if (parentPath.isBlank()) {
            return root
        }

        var currentPath = ""
        var currentDir = root

        val parts = parentPath
            .split("/")
            .filter { it.isNotBlank() }

        for (part in parts) {
            currentPath = if (currentPath.isBlank()) {
                part
            } else {
                "$currentPath/$part"
            }

            val cached = directoryCache[currentPath]
            if (cached != null && cached.exists() && cached.isDirectory) {
                currentDir = cached
                continue
            }

            val existing = currentDir.findFile(part)
            val nextDir = when {
                existing != null && existing.isDirectory -> existing

                existing != null && existing.isFile -> {
                    throw IOException("Target path segment is a file, expected directory: $currentPath")
                }

                else -> {
                    currentDir.createDirectory(part)
                        ?: throw IOException("Could not create target directory through Tree URI: $currentPath")
                }
            }

            directoryCache[currentPath] = nextDir
            currentDir = nextDir
        }

        return currentDir
    }

    private fun getExistingDirectory(path: String): DocumentFile? {
        if (path.isBlank()) {
            return root
        }

        val cached = directoryCache[path]
        if (cached != null && cached.exists() && cached.isDirectory) {
            return cached
        }

        var currentPath = ""
        var currentDir = root

        val parts = path
            .split("/")
            .filter { it.isNotBlank() }

        for (part in parts) {
            currentPath = if (currentPath.isBlank()) {
                part
            } else {
                "$currentPath/$part"
            }

            val cachedPart = directoryCache[currentPath]
            if (cachedPart != null && cachedPart.exists() && cachedPart.isDirectory) {
                currentDir = cachedPart
                continue
            }

            val next = currentDir.findFile(part)
            if (next == null || !next.isDirectory) {
                return null
            }

            directoryCache[currentPath] = next
            currentDir = next
        }

        return currentDir
    }

    private fun findCachedFile(
        normalizedPath: String,
        parentDir: DocumentFile,
        fileName: String
    ): DocumentFile? {
        if (fileCache.containsKey(normalizedPath)) {
            val cached = fileCache[normalizedPath]
            if (cached != null && cached.exists()) {
                return cached
            }

            if (cached == null) {
                return null
            }
        }

        val found = parentDir.findFile(fileName)
        fileCache[normalizedPath] = found

        return found
    }

    private fun guessMimeType(fileName: String): String {
        val lower = fileName.lowercase()

        return when {
            lower.endsWith(".txt") ||
                    lower.endsWith(".ini") ||
                    lower.endsWith(".cfg") ||
                    lower.endsWith(".json") ||
                    lower.endsWith(".xml") -> "text/plain"

            lower.endsWith(".dds") -> "image/vnd.ms-dds"
            lower.endsWith(".png") -> "image/png"
            lower.endsWith(".jpg") || lower.endsWith(".jpeg") -> "image/jpeg"

            else -> "application/octet-stream"
        }
    }

    private fun FileRecord.toDeploymentRecord(): DeploymentRecord {
        return DeploymentRecord(
            normalizedPath = normalizedPath,
            sourceFilePath = sourceFilePath,
            winningModId = winningModId,
            winningModName = winningModName,
            hash = hash
        )
    }
}