package com.monkopedia.kcsg

import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.log10

/**
 * Renders this double as `java.lang.Double.toString` does, identically on every Kotlin target.
 *
 * `Double.toString()` is target-dependent (#41): JS prints ECMAScript `Number::toString`
 * (`1`, `-0.0` as `0`, `1e-7`, `1e+21`), Kotlin/Wasm prints `10000000.0` and `1e-7` with
 * digits that are not always the shortest or closest, and Kotlin/Native prints Java's layout
 * but differs from it on some subnormals. Mesh text (STL, OBJ, MTL) is built from these
 * strings, so it has to come from one definition.
 *
 * That definition is the JDK 19+ `Double.toString` specification: the shortest decimal that
 * rounds to the value, the closest such decimal when there are several, and Java's layout
 * (plain with at least one fractional digit for `1e-3 <= |x| < 1e7`, otherwise `d.dddE±n`).
 * On the JVM this delegates to `Double.toString` itself, so JVM output is unchanged. JDK 8-18
 * deviate from the specification only for subnormals and for some values of magnitude 1e16
 * and above, where they can print a non-shortest decimal (`1.0E23` as `9.999999999999999E22`).
 */
internal fun Double.toJvmString(): String {
    if (isNaN()) return "NaN"
    if (isInfinite()) return if (this > 0) "Infinity" else "-Infinity"
    if (this == 0.0) return if (1.0 / this < 0) "-0.0" else "0.0"
    return platformJavaString(this)
}

/** Java `Double.toString` of a finite non-zero [value]; see [toJvmString]. */
internal expect fun platformJavaString(value: Double): String

private const val PLAIN_LOWER = 1e-3
private const val PLAIN_UPPER = 1e7

/**
 * Lays out the decimal `0.digits * 10^point` (no leading or trailing zeros in [digits]) as
 * Java does for a double of sign [negative] and magnitude [magnitude].
 */
private fun layoutJava(negative: Boolean, magnitude: Double, digits: String, point: Int): String =
    buildString(digits.length + 8) {
        if (negative) append('-')
        if (magnitude >= PLAIN_LOWER && magnitude < PLAIN_UPPER) {
            when {
                point <= 0 -> {
                    append("0.")
                    repeat(-point) { append('0') }
                    append(digits)
                }
                point >= digits.length -> {
                    append(digits)
                    repeat(point - digits.length) { append('0') }
                    append(".0")
                }
                else -> append(digits, 0, point).append('.').append(digits, point, digits.length)
            }
        } else {
            append(digits[0]).append('.')
            if (digits.length > 1) append(digits, 1, digits.length) else append('0')
            append('E').append(point - 1)
        }
    }

/**
 * Re-lays out [rendered], an ECMAScript `Number::toString` rendering of the finite non-zero
 * [value] (`1`, `0.001`, `1e-7`, `1.5e+21`), in Java's layout. ECMAScript already specifies
 * the shortest-then-closest digits, so only the layout and the two-digit subnormal rule of
 * [closerTwoDigitDecimal] differ from Java.
 */
internal fun formatJavaFromEcmaScript(value: Double, rendered: String): String {
    val magnitude = abs(value)
    if (magnitude >= PLAIN_LOWER && magnitude < PLAIN_UPPER && rendered.indexOf('.') >= 0) {
        // In-range decimals with a fractional part are already laid out as Java does.
        return rendered
    }
    var end = rendered.indexOf('e')
    var exponent = 0
    if (end < 0) {
        end = rendered.length
    } else {
        exponent = rendered.substring(end + 1).removePrefix("+").toInt()
    }
    val mantissa = rendered.substring(if (value < 0) 1 else 0, end)
    val dot = mantissa.indexOf('.')
    val intPart = if (dot < 0) mantissa else mantissa.substring(0, dot)
    val all = if (dot < 0) mantissa else intPart + mantissa.substring(dot + 1)
    val significant = all.trimStart('0')
    val point = intPart.length - (all.length - significant.length) + exponent
    return finish(value < 0, magnitude, significant.trimEnd('0'), point)
}

/**
 * Java `Double.toString` of a finite non-zero [value], computed exactly from its bits: the
 * Burger-Dybvig free-format algorithm (shortest digits, closest on the last one, ties to even)
 * over arbitrary-precision integers. Used on targets whose own rendering cannot be trusted.
 */
