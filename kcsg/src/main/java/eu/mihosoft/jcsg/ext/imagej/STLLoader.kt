package eu.mihosoft.jcsg.ext.imagej

import eu.mihosoft.vvecmath.Vector3d
import java.io.*
import java.text.ParseException

/**
 * Fork of
 * https://github.com/fiji/fiji/blob/master/src-plugins/3D_Viewer/src/main/java/customnode/STLLoader.java
 *
 * TODO: license unclear
 */
internal class STLLoader  //        /**
//         * Load the specified stl file and returns the result as a hash map, mapping
//         * the object names to the corresponding <code>CustomMesh</code> objects.
//         */
//        public static Map<String, CustomMesh> load(String stlfile)
//                        throws IOException {
//                STLLoader sl = new STLLoader();
//                try {
//                        sl.parse(stlfile);
//                } catch (RuntimeException e) {
//                        System.out.println("error reading " + sl.name);
//                        throw e;
//                }
//                return sl.meshes;
//        }
//
//        private HashMap<String, CustomMesh> meshes;
{
    var line: String? = null
    var `in`: BufferedReader? = null

    // attributes of the currently read mesh
    private var vertices = ArrayList<Vector3d>()
    private val normal = Vector3d.zero().asModifiable() //to be used for file checking
    private var fis: FileInputStream? = null
    private var triangles = 0

    //    private DecimalFormat decimalFormat = new DecimalFormat("0.0E0");
    @Throws(IOException::class)
    fun parse(f: File): ArrayList<Vector3d> {
        vertices.clear()

        // determine if this is a binary or ASCII STL
        // and send to the appropriate parsing method
        // Hypothesis 1: this is an ASCII STL
        val br = BufferedReader(FileReader(f))
        val line = br.readLine()
        val words = line.trim { it <= ' ' }.split("\\s+").toTypedArray()
        if (line.indexOf('\u0000') < 0 && words[0].equals("solid", ignoreCase = true)) {
            println("Looks like an ASCII STL")
            parseAscii(f)
            return vertices
        }

        // Hypothesis 2: this is a binary STL
        val fs = FileInputStream(f)

        // bytes 80, 81, 82 and 83 form a little-endian int
        // that contains the number of triangles
        val buffer = ByteArray(84)
        fs.read(buffer, 0, 84)
        triangles = (buffer[83].toInt() and 0xff shl 24
            or (buffer[82].toInt() and 0xff shl 16) or (buffer[81].toInt() and 0xff shl 8) or (buffer[80].toInt() and 0xff))
        if ((f.length() - 84) / 50 == triangles.toLong()) {
            println("Looks like a binary STL")
            parseBinary(f)
            return vertices
        }
        System.err.println("File is not a valid STL")
        return vertices
    }

    private fun parseAscii(f: File) {
        try {
            `in` = BufferedReader(FileReader(f))
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        }
        vertices = ArrayList()
        try {
            while (`in`!!.readLine().also { line = it } != null) {
                val numbers = line!!.trim { it <= ' ' }.split("\\s+").toTypedArray()
                if (numbers[0] == "vertex") {
                    val x = parseFloat(numbers[1])
                    val y = parseFloat(numbers[2])
                    val z = parseFloat(numbers[3])
                    val vertex = Vector3d.xyz(x.toDouble(), y.toDouble(), z.toDouble())
                    vertices.add(vertex)
                } else if (numbers[0] == "facet" && numbers[1] == "normal") {
                    normal.x = parseFloat(numbers[2]).toDouble()
                    normal.y = parseFloat(numbers[3]).toDouble()
                    normal.z = parseFloat(numbers[4]).toDouble()
                }
            }
            `in`!!.close()
        } catch (e: IOException) {
            e.printStackTrace()
        } catch (e: ParseException) {
            e.printStackTrace()
        }
    }

    private fun parseBinary(f: File) {
        vertices = ArrayList()
        try {
            fis = FileInputStream(f)
            for (h in 0..83) {
                fis!!.read() // skip the header bytes
            }
            for (t in 0 until triangles) {
                val tri = ByteArray(50)
                for (tb in 0..49) {
                    tri[tb] = fis!!.read().toByte()
                }
                normal.x = leBytesToFloat(tri[0], tri[1], tri[2], tri[3]).toDouble()
                normal.y = leBytesToFloat(tri[4], tri[5], tri[6], tri[7]).toDouble()
                normal.z = leBytesToFloat(tri[8], tri[9], tri[10], tri[11]).toDouble()
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
                    vertices.add(p)
                }
            }
            fis!!.close()
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    //    private float parseFloat(String string) throws ParseException {
    //        //E+05 -> E05, e+05 -> E05
    //        string = string.replaceFirst("[eE]\\+", "E");
    //        //E-05 -> E-05, e-05 -> E-05
    //        string = string.replaceFirst("e\\-", "E-");
    //        return decimalFormat.parse(string).floatValue();
    //    }
    @Throws(ParseException::class)
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