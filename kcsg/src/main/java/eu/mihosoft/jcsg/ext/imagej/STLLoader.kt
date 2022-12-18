package eu.mihosoft.jcsg.ext.imagej

import eu.mihosoft.jcsg.ext.vvecmath.Vector3d
import java.io.File
import java.io.FileInputStream
import java.io.InputStream

/**
 * Fork of
 * https://github.com/fiji/fiji/blob/master/src-plugins/3D_Viewer/src/main/java/customnode/STLLoader.java
 *
 * TODO: license unclear
 */
internal class STLLoader {

    fun parse(f: File): List<Vector3d> {
        return parse({ FileInputStream(f) }, { f.length() })
    }

    fun parse(inputStreamFactory: () -> InputStream, length: () -> Long): List<Vector3d> {
        // determine if this is a binary or ASCII STL
        // and send to the appropriate parsing method
        // Hypothesis 1: this is an ASCII STL
        val br = inputStreamFactory().bufferedReader()
        val line = br.readLine()
        val words = line.trim().split(Regex("\\s+"))
        if (line.indexOf('\u0000') < 0 && words[0].equals("solid", ignoreCase = true)) {
            println("Looks like an ASCII STL")
            return parseAscii(inputStreamFactory)
        }

        // Hypothesis 2: this is a binary STL
        val fs = inputStreamFactory()

        // bytes 80, 81, 82 and 83 form a little-endian int
        // that contains the number of triangles
        val buffer = ByteArray(84)
        fs.read(buffer, 0, 84)
        val triangles = (
            buffer[83].toInt() and 0xff shl 24
                or (buffer[82].toInt() and 0xff shl 16) or (buffer[81].toInt() and 0xff shl 8) or (buffer[80].toInt() and 0xff)
            )
        if ((length() - 84) / 50 == triangles.toLong()) {
            println("Looks like a binary STL")
            return parseBinary(inputStreamFactory, triangles)
        }
        System.err.println("File is not a valid STL")
        return ArrayList()
    }

    private fun parseAscii(inputStreamFactory: () -> InputStream): List<Vector3d> = buildList {
        inputStreamFactory().bufferedReader().useLines { lines ->
            lines.forEach { line ->
                val numbers = line.trim().split(Regex("\\s+")).toTypedArray()
                if (numbers[0] == "vertex") {
                    val x = parseFloat(numbers[1])
                    val y = parseFloat(numbers[2])
                    val z = parseFloat(numbers[3])
                    val vertex = Vector3d.xyz(x.toDouble(), y.toDouble(), z.toDouble())
                    add(vertex)
                } else if (numbers[0] == "facet" && numbers[1] == "normal") {
                    parseFloat(numbers[2]).toDouble()
                    parseFloat(numbers[3]).toDouble()
                    parseFloat(numbers[4]).toDouble()
                }
            }
        }
    }

    private fun parseBinary(inputStreamFactory: () -> InputStream, triangles: Int): List<Vector3d> = buildList {
        inputStreamFactory().use { fis ->
            for (h in 0..83) {
                fis.read() // skip the header bytes
            }
            for (t in 0 until triangles) {
                val tri = ByteArray(50)
                for (tb in 0..49) {
                    tri[tb] = fis.read().toByte()
                }
                leBytesToFloat(tri[0], tri[1], tri[2], tri[3]).toDouble()
                leBytesToFloat(tri[4], tri[5], tri[6], tri[7]).toDouble()
                leBytesToFloat(tri[8], tri[9], tri[10], tri[11]).toDouble()
                for (i in 0..2) {
                    val j = i * 12 + 12
                    val px = leBytesToFloat(
                        tri[j], tri[j + 1], tri[j + 2],
                        tri[j + 3]
                    )
                    val py = leBytesToFloat(
                        tri[j + 4], tri[j + 5],
                        tri[j + 6], tri[j + 7]
                    )
                    val pz = leBytesToFloat(
                        tri[j + 8], tri[j + 9],
                        tri[j + 10], tri[j + 11]
                    )
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
        return java.lang.Float.intBitsToFloat(
            b3.toInt() and 0xff shl 24 or (b2.toInt() and 0xff shl 16)
                or (b1.toInt() and 0xff shl 8) or (b0.toInt() and 0xff)
        )
    }
}
