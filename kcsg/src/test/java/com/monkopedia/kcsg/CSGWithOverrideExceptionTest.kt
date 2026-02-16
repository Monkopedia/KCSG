package com.monkopedia.kcsg

import com.monkopedia.kcsg.testutil.RecordingOpOverride
import org.junit.Assert.assertSame
import org.junit.Assert.assertThrows
import org.junit.Test

class CSGWithOverrideExceptionTest {
    @Test
    fun withOverrideRestoresPreviousOverrideAfterException() {
        val previous = RecordingOpOverride()
        val temporary = RecordingOpOverride()
        val original = CSG.opOverride

        try {
            CSG.opOverride = previous
            assertThrows(IllegalStateException::class.java) {
                CSG.withOverride(temporary) {
                    throw IllegalStateException("boom")
                }
            }
            assertSame(previous, CSG.opOverride)
        } finally {
            CSG.opOverride = original
        }
    }
}
