package com.monkopedia.kcsg

import com.monkopedia.kcsg.testutil.RecordingOpOverride
import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlin.test.assertSame

class CSGWithOverrideExceptionTest {
    @Test
    fun withOverrideRestoresPreviousOverrideAfterException() {
        val previous = RecordingOpOverride()
        val temporary = RecordingOpOverride()
        val original = CSG.opOverride

        try {
            CSG.opOverride = previous
            assertFailsWith<IllegalStateException> {
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
