package com.monkopedia.kcsg

import java.nio.file.Path
import kotlin.properties.PropertyDelegateProvider
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

abstract class KcsgBuilder {
    /**
     * Version of [csg] that allows a primitive to be returned from the builder instead.
     */
    inline fun primitive(
        exported: Boolean = false,
        allowCaching: Boolean = true,
        crossinline lazyBuilder: BuilderContext.() -> Primitive
    ): PropertyDelegateProvider<Nothing?, ReadOnlyProperty<Nothing?, CSG>> {
        return csg(exported = exported, allowCaching = allowCaching) {
            lazyBuilder().toCSG()
        }
    }

    /**
     * Creates a lazy CSG property labeled by name of the property. By default this property is not
     * exported and will only be built if exported by default or caller.
     *
     * Setting [exported] to true has the same effect as calling [export] on the property.
     */
    fun csg(
        exported: Boolean = false,
        allowCaching: Boolean = true,
        lazyBuilder: BuilderContext.() -> CSG
    ): PropertyDelegateProvider<Nothing?, ReadOnlyProperty<Nothing?, CSG>> {
        return PropertyDelegateProvider { _, property ->
            val propertyName = property.name
            val lazy = lazy {
                if (allowCaching && supportsCaching) {
                    getCached(lazyBuilder)
                } else {
                    BuilderContextImpl.lazyBuilder()
                }
            }.wrapGetter {
                CSG.opOverride?.operation("p:${getHash(lazyBuilder)}")
            }
            track(propertyName, lazy)
            if (exported) {
                export(propertyName)
            }
            ReadOnlyProperty { _, _ ->
                lazy.value
            }
        }
    }

    /**
     * Imports another script.
     *
     * The resolve behavior depends on the host that is executing this script.
     */
    fun import(csgsName: String): Lazy<ImportedScript> {
        return lazy { findScript(csgsName) }
    }

    /**
     * Loads an STL with the same registration as a named lazy object as [csg].
     */
    fun stl(stlName: String): PropertyDelegateProvider<Nothing?, ReadOnlyProperty<Nothing?, CSG>> {
        return PropertyDelegateProvider { _, property ->
            val propertyName = property.name
            val lazy = lazy { STL.file(findStl(stlName)) }
            track(propertyName, lazy)
            ReadOnlyProperty { _, _ ->
                lazy.value
            }
        }
    }

    /**
     * Exports [name] by default in execution. This will only set it for default exporting,
     * and can be overridden to false by the executor. Similarly, the executor can also export
     * properties that are not exported by default.
     */
    fun export(name: String) {
        exportProperty(name)
    }

    /**
     * Same effect as [export] but for typo proof execution.
     */
    fun export(property: KProperty<CSG>) {
        export(property.name)
    }

    private fun getCached(lazyBuilder: BuilderContext.() -> CSG): CSG {
        val hash = getHash(lazyBuilder)
        return checkCached(hash)
            ?: CSG.withOverride(null) { BuilderContextImpl.lazyBuilder() }.also {
                storeCached(hash, it)
            }
    }

    private fun getHash(lazyBuilder: BuilderContext.() -> CSG): String {
        return HashingOpOverride().also {
            CSG.withOverride(it) {
                BuilderContextImpl.lazyBuilder()
            }
        }.hash()
    }

    protected open val supportsCaching: Boolean
        get() = false

    protected abstract fun exportProperty(propertyName: String)
    protected abstract fun track(propertyName: String, lazy: Lazy<CSG>)
    protected abstract fun findStl(stlName: String): Path
    protected abstract fun findScript(csgsName: String): ImportedScript
    protected open fun checkCached(hash: String): CSG? = null
    protected open fun storeCached(hash: String, csg: CSG) {}

    sealed class BuilderContext

    private object BuilderContextImpl : BuilderContext()
}

private inline fun <T> Lazy<T>.wrapGetter(crossinline getter: () -> T?): Lazy<T> {
    return object : Lazy<T> {
        override val value: T
            get() = getter() ?: this@wrapGetter.value

        override fun isInitialized(): Boolean = this@wrapGetter.isInitialized()
    }
}
