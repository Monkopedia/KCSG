package com.monkopedia.kcsg.ext.imagej

import com.monkopedia.kcsg.Vector3d
import kotlinx.io.buffered
import kotlinx.io.files.FileSystem
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import kotlinx.io.readLine
import kotlinx.io.Source
import org.slf4j.LoggerFactory

/**
 * Fork of
 * https://github.com/fiji/fiji/blob/master/src-plugins/3D_Viewer/src/main/java/customnode/STLLoader.java
 *
 * TODO: license unclear
 */
internal object STLLoader {

    private val logger = LoggerFactory.getLogger("KCSG.STLLoader")

    fun parse(path: Path, fileSystem: FileSystem = SystemFileSystem): List<Vector3d> {
        return parse(
            sourceFactory = {
                fileSystem.source(path).buffered()
            },
            length = { fileSystem.metadataOrNull(path)?.size ?: 0L },
        )
    }

    fun parse(sourceFactory: () -> Source, length: () -> Long): List<Vector3d> {
        // determine if this is a binary or ASCII STL
        // and send to the appropriate parsing method
        // Hypothesis 1: this is an ASCII STL
        val line = sourceFactory().use { source -> source.readLine() } ?: return ArrayList()
        val words = line.trim().split(Regex("\\s+"))
        if (line.indexOf('\u0000') < 0 && words[0].equals("solid", ignoreCase = true)) {
            logger.info("Looks like an ASCII STL")
            return parseAscii(sourceFactory)
        }

        // Hypothesis 2: this is a binary STL
        val buffer = ByteArray(84)
        sourceFactory().use { fs ->
            // bytes 80, 81, 82 and 83 form a little-endian int
            // that contains the number of triangles
            if (!fs.readFully(buffer)) return ArrayList()
        }
        val triangles = (
            buffer[83].toInt() and 0xff shl 24
                or (buffer[82].toInt() and 0xff shl 16) or (buffer[81].toInt() and 0xff shl 8) or (buffer[80].toInt() and 0xff)
            )
        if ((length() - 84) / 50 == triangles.toLong()) {
            logger.info("Looks like a binary STL")
            return parseBinary(sourceFactory, triangles)
        }
        logger.error("File is not a valid STL")
        return ArrayList()
    }

    private fun parseAscii(sourceFactory: () -> Source): List<Vector3d> = buildList {
        sourceFactory().use { source ->
            while (true) {
                val line = source.readLine() ?: break
                val numbers = line.trim().split(Regex("\\s+"))
                if (numbers.isEmpty()) continue
                if (numbers[0] == "vertex" && numbers.size >= 4) {
                    val x = parseFloat(numbers[1])
                    val y = parseFloat(numbers[2])
                    val z = parseFloat(numbers[3])
                    val vertex = Vector3d.xyz(x.toDouble(), y.toDouble(), z.toDouble())
                    add(vertex)
                } else if (numbers[0] == "facet" && numbers.getOrNull(1) == "normal" && numbers.size >= 5) {
                    parseFloat(numbers[2]).toDouble()
                    parseFloat(numbers[3]).toDouble()
                    parseFloat(numbers[4]).toDouble()
                }
            }
        }
    }

    private fun parseBinary(sourceFactory: () -> Source, triangles: Int): List<Vector3d> =
        buildList {
            sourceFactory().use { fis ->
                for (h in 0..83) {
                    fis.readByte() // skip the header bytes
                }
                for (t in 0 until triangles) {
                    val tri = ByteArray(50)
                    if (!fis.readFully(tri)) {
                        return@use
                    }
                    leBytesToFloat(tri[0], tri[1], tri[2], tri[3]).toDouble()
                    leBytesToFloat(tri[4], tri[5], tri[6], tri[7]).toDouble()
                    leBytesToFloat(tri[8], tri[9], tri[10], tri[11]).toDouble()
                    for (i in 0..2) {
                        val j = i * 12 + 12
                        val px = leBytesToFloat(tri[j], tri[j + 1], tri[j + 2], tri[j + 3])
                        val py = leBytesToFloat(tri[j + 4], tri[j + 5], tri[j + 6], tri[j + 7])
                        val pz = leBytesToFloat(tri[j + 8], tri[j + 9], tri[j + 10], tri[j + 11])
                        val p = Vector3d.xyz(px.toDouble(), py.toDouble(), pz.toDouble())
                        add(p)
                    }
                }
            }
        }

    private fun parseFloat(string: String): Float {
        return string.toFloat()
    }

    private fun leBytesToFloat(b0: Byte, b1: Byte, b2: Byte, b3: Byte): Float {
        val bits = ((b3.toInt() and 0xff) shl 24) or
            ((b2.toInt() and 0xff) shl 16) or
            ((b1.toInt() and 0xff) shl 8) or
            (b0.toInt() and 0xff)
        return Float.fromBits(bits)
    }

    private fun Source.readFully(buffer: ByteArray): Boolean {
        var offset = 0
        while (offset < buffer.size) {
            val read = readAtMostTo(buffer, offset, buffer.size - offset)
            if (read <= 0) {
                return false
            }
            offset += read
        }
        return true
    }
}
