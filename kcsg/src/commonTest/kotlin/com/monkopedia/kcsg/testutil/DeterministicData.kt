package com.monkopedia.kcsg.testutil

import com.monkopedia.kcsg.Vector3d
import kotlin.random.Random

object DeterministicData {
    const val DEFAULT_SEED: Int = 0x1A17C0DE

    fun random(seed: Int = DEFAULT_SEED): Random = Random(seed)

    fun vector3dList(
        count: Int,
        seed: Int = DEFAULT_SEED,
        min: Double = -1.0,
        max: Double = 1.0,
    ): List<Vector3d> {
        require(count >= 0) { "count must be >= 0" }
        require(max >= min) { "max must be >= min" }

        val rng = random(seed)
        val range = max - min
        return List(count) {
            Vector3d.xyz(
                min + rng.nextDouble() * range,
                min + rng.nextDouble() * range,
                min + rng.nextDouble() * range,
            )
        }
    }
}
