package com.monkopedia.kcsg

/**
 * Simple implementation that surfaces a [KcsgScript] as a [ImportedScript]
 */
class ImportedKcsgScript(private val script: KcsgScript) : ImportedScript {
    override val exports: Collection<String>
        get() = script.exports()
    override val targets: Collection<String>
        get() = script.targets()

    override fun get(name: String): CSG {
        return script.generateTarget(name)
    }
}
