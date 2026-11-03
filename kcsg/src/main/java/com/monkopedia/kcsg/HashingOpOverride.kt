package com.monkopedia.kcsg

import kotlinx.io.Source
import kotlinx.io.buffered
import kotlinx.io.files.FileSystem
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import kotlinx.io.readByteArray

/**
 * An [OpOverride] that, instead of building geometry, folds every operation and its arguments into
 * a SHA-256 digest. The digest ([hash]) is the cache key `csgs` uses for its on-disk STL cache, so
 * two builds may share a key only if they would produce the same solid.
 *
 * Keying: the digest covers a format-version tag followed by one record per call
 * ([operation], [bounds], [double], [file], [source]). Each record is the call kind, the
 * operation name and the argument count, then each argument as a tagged value. Every
 * variable-length field (tags, strings, stream/file contents, list sizes) is length-prefixed so
 * the byte stream is unambiguous: no two different call sequences serialise to the same bytes.
 *
 * A [CSG] argument is encoded one of two ways:
 * - a placeholder returned by an earlier call on this instance is written as a reference to that
 *   call's slot. The placeholder has no geometry of its own; the slot stands for the result of the
 *   earlier, already-hashed call, so the reference is fully determined by the preceding records.
 * - any other CSG (built with real geometry outside the hashing pass, e.g. captured by a script)
 *   is written as its full polygon/vertex content the first time it is seen, then assigned a slot
 *   so later uses of the same instance are a cheap reference. Hashing this content is linear in
 *   the mesh size, which is small next to the boolean work a cache hit avoids.
 *
 * Invalidation: there is no invalidation of entries; a changed input changes the key, so the old
 * entry is simply never looked up again. Bump [FORMAT_VERSION] whenever the encoding changes so
 * keys from an older encoding can never be matched by the new one.
 */
class HashingOpOverride : OpOverride {
    private val sha256 = Sha256()
    private val csgLookup = mutableListOf<Pair<CSG, Int>>()
    private var count = 0
    private val fileSystem: FileSystem = SystemFileSystem

    init {
        updateHash(bytes { writeTag(FORMAT_VERSION) })
    }

    fun hash(): String {
        return sha256.digest().toHexString()
    }

    private fun updateHash(bytes: ByteArray) {
        sha256.update(bytes)
    }

    private inline fun bytes(write: HashBytesBuilder.() -> Unit): ByteArray {
        return HashBytesBuilder().apply(write).toByteArray()
    }

    private fun hash(any: Any?) {
        val bytes = when (any) {
            null -> bytes { writeTag("null") }
            is String -> bytes {
                writeTag("str")
                writeFramed(any.encodeToByteArray())
            }
            is CSG -> {
                hashCsg(any)
                return
            }
            is Transform -> bytes {
                writeTag("tr")
                write(any, transformBuffer)
            }
            is Vector3d -> bytes {
                writeTag("v3d")
                write(any)
            }
            is Cube -> bytes {
                writeTag("cub")
                write(any.center)
                write(any.dimensions)
                write(if (any.centered) 1 else 0)
            }
            is Cylinder -> bytes {
                writeTag("cyl")
                write(any.start)
                write(any.end)
                write(any.startRadius)
                write(any.endRadius)
                write(any.numSlices)
            }
            is Polyhedron -> bytes {
                writeTag("plh")
                write(any.points.size)
                any.points.forEach { write(it) }
                write(any.faces.size)
                any.faces.forEach { face ->
                    write(face.size)
                    face.forEach { write(it) }
                }
            }
            is RoundedCube -> bytes {
                writeTag("rcb")
                write(any.center)
                write(any.dimensions)
                write(any.cornerRadius)
                write(any.resolution)
                write(if (any.centered) 1 else 0)
            }
            is Sphere -> bytes {
                writeTag("sph")
                write(any.center)
                write(any.radius)
                write(any.numSlices)
                write(any.numStacks)
            }
            is Source -> bytes {
                writeTag("is")
                writeFramed(any.readByteArray())
            }
            is Bounds -> bytes {
                writeTag("bnd")
                write(any.min)
                write(any.max)
            }
            is Boolean -> bytes {
                writeTag("bool")
                write(if (any) 1 else 0)
            }
            is Polygon -> bytes {
                writeTag("poly")
                write(any)
            }
            is UnityModifier -> bytes {
                writeTag("wf:unity")
            }
            is XModifier -> bytes {
                writeTag("wf:x")
                write(if (any.centered) 1 else 0)
            }
            is YModifier -> bytes {
                writeTag("wf:y")
                write(if (any.centered) 1 else 0)
            }
            is ZModifier -> bytes {
                writeTag("wf:z")
                write(if (any.centered) 1 else 0)
            }
            is WeightFunction -> error(
                "Cannot compute a cache hash for custom WeightFunction " +
                    "${any::class.simpleName ?: "<anonymous>"}; only the built-in modifiers " +
                    "(XModifier/YModifier/ZModifier/UnityModifier) are hashable. For builds using a " +
                    "custom weighted() function, disable caching via csg(allowCaching = false)."
            )
            else -> error(
                "HashingOpOverride has no hash encoding for type " +
                    "${any::class.simpleName ?: any::class}. Add an explicit case — a silent " +
                    "hashCode() fallback previously caused stale-cache collisions (see Cube.centered, Polygon)."
            )
        }
        updateHash(bytes)
    }

