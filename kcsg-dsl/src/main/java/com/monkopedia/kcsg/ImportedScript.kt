package com.monkopedia.kcsg

import eu.mihosoft.jcsg.CSG

interface ImportedScript {
    /**
     * List of elements exported by the script.
     */
    val exports: Collection<String>

    /**
     * List of all targets defined by the script.
     */
    val targets: Collection<String>

    /**
     * Gets a CSG element defined in the imported script.
     *
     * It does not matter if the element is exported or not.
     * This method will throw if the element cannot be found.
     */
    operator fun get(name: String): CSG
}
