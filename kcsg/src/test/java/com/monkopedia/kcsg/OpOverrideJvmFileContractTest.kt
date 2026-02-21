package com.monkopedia.kcsg

import com.monkopedia.kcsg.testutil.RecordingOpOverride
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Test
import kotlinx.io.files.Path

class OpOverrideJvmFileContractTest {
    @Test
    fun stlFileDispatchesToOverrideBeforeReadingDisk() {
        val sentinel = CSG.withOverride(null) { CSG.fromPolygons() }
        val override = RecordingOpOverride(
            fileResult = { sentinel },
        )

        val previous = CSG.opOverride
        try {
            CSG.opOverride = override

            val result = STL.file(Path("unused.stl"))

            assertSame(sentinel, result)
            assertEquals(1, override.fileCalls.size)
        } finally {
            CSG.opOverride = previous
        }
    }
}