    private val transformBuffer = DoubleArray(16)

    private fun hashCsg(csg: CSG) {
        val slot = csgLookup.find { it.first === csg }?.second
        if (slot != null) {
            updateHash(
                bytes {
                    writeTag("csg:ref")
                    write(slot)
                }
            )
            return
        }
        val polygons = csg.polygons
        updateHash(
            bytes {
                writeTag("csg:geo")
                write(polygons.size)
            }
        )
        polygons.forEach { polygon -> updateHash(bytes { write(polygon) }) }
        placeCsg(csg)
    }

    private fun hashCall(kind: String, name: String, args: Array<out Any?>) {
        updateHash(
            bytes {
                writeTag(kind)
                write(args.size)
            }
        )
        hash(name)
        args.forEach { hash(it) }
    }

    private fun createCSG(): CSG {
        CSG.withOverride(null) {
            return CSG.fromPolygons().also {
                placeCsg(it)
            }
        }
    }

    private fun placeCsg(csg: CSG): Int {
        return csgLookup.size.also {
            csgLookup.add(csg to it)
        }
    }

    private fun createVector(): Vector3d {
        return Vector3d(count++.toDouble(), count++.toDouble(), count++.toDouble())
    }

    override fun operation(s: String, vararg csg: Any?): CSG {
        hashCall("call:op", s, csg)
        return createCSG()
    }

    override fun bounds(s: String, vararg csg: Any?): Bounds {
        hashCall("call:bounds", s, csg)
        return Bounds(createVector(), createVector())
    }

    override fun double(s: String, vararg csg: Any?): Double {
        hashCall("call:double", s, csg)
        return count++.toDouble()
    }

    override fun file(path: Path): CSG {
        val resolvedPath = runCatching { fileSystem.resolve(path).toString() }
            .getOrElse { path.toString() }
        updateHash(bytes { writeTag("call:file") })
        if (fileSystem.metadataOrNull(path) != null) {
            hash(resolvedPath)
            fileSystem.source(path).buffered().use {
                hash(it)
            }
        } else {
            hash(resolvedPath)
            hash(null)
        }
        return createCSG()
    }

    override fun source(sourceFactory: () -> Source): CSG {
        updateHash(bytes { writeTag("call:source") })
        sourceFactory().use {
            hash(it)
        }
        return createCSG()
    }

    private companion object {
        /** Leads every digest; bump when the encoding changes (see class doc). */
        const val FORMAT_VERSION = "kcsg-hash-v2"
    }
}

private fun ByteArray.toHexString(): String {
    val digits = "0123456789abcdef"
    val builder = StringBuilder(size * 2)
    for (byte in this) {
        val value = byte.toInt() and 0xFF
        builder.append(digits[value ushr 4])
        builder.append(digits[value and 0x0F])
    }
    return builder.toString()
}

private class HashBytesBuilder {
    private var data = ByteArray(64)
    private var size = 0

    fun writeTag(tag: String) {
        writeFramed(tag.encodeToByteArray())
    }

    /** Writes [bytes] preceded by their length, so adjacent fields cannot run into each other. */
    fun writeFramed(bytes: ByteArray) {
        write(bytes.size)
        writeBytes(bytes)
    }

    fun write(polygon: Polygon) {
        write(polygon.vertices.size)
        polygon.vertices.forEach {
            write(it.pos)
            write(it.normal)
            write(it.weight)
        }
    }

    fun writeBytes(bytes: ByteArray) {
        ensureCapacity(bytes.size)
        bytes.copyInto(
            destination = data,
            destinationOffset = size,
            startIndex = 0,
            endIndex = bytes.size,
        )
        size += bytes.size
    }

    fun write(value: Transform, transformBuffer: DoubleArray) {
        value.to(transformBuffer).forEach {
            write(it)
        }
    }

    fun write(value: Vector3d) {
        write(value.x)
        write(value.y)
        write(value.z)
    }

    fun write(value: Double) {
        write(value.toRawBits())
    }

    fun write(value: Long) {
        ensureCapacity(8)
        data[size++] = (value and 0xFF).toByte()
        data[size++] = ((value shr 8) and 0xFF).toByte()
        data[size++] = ((value shr 16) and 0xFF).toByte()
        data[size++] = ((value shr 24) and 0xFF).toByte()
        data[size++] = ((value shr 32) and 0xFF).toByte()
        data[size++] = ((value shr 40) and 0xFF).toByte()
        data[size++] = ((value shr 48) and 0xFF).toByte()
        data[size++] = ((value shr 56) and 0xFF).toByte()
    }

    fun write(value: Int) {
        ensureCapacity(4)
        data[size++] = (value and 0xFF).toByte()
        data[size++] = ((value shr 8) and 0xFF).toByte()
        data[size++] = ((value shr 16) and 0xFF).toByte()
        data[size++] = ((value shr 24) and 0xFF).toByte()
    }

    fun toByteArray(): ByteArray {
        return data.copyOf(size)
    }

    private fun ensureCapacity(additional: Int) {
        val required = size + additional
        if (required <= data.size) {
            return
        }
        var newSize = data.size
        while (newSize < required) {
            newSize *= 2
        }
        data = data.copyOf(newSize)
    }
}
