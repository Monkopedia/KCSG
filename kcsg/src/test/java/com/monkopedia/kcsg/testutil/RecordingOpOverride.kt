package com.monkopedia.kcsg.testutil

import com.monkopedia.kcsg.Bounds
import com.monkopedia.kcsg.CSG
import com.monkopedia.kcsg.OpOverride
import kotlinx.io.Source
import kotlinx.io.files.Path

class RecordingOpOverride(
    private val operationResult: ((name: String, args: Array<out Any?>) -> CSG?)? = null,
    private val boundsResult: ((name: String, args: Array<out Any?>) -> Bounds?)? = null,
    private val doubleResult: ((name: String, args: Array<out Any?>) -> Double?)? = null,
    private val fileResult: ((path: Path) -> CSG?)? = null,
    private val sourceResult: ((sourceFactory: () -> Source) -> CSG?)? = null,
) : OpOverride {
    val operationCalls = mutableListOf<Pair<String, List<Any?>>>()
    val boundsCalls = mutableListOf<Pair<String, List<Any?>>>()
    val doubleCalls = mutableListOf<Pair<String, List<Any?>>>()
    val fileCalls = mutableListOf<Path>()
    val sourceCalls = mutableListOf<() -> Source>()

    override fun operation(s: String, vararg csg: Any?): CSG? {
        operationCalls += s to csg.toList()
        return operationResult?.invoke(s, csg)
    }

    override fun bounds(s: String, vararg csg: Any?): Bounds? {
        boundsCalls += s to csg.toList()
        return boundsResult?.invoke(s, csg)
    }

    override fun double(s: String, vararg csg: Any?): Double? {
        doubleCalls += s to csg.toList()
        return doubleResult?.invoke(s, csg)
    }

    override fun file(path: Path): CSG? {
        fileCalls.add(path)
        return fileResult?.invoke(path)
    }

    override fun source(sourceFactory: () -> Source): CSG? {
        sourceCalls.add(sourceFactory)
        return sourceResult?.invoke(sourceFactory)
    }
}
