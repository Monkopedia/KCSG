package com.monkopedia.kcsg

import org.junit.Assert.assertEquals
import org.junit.Test

class Sha256Test {
    @Test
    fun knownVectorsMatch() {
        val emptyDigest = Sha256().apply {
            update(byteArrayOf())
        }.digest()
        assertEquals(
            "e3b0c44298fc1c149afbf4c8996fb924" +
                "27ae41e4649b934ca495991b7852b855",
            emptyDigest.toHexString(),
        )

        val abcDigest = Sha256().apply {
            update("abc".encodeToByteArray())
        }.digest()
        assertEquals(
            "ba7816bf8f01cfea414140de5dae2223" +
                "b00361a396177a9cb410ff61f20015ad",
            abcDigest.toHexString(),
        )
    }

    @Test
    fun incrementalUpdatesMatchSingleUpdate() {
        val single = Sha256().apply {
            update("abcdef".encodeToByteArray())
        }.digest()

        val split = Sha256().apply {
            update("abc".encodeToByteArray())
            update("def".encodeToByteArray())
        }.digest()

        assertEquals(single.toHexString(), split.toHexString())
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
}