internal fun shortestJavaString(value: Double): String {
    val bits = value.toRawBits()
    val biased = ((bits ushr 52) and 0x7FF).toInt()
    val fraction = bits and 0xFFFFFFFFFFFFFL
    val f = if (biased == 0) fraction else fraction or HIDDEN_BIT
    val e = if (biased == 0) -1074 else biased - 1075
    // The rounding interval is closed when the significand is even (round-half-even).
    val inclusive = (f and 1L) == 0L
    val unequalGaps = f == HIDDEN_BIT && biased > 1
    // value = r / s; the interval around it is (value - mMinus / s, value + mPlus / s).
    val r: BigNat
    val s: BigNat
    val mPlus: BigNat
    val mMinus: BigNat
    if (e >= 0) {
        if (!unequalGaps) {
            r = BigNat.of(f).shl(e + 1)
            s = BigNat.of(2)
            mPlus = BigNat.of(1).shl(e)
        } else {
            r = BigNat.of(f).shl(e + 2)
            s = BigNat.of(4)
            mPlus = BigNat.of(1).shl(e + 1)
        }
        mMinus = BigNat.of(1).shl(e)
    } else {
        if (!unequalGaps) {
            r = BigNat.of(f).shl(1)
            s = BigNat.of(1).shl(1 - e)
            mPlus = BigNat.of(1)
        } else {
            r = BigNat.of(f).shl(2)
            s = BigNat.of(1).shl(2 - e)
            mPlus = BigNat.of(2)
        }
        mMinus = BigNat.of(1)
    }
    val magnitude = abs(value)
    // Scale so that value = (r / s) * 10^point with r / s in [0.1, 1).
    var point = ceil(log10(magnitude) - 1e-10).toInt()
    if (point >= 0) {
        s.mulPow10(point)
    } else {
        r.mulPow10(-point)
        mPlus.mulPow10(-point)
        mMinus.mulPow10(-point)
    }
    if (reachesUpper(r, mPlus, s, inclusive)) {
        s.mulSmall(10)
        point++
    } else {
        while (true) {
            r.mulSmall(10)
            mPlus.mulSmall(10)
            mMinus.mulSmall(10)
            if (reachesUpper(r, mPlus, s, inclusive)) break
            point--
        }
        // The estimate was one too high. The loop has already applied the first digit's
        // multiply, so generation starts without one.
        return generate(value, magnitude, r, s, mPlus, mMinus, inclusive, point, false)
    }
    return generate(value, magnitude, r, s, mPlus, mMinus, inclusive, point, true)
}

private fun reachesUpper(r: BigNat, mPlus: BigNat, s: BigNat, inclusive: Boolean): Boolean {
    val c = BigNat.compareSum(r, mPlus, s)
    return if (inclusive) c >= 0 else c > 0
}

private fun generate(
    value: Double,
    magnitude: Double,
    r: BigNat,
    s: BigNat,
    mPlus: BigNat,
    mMinus: BigNat,
    inclusive: Boolean,
    point: Int,
    multiplyFirst: Boolean,
): String {
    val digits = CharArray(20)
    var count = 0
    var multiply = multiplyFirst
    while (true) {
        if (multiply) {
            r.mulSmall(10)
            mPlus.mulSmall(10)
            mMinus.mulSmall(10)
        }
        multiply = true
        var d = 0
        while (r >= s) {
            r.sub(s)
            d++
        }
        val low = if (inclusive) r.compareTo(mMinus) <= 0 else r < mMinus
        val high = reachesUpper(r, mPlus, s, inclusive)
        if (!low && !high) {
            digits[count++] = '0' + d
            continue
        }
        if (high && (!low || roundUpOnTie(r, s, d))) d++
        // Burger-Dybvig never rounds the last digit up to 10 once the scale is fixed up.
        digits[count++] = '0' + d
        break
    }
    return finish(value < 0, magnitude, digits.concatToString(0, count).trimEnd('0'), point)
}

/** Both last digits are in the interval: take the closer, and the even one on an exact tie. */
private fun roundUpOnTie(r: BigNat, s: BigNat, d: Int): Boolean {
    val twice = r.copy().also { it.shl(1) }
    val c = twice.compareTo(s)
    return c > 0 || (c == 0 && d % 2 == 1)
}

private fun finish(negative: Boolean, magnitude: Double, digits: String, point: Int): String {
    if (digits.length == 1 && magnitude < SUBNORMAL_TWO_DIGIT_LIMIT) {
        closerTwoDigitDecimal(magnitude, digits[0] - '0', point)?.let { (d, p) ->
            return layoutJava(negative, magnitude, d, p)
        }
    }
    return layoutJava(negative, magnitude, digits, point)
}

private const val HIDDEN_BIT = 1L shl 52

/**
 * Below this, the rounding interval of a double (one `Double.MIN_VALUE` wide) can hold more
 * than one two-digit decimal, which is the only case where JDK 19+ prefers a two-digit
 * decimal over the shortest one-digit decimal. Above it the rule never changes the output.
 */
private const val SUBNORMAL_TWO_DIGIT_LIMIT = 1e-320

/** `Double.MIN_VALUE` (2^-1074) scaled by 10^324. */
private const val MIN_VALUE_TIMES_1E324 = 4.940656458412465

