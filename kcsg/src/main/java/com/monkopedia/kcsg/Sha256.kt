package com.monkopedia.kcsg

/**
 * Minimal streaming SHA-256 implementation for multiplatform hashing.
 */
internal class Sha256 {
    private val state = IntArray(8)
    private val schedule = IntArray(64)
    private val buffer = ByteArray(BLOCK_SIZE)
    private var buffered = 0
    private var totalBytes = 0L

    init {
        reset()
    }

    fun update(input: ByteArray) {
        update(input, 0, input.size)
    }

    fun digest(): ByteArray {
        val bitLength = totalBytes * 8
        update(byteArrayOf(0x80.toByte()))

        val zeroPad = if (buffered <= 56) {
            56 - buffered
        } else {
            56 + (BLOCK_SIZE - buffered)
        }
        if (zeroPad > 0) {
            update(ByteArray(zeroPad))
        }

        val lengthBytes = ByteArray(8)
        for (i in 0 until 8) {
            lengthBytes[7 - i] = ((bitLength ushr (i * 8)) and 0xFF).toByte()
        }
        update(lengthBytes)

        val output = ByteArray(32)
        for (i in 0 until state.size) {
            val word = state[i]
            val start = i * 4
            output[start] = (word ushr 24).toByte()
            output[start + 1] = (word ushr 16).toByte()
            output[start + 2] = (word ushr 8).toByte()
            output[start + 3] = word.toByte()
        }
        reset()
        return output
    }

    private fun update(input: ByteArray, offset: Int, length: Int) {
        if (length == 0) {
            return
        }

        totalBytes += length.toLong()
        var index = offset
        var remaining = length

        if (buffered > 0) {
            val toCopy = minOf(BLOCK_SIZE - buffered, remaining)
            input.copyInto(
                destination = buffer,
                destinationOffset = buffered,
                startIndex = index,
                endIndex = index + toCopy,
            )
            buffered += toCopy
            index += toCopy
            remaining -= toCopy
            if (buffered == BLOCK_SIZE) {
                processBlock(buffer, 0)
                buffered = 0
            }
        }

        while (remaining >= BLOCK_SIZE) {
            processBlock(input, index)
            index += BLOCK_SIZE
            remaining -= BLOCK_SIZE
        }

        if (remaining > 0) {
            input.copyInto(
                destination = buffer,
                destinationOffset = 0,
                startIndex = index,
                endIndex = index + remaining,
            )
            buffered = remaining
        }
    }

    private fun processBlock(data: ByteArray, offset: Int) {
        for (i in 0 until 16) {
            val base = offset + i * 4
            schedule[i] = (
                ((data[base].toInt() and 0xFF) shl 24) or
                    ((data[base + 1].toInt() and 0xFF) shl 16) or
                    ((data[base + 2].toInt() and 0xFF) shl 8) or
                    (data[base + 3].toInt() and 0xFF)
                )
        }
        for (i in 16 until 64) {
            val s0 = rotateRight(schedule[i - 15], 7) xor
                rotateRight(schedule[i - 15], 18) xor
                (schedule[i - 15] ushr 3)
            val s1 = rotateRight(schedule[i - 2], 17) xor
                rotateRight(schedule[i - 2], 19) xor
                (schedule[i - 2] ushr 10)
            schedule[i] = schedule[i - 16] + s0 + schedule[i - 7] + s1
        }

        var a = state[0]
        var b = state[1]
        var c = state[2]
        var d = state[3]
        var e = state[4]
        var f = state[5]
        var g = state[6]
        var h = state[7]

        for (i in 0 until 64) {
            val sum1 = rotateRight(e, 6) xor rotateRight(e, 11) xor rotateRight(e, 25)
            val ch = (e and f) xor (e.inv() and g)
            val t1 = h + sum1 + ch + ROUND_CONSTANTS[i] + schedule[i]
            val sum0 = rotateRight(a, 2) xor rotateRight(a, 13) xor rotateRight(a, 22)
            val maj = (a and b) xor (a and c) xor (b and c)
            val t2 = sum0 + maj

            h = g
            g = f
            f = e
            e = d + t1
            d = c
            c = b
            b = a
            a = t1 + t2
        }

        state[0] += a
        state[1] += b
        state[2] += c
        state[3] += d
        state[4] += e
        state[5] += f
        state[6] += g
        state[7] += h
    }

    private fun reset() {
        state[0] = 0x6a09e667
        state[1] = 0xbb67ae85.toInt()
        state[2] = 0x3c6ef372
        state[3] = 0xa54ff53a.toInt()
        state[4] = 0x510e527f
        state[5] = 0x9b05688c.toInt()
        state[6] = 0x1f83d9ab
        state[7] = 0x5be0cd19
        buffered = 0
        totalBytes = 0L
    }

    private fun rotateRight(value: Int, amount: Int): Int {
        return (value ushr amount) or (value shl (32 - amount))
    }

    private companion object {
        private const val BLOCK_SIZE = 64

        private val ROUND_CONSTANTS = intArrayOf(
            0x428a2f98,
            0x71374491,
            0xb5c0fbcf.toInt(),
            0xe9b5dba5.toInt(),
            0x3956c25b,
            0x59f111f1,
            0x923f82a4.toInt(),
            0xab1c5ed5.toInt(),
            0xd807aa98.toInt(),
            0x12835b01,
            0x243185be,
            0x550c7dc3,
            0x72be5d74,
            0x80deb1fe.toInt(),
            0x9bdc06a7.toInt(),
            0xc19bf174.toInt(),
            0xe49b69c1.toInt(),
            0xefbe4786.toInt(),
            0x0fc19dc6,
            0x240ca1cc,
            0x2de92c6f,
            0x4a7484aa,
            0x5cb0a9dc,
            0x76f988da,
            0x983e5152.toInt(),
            0xa831c66d.toInt(),
            0xb00327c8.toInt(),
            0xbf597fc7.toInt(),
            0xc6e00bf3.toInt(),
            0xd5a79147.toInt(),
            0x06ca6351,
            0x14292967,
            0x27b70a85,
            0x2e1b2138,
            0x4d2c6dfc,
            0x53380d13,
            0x650a7354,
            0x766a0abb,
            0x81c2c92e.toInt(),
            0x92722c85.toInt(),
            0xa2bfe8a1.toInt(),
            0xa81a664b.toInt(),
            0xc24b8b70.toInt(),
            0xc76c51a3.toInt(),
            0xd192e819.toInt(),
            0xd6990624.toInt(),
            0xf40e3585.toInt(),
            0x106aa070,
            0x19a4c116,
            0x1e376c08,
            0x2748774c,
            0x34b0bcb5,
            0x391c0cb3,
            0x4ed8aa4a,
            0x5b9cca4f,
            0x682e6ff3,
            0x748f82ee,
            0x78a5636f,
            0x84c87814.toInt(),
            0x8cc70208.toInt(),
            0x90befffa.toInt(),
            0xa4506ceb.toInt(),
            0xbef9a3f7.toInt(),
            0xc67178f2.toInt(),
        )
    }
}
