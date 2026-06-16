package com.shonkware.droidmodloader.ui.workflow

import com.shonkware.droidmodloader.engine.model.Mod
import com.shonkware.droidmodloader.engine.model.ModType
import org.junit.Assert.assertEquals
import org.junit.Test

class ArchiveImportExecutionWorkflowTest {

    @Test
    fun sanitizeArchiveDisplayName_replacesUnsupportedCharacters() {
        assertEquals(
            "A_Mod_Name__1_.7z",
            sanitizeArchiveDisplayName("A/Mod:Name?[1].7z")
        )
    }

    @Test
    fun sanitizeArchiveDisplayName_preservesSupportedCharacters() {
        assertEquals(
            "My Mod-1.2_test.zip",
            sanitizeArchiveDisplayName("My Mod-1.2_test.zip")
        )
    }

    @Test
    fun calculateNextArchivePriority_returnsOneForEmptyList() {
        assertEquals(1, calculateNextArchivePriority(emptyList()))
    }

    @Test
    fun calculateNextArchivePriority_returnsOneMoreThanHighestPriority() {
        val mods = listOf(
            mod(id = "first", priority = 10),
            mod(id = "second", priority = 3),
            mod(id = "third", priority = 25)
        )

        assertEquals(26, calculateNextArchivePriority(mods))
    }

    private fun mod(id: String, priority: Int): Mod {
        return Mod(
            id = id,
            name = id,
            installPath = "/mods/$id",
            enabled = true,
            priority = priority,
            modType = ModType.LOOSE
        )
    }
}