/**
 * JDK 19+ `Double.toString`: when the shortest decimal that rounds to [magnitude] has a single
 * digit, choose among all decimals of length one or two that round to it the one closest to
 * it. [digit] and [point] describe that one-digit decimal as `0.digit * 10^point`. Returns the
 * replacement as (digits, point), or null when the one-digit decimal is already the closest.
 *
 * Works in units of `Double.MIN_VALUE`, where the subnormal [magnitude] is the integer `k` and
 * a candidate `m * 10^t` rounds to it exactly when it lies within half a unit of `k`. No
 * candidate is ever exactly half a unit away, as `k * 2^-1074` is never a short decimal.
 */
private fun closerTwoDigitDecimal(magnitude: Double, digit: Int, point: Int): Pair<String, Int>? {
    val k = magnitude / Double.MIN_VALUE
    fun units(m: Int, t: Int): Double {
        var scale = MIN_VALUE_TIMES_1E324
        var shift = t + 324
        while (shift > 0) {
            scale /= 10.0
            shift--
        }
        while (shift < 0) {
            scale *= 10.0
            shift++
        }
        return m / scale
    }
    var bestDistance = abs(units(digit, point - 1) - k)
    var best: Pair<String, Int>? = null
    // Two-digit decimals m * 10^t at the one-digit decimal's scale and the next one down.
    for (t in (point - 3)..(point - 2)) {
        for (m in 11..99) {
            if (m % 10 == 0) continue
            val distance = abs(units(m, t) - k)
            if (distance < 0.5 && distance < bestDistance) {
                bestDistance = distance
                best = m.toString() to (t + 2)
            }
        }
    }
    return best
}

/** Minimal mutable arbitrary-precision natural number, little-endian base 2^32 limbs. */
private class BigNat private constructor(private var limbs: IntArray, private var size: Int) :
    Comparable<BigNat> {

    fun copy(): BigNat = BigNat(limbs.copyOf(), size)

    fun shl(bits: Int): BigNat {
        if (size == 0 || bits == 0) return this
        val words = bits / 32
        val rem = bits % 32
        val out = IntArray(size + words + 1)
        for (i in 0 until size) {
            val v = limbs[i].toLong() and MASK
            val shifted = v shl rem
            out[i + words] = out[i + words] or shifted.toInt()
            out[i + words + 1] = (shifted ushr 32).toInt()
        }
        limbs = out
        size = out.size
        trim()
        return this
    }

    fun mulSmall(m: Int) {
        var carry = 0L
        for (i in 0 until size) {
            val p = (limbs[i].toLong() and MASK) * m + carry
            limbs[i] = p.toInt()
            carry = p ushr 32
        }
        if (carry != 0L) {
            ensure(size + 1)
            limbs[size++] = carry.toInt()
        }
    }

    fun mulPow10(n: Int) {
        var left = n
        while (left >= 9) {
            mulSmall(1_000_000_000)
            left -= 9
        }
        var tail = 1
        repeat(left) { tail *= 10 }
        if (tail != 1) mulSmall(tail)
    }

    /** this -= other; requires this >= other. */
    fun sub(other: BigNat) {
        var borrow = 0L
        for (i in 0 until size) {
            val o = if (i < other.size) other.limbs[i].toLong() and MASK else 0L
            val d = (limbs[i].toLong() and MASK) - o - borrow
            limbs[i] = d.toInt()
            borrow = if (d < 0) 1L else 0L
        }
        trim()
    }

    override fun compareTo(other: BigNat): Int {
        if (size != other.size) return if (size < other.size) -1 else 1
        for (i in size - 1 downTo 0) {
            val a = limbs[i].toLong() and MASK
            val b = other.limbs[i].toLong() and MASK
            if (a != b) return if (a < b) -1 else 1
        }
        return 0
    }

    private fun ensure(capacity: Int) {
        if (limbs.size < capacity) limbs = limbs.copyOf(maxOf(capacity, limbs.size * 2))
    }

    private fun trim() {
        while (size > 0 && limbs[size - 1] == 0) size--
    }

    companion object {
        private const val MASK = 0xFFFFFFFFL

        fun of(v: Long): BigNat {
            val n = BigNat(intArrayOf(v.toInt(), (v ushr 32).toInt()), 2)
            n.trim()
            return n
        }

        /** Sign of (a + b) - c. */
        fun compareSum(a: BigNat, b: BigNat, c: BigNat): Int {
            val n = maxOf(a.size, b.size, c.size) + 1
            var carry = 0L
            val sum = IntArray(n)
            for (i in 0 until n) {
                val x = if (i < a.size) a.limbs[i].toLong() and MASK else 0L
                val y = if (i < b.size) b.limbs[i].toLong() and MASK else 0L
                val t = x + y + carry
                sum[i] = t.toInt()
                carry = t ushr 32
            }
            return BigNat(sum, n).also { it.trim() }.compareTo(c)
        }
    }
}
