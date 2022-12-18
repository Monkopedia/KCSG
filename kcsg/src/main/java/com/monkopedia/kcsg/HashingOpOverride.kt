package com.monkopedia.kcsg

import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.nio.file.Path
import java.security.MessageDigest
import kotlin.io.path.absolutePathString
import kotlin.io.path.exists
import kotlin.io.path.inputStream

class HashingOpOverride : OpOverride {
    private val md = MessageDigest.getInstance("SHA-256")
    private val csgLookup = mutableListOf<Pair<CSG, Int>>()
    private var count = 0

    fun hash(): String {
        return md.digest().fold("") { str, it -> str + "%02x".format(it) }
    }

    private fun updateHash(bytes: ByteArray) {
        md.update(bytes)
    }

    private fun hash(any: Any?) {
        val bytes = when (any) {
            null -> byteArrayOf(0)
            is String -> {
                any.toByteArray()
            }
            is CSG -> ByteArrayOutputStream().use {
                it.writeBytes("csg".toByteArray())
                it.write(csgLookup.find { it.first === any }?.second ?: placeCsg(any))
                it.toByteArray()
            }
            is Transform -> ByteArrayOutputStream().use {
                it.writeBytes("tr".toByteArray())
                it.write(any)
                it.toByteArray()
            }
            is Vector3d -> ByteArrayOutputStream().use {
                it.writeBytes("v3d".toByteArray())
                it.write(any.x)
                it.write(any.y)
                it.write(any.z)
                it.toByteArray()
            }
            is Cube -> ByteArrayOutputStream().use {
                it.writeBytes("cub".toByteArray())
                it.write(any.center)
                it.write(any.dimensions)
                it.toByteArray()
            }
            is Cylinder -> ByteArrayOutputStream().use {
                it.writeBytes("cyl".toByteArray())
                it.write(any.start)
                it.write(any.end)
                it.write(any.startRadius)
                it.write(any.endRadius)
                it.write(any.numSlices)
                it.toByteArray()
            }
            is Polyhedron -> ByteArrayOutputStream().use { os ->
                os.writeBytes("plh".toByteArray())
                any.points.forEach { os.write(it) }
                any.faces.forEach { list -> list.forEach { os.write(it) } }
                os.toByteArray()
            }
            is RoundedCube -> ByteArrayOutputStream().use {
                it.writeBytes("rcb".toByteArray())
                it.write(any.center)
                it.write(any.dimensions)
                it.write(any.cornerRadius)
                it.write(any.resolution)
                it.write(if (any.centered) 1 else 0)
                it.toByteArray()
            }
            is Sphere -> ByteArrayOutputStream().use {
                it.writeBytes("sph".toByteArray())
                it.write(any.center)
                it.write(any.radius)
                it.write(any.numSlices)
                it.write(any.numStacks)
                it.toByteArray()
            }
            is InputStream -> ByteArrayOutputStream().use {
                it.writeBytes("is".toByteArray())
                any.copyTo(it)
                it.toByteArray()
            }
            else -> ByteArrayOutputStream().use {
                it.writeBytes("else".toByteArray())
                it.write(any.hashCode())
                it.toByteArray()
            }
        }
        updateHash(bytes)
    }

    private val transformBuffer = DoubleArray(16)

    private fun ByteArrayOutputStream.write(transform: Transform) {
        transform.to(transformBuffer).forEach {
            write(it)
        }
    }

    private fun ByteArrayOutputStream.write(value: Vector3d) {
        write(value.x)
        write(value.y)
        write(value.z)
    }

    private fun ByteArrayOutputStream.write(value: Double) {
        write(java.lang.Double.doubleToRawLongBits(value))
    }

    private fun ByteArrayOutputStream.write(value: Long) {
        write((value and 0xff).toByte().toInt())
        write(((value shr 8) and 0xff).toByte().toInt())
        write(((value shr 16) and 0xff).toByte().toInt())
        write(((value shr 24) and 0xff).toByte().toInt())
        write(((value shr 32) and 0xff).toByte().toInt())
        write(((value shr 40) and 0xff).toByte().toInt())
        write(((value shr 48) and 0xff).toByte().toInt())
        write(((value shr 56) and 0xff).toByte().toInt())
    }

    private fun ByteArrayOutputStream.write(value: Int) {
        write((value and 0xff).toByte().toInt())
        write(((value shr 8) and 0xff).toByte().toInt())
        write(((value shr 16) and 0xff).toByte().toInt())
        write(((value shr 24) and 0xff).toByte().toInt())
    }

    private fun createCSG(): CSG {
        try {
            CSG.setOverride(null)
            return CSG.fromPolygons().also {
                placeCsg(it)
            }
        } finally {
            CSG.setOverride(this)
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
        if (path.exists()) {
            hash(path.absolutePathString())
            hash(path.inputStream())
        } else {
            hash(path.absolutePathString())
            hash(null)
        }
        return createCSG()
    }

    override fun inputStream(inputStreamFactory: () -> InputStream): CSG {
        hash(inputStreamFactory())
        return createCSG()
    }
}
