package com.monkopedia.kcsg

import kotlin.test.assertEquals
import kotlin.test.Test

/**
 * Expected digests are independent of kcsg: the NIST values are quoted from FIPS 180-2 Appendix B
 * and the FIPS 180 / NIST CSRC example set, and the remaining values were produced with
 * coreutils `sha256sum` (e.g. `head -c 55 /dev/zero | tr '\0' a | sha256sum`).
 */
class Sha256Test {
    @Test
    fun knownVectorsMatch() {
        assertEquals(
            "e3b0c44298fc1c149afbf4c8996fb924" +
                "27ae41e4649b934ca495991b7852b855",
            sha256(byteArrayOf()),
        )
        assertEquals(
            "ba7816bf8f01cfea414140de5dae2223" +
                "b00361a396177a9cb410ff61f20015ad",
            sha256("abc".encodeToByteArray()),
        )
    }

    @Test
    fun nist448BitMessage() {
        // FIPS 180-2 Appendix B.2: 56 bytes, so the padding spills into a second block.
        assertEquals(
            "248d6a61d20638b8e5c026930c3e6039" +
                "a33ce45964ff2167f6ecedd419db06c1",
            sha256(NIST_448.encodeToByteArray()),
        )
    }

    @Test
    fun nist896BitMessage() {
        // NIST CSRC SHA-256 example: 112 bytes, one full block plus a partial one.
        assertEquals(
            "cf5b16a778af8380036ce59e7b049237" +
                "0b249b11e8f07a51afac45037afee9d1",
            sha256(NIST_896.encodeToByteArray()),
        )
    }

    @Test
    fun nistMillionA() {
        // FIPS 180-2 Appendix B.3: one million repetitions of 'a'. The bit length (8,000,000)
        // needs three non-zero bytes in the length field.
        val expected = "cdc76e5c9914fb9281a1c7e284d73e67" +
            "f1809a48a497200e046d39ccc7112cd0"
        val input = repeatA(1_000_000)
        assertEquals(expected, sha256(input))
        assertEquals(expected, sha256Chunked(input, 1000))
        assertEquals(expected, sha256Chunked(input, 37))
    }

    @Test
    fun paddingBoundaries() {
        // 55: padding fits in one block. 56/57/63: padding needs a second block.
        // 64: exactly one block. 65+: block carry. 119/120/128 repeat the pattern one block on.
        val expected = mapOf(
            55 to "9f4390f8d30c2dd92ec9f095b65e2b9ae9b0a925a5258e241c9f1e910f734318",
            56 to "b35439a4ac6f0948b6d6f9e3c6af0f5f590ce20f1bde7090ef7970686ec6738a",
            57 to "f13b2d724659eb3bf47f2dd6af1accc87b81f09f59f2b75e5c0bed6589dfe8c6",
            63 to "7d3e74a05d7db15bce4ad9ec0658ea98e3f06eeecf16b4c6fff2da457ddc2f34",
            64 to "ffe054fe7ae0cb6dc65c3af9b61d5209f439851db43d0ba5997337df154668eb",
            65 to "635361c48bb9eab14198e76ea8ab7f1a41685d6ad62aa9146d301d4f17eb0ae0",
            119 to "31eba51c313a5c08226adf18d4a359cfdfd8d2e816b13f4af952f7ea6584dcfb",
            120 to "2f3d335432c70b580af0e8e1b3674a7c020d683aa5f73aaaedfdc55af904c21c",
            128 to "6836cf13bac400e9105071cd6af47084dfacad4e5e302c94bfed24e013afb73e"
        )
        for ((length, digest) in expected) {
            val input = repeatA(length)
            assertEquals(digest, sha256(input), "single update, length $length")
            for (chunk in intArrayOf(1, 7, 30, 63, 64)) {
                assertEquals(
                    digest,
                    sha256Chunked(input, chunk),
                    "chunked by $chunk, length $length"
                )
            }
        }
    }

    @Test
    fun largeNonUniformInput() {
        // 1 MiB + 37 bytes of (i % 251), so no two adjacent blocks are identical.
        // python3 -c "import sys; sys.stdout.buffer.write(bytes(i%251 for i in range(1048576+37)))"
        //     | sha256sum
        val expected = "1873887da67560ac533b212299c64f42" +
            "421d197e67f687e0ab10c53f426f4465"
        val input = ByteArray(1_048_576 + 37) { (it % 251).toByte() }
        assertEquals(expected, sha256(input))
        assertEquals(expected, sha256Chunked(input, 4099))
    }

    @Test
    fun incrementalUpdatesMatchPublishedDigest() {
        // Split at non-block-aligned boundaries so the buffered carry path is exercised.
        val bytes = NIST_896.encodeToByteArray()
        val digest = Sha256().apply {
            update(bytes.copyOfRange(0, 30))
            update(bytes.copyOfRange(30, 56))
            update(bytes.copyOfRange(56, bytes.size))
        }.digest().toHexString()
        assertEquals(
            "cf5b16a778af8380036ce59e7b049237" +
                "0b249b11e8f07a51afac45037afee9d1",
            digest,
        )
    }

    @Test
    fun digestResetsState() {
        val hasher = Sha256()
        hasher.update(repeatA(100))
        hasher.digest()
        hasher.update("abc".encodeToByteArray())
        assertEquals(
            "ba7816bf8f01cfea414140de5dae2223" +
                "b00361a396177a9cb410ff61f20015ad",
            hasher.digest().toHexString(),
        )
    }

    private fun repeatA(length: Int): ByteArray = ByteArray(length) { 'a'.code.toByte() }

    private fun sha256(input: ByteArray): String =
        Sha256().apply { update(input) }.digest().toHexString()

    private fun sha256Chunked(input: ByteArray, chunk: Int): String {
        val hasher = Sha256()
        var start = 0
        while (start < input.size) {
            val end = minOf(start + chunk, input.size)
            hasher.update(input.copyOfRange(start, end))
            start = end
        }
        return hasher.digest().toHexString()
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

    private companion object {
        const val NIST_448 = "abcdbcdecdefdefgefghfghighijhijkijkljklmklmnlmnomnopnopq"
        const val NIST_896 = "abcdefghbcdefghicdefghijdefghijkefghijklfghijklmghijklmnhijklmno" +
            "ijklmnopjklmnopqklmnopqrlmnopqrsmnopqrstnopqrstu"
    }
}
