@file:Suppress("MagicNumber")

package services.afroforge.graviton.data

import kotlin.random.Random

/**
 * Represents a range of values for randomization.
 */
sealed class ValueRange<T> {
    abstract fun sample(random: Random = Random.Default): T

    /**
     * Constant value (no randomization).
     */
    data class Constant<T>(val value: T) : ValueRange<T>() {
        override fun sample(random: Random) = value
    }

    /**
     * Uniform random distribution between min and max.
     */
    data class Uniform(val min: Double, val max: Double) : ValueRange<Double>() {
        override fun sample(random: Random) = min + random.nextDouble() * (max - min)
    }

    /**
     * Random integer between min (inclusive) and max (inclusive).
     */
    data class UniformInt(val min: Int, val max: Int) : ValueRange<Int>() {
        override fun sample(random: Random) = random.nextInt(min, max + 1)
    }

    /**
     * Random value from a list of options.
     */
    data class Choice<T>(val options: List<T>) : ValueRange<T>() {
        constructor(vararg options: T) : this(options.toList())

        override fun sample(random: Random) = options[random.nextInt(options.size)]
    }

    /**
     * Random value with Gaussian (normal) distribution.
     * @param mean Center of the distribution
     * @param stdDev Standard deviation
     */
    data class Gaussian(val mean: Double, val stdDev: Double) : ValueRange<Double>() {
        override fun sample(random: Random): Double {
            // Box-Muller transform for Gaussian distribution
            val u1 = random.nextDouble()
            val u2 = random.nextDouble()
            val z0 = kotlin.math.sqrt(-2.0 * kotlin.math.ln(u1)) * kotlin.math.cos(2.0 * Math.PI * u2)
            return mean + z0 * stdDev
        }
    }

    companion object {
        /**
         * Create a constant range.
         */
        fun <T> constant(value: T): ValueRange<T> = Constant(value)

        /**
         * Create a uniform range.
         */
        fun uniform(
            min: Double,
            max: Double,
        ): ValueRange<Double> = Uniform(min, max)

        /**
         * Create a uniform integer range.
         */
        fun uniformInt(
            min: Int,
            max: Int,
        ): ValueRange<Int> = UniformInt(min, max)

        /**
         * Create a choice range.
         */
        fun <T> choice(vararg options: T): ValueRange<T> = Choice(*options)

        /**
         * Create a Gaussian range.
         */
        fun gaussian(
            mean: Double,
            stdDev: Double,
        ): ValueRange<Double> = Gaussian(mean, stdDev)
    }
}

/**
 * A range that can return either a color gradient or a single color.
 */
sealed class ColorRange {
    abstract fun sample(
        time: Double,
        random: Random,
    ): Color

    /**
     * Single constant color.
     */
    data class Constant(val color: Color) : ColorRange() {
        override fun sample(
            time: Double,
            random: Random,
        ) = color
    }

    /**
     * Gradient over time.
     */
    data class Animated(val gradient: Gradient) : ColorRange() {
        override fun sample(
            time: Double,
            random: Random,
        ) = gradient.evaluate(time)
    }

    /**
     * Random color from a list.
     */
    data class RandomChoice(val colors: List<Color>) : ColorRange() {
        constructor(vararg colors: Color) : this(colors.toList())

        override fun sample(
            time: Double,
            random: kotlin.random.Random,
        ) = colors[random.nextInt(colors.size)]
    }

    companion object {
        fun constant(color: Color): ColorRange = Constant(color)

        fun animated(gradient: Gradient): ColorRange = Animated(gradient)

        fun random(vararg colors: Color): ColorRange = RandomChoice(*colors)
    }
}
