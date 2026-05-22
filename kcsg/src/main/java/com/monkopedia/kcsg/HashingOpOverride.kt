package com.monkopedia.kcsg

import kotlinx.io.Source
import kotlinx.io.buffered
import kotlinx.io.files.FileSystem
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import kotlinx.io.readByteArray

class HashingOpOverride : OpOverride {
    private val sha256 = Sha256()
    private val csgLookup = mutableListOf<Pair<CSG, Int>>()
    private var count = 0
    private val fileSystem: FileSystem = SystemFileSystem

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
            null -> byteArrayOf(0)
            is String -> {
                any.encodeToByteArray()
            }
            is CSG -> bytes {
                writeTag("csg")
                write(csgLookup.find { it.first === any }?.second ?: placeCsg(any))
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
                any.points.forEach { write(it) }
                any.faces.forEach { list -> list.forEach(::write) }
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
                writeBytes(any.readByteArray())
            }
            else -> bytes {
                writeTag("else")
                write(any.hashCode())
            }
        }
        updateHash(bytes)
    }

    private val transformBuffer = DoubleArray(16)

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
        hash(s)
        csg.forEach { hash(it) }
        return createCSG()
    }

    override fun bounds(s: String, vararg csg: Any?): Bounds {
        hash(s)
        csg.forEach { hash(it) }
        return Bounds(createVector(), createVector())
    }

    override fun double(s: String, vararg csg: Any?): Double {
        hash(s)
        csg.forEach { hash(it) }
        return count++.toDouble()
    }

    override fun file(path: Path): CSG {
        val resolvedPath = runCatching { fileSystem.resolve(path).toString() }
            .getOrElse { path.toString() }
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
        sourceFactory().use {
            hash(it)
        }
        return createCSG()
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
        writeBytes(tag.encodeToByteArray())
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
