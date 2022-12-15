package com.monkopedia.kcsg

import eu.mihosoft.jcsg.CSG
import eu.mihosoft.jcsg.Primitive
import eu.mihosoft.jcsg.STL
import java.nio.file.Path
import kotlin.properties.PropertyDelegateProvider
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

abstract class KcsgBuilder {
    /**
     * Version of [csg] that allows a primitive to be returned from the builder instead.
     */
    fun primitive(exported: Boolean = false, lazyBuilder: BuilderContext.() -> Primitive): PropertyDelegateProvider<Nothing?, ReadOnlyProperty<Nothing?, CSG>> {
        return PropertyDelegateProvider { _, property ->
            val propertyName = property.name
            val lazy = lazy { BuilderContextImpl.lazyBuilder().toCSG() }
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
     * Creates a lazy CSG property labeled by name of the property. By default this property is not
     * exported and will only be built if exported by default or caller.
     *
     * Setting [exported] to true has the same effect as calling [export] on the property.
     */
    fun csg(exported: Boolean = false, lazyBuilder: BuilderContext.() -> CSG): PropertyDelegateProvider<Nothing?, ReadOnlyProperty<Nothing?, CSG>> {
        return PropertyDelegateProvider { _, property ->
            val propertyName = property.name
            val lazy = lazy { BuilderContextImpl.lazyBuilder() }
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

    protected abstract fun exportProperty(propertyName: String)
    protected abstract fun track(propertyName: String, lazy: Lazy<CSG>)
    protected abstract fun findStl(stlName: String): Path

    sealed class BuilderContext

    private object BuilderContextImpl : BuilderContext()
}
