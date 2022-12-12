package utils

import kotlin.math.absoluteValue
import kotlin.math.roundToInt
import kotlin.math.roundToLong
import kotlin.reflect.KProperty
import kotlin.time.Duration

fun KPixelGameEngine.animation(duration: Duration, fps: Int = limitFps, code: AnimationContext.() -> Unit): Animation {
    return SingleAnimation(duration, fps, code)
}

fun action(code: () -> Unit): Animation = Action(code)

interface Animation {
    val frames: Long
    val ended: Boolean
    fun update()
}

val Animation?.active get() = this?.ended == false
val Animation?.ended get() = this?.ended == true

infix fun Animation.then(next: Animation) = CombinedAnimation(this, next)

class Action(val code: () -> Unit) : Animation {
    private var hasRun = false
    override val frames: Long
        get() = 1
    override val ended: Boolean
        get() = hasRun

    override fun update() {
        if (!hasRun) code().also { hasRun = true }
    }
}

class CombinedAnimation(private val first: Animation, private val second: Animation) : Animation {
    override val frames: Long
        get() = first.frames + second.frames
    override val ended: Boolean
        get() = second.ended

    override fun update() {
        if (!first.ended) first.update() else second.update()
    }
}

class SingleAnimation(duration: Duration, fps: Int, val code: AnimationContext.() -> Unit) : Animation {
    override val ended: Boolean get() = context.currentFrame >= frames
    override val frames = (duration.inWholeMilliseconds / 1000.0 * fps).roundToLong().coerceAtLeast(1)
    private val context = AnimationContext(frames)

    override fun update() {
        if (!ended) {
            context.code()
            context.currentFrame++
        }
    }
}

abstract class AnimatedValue<T>(val context: AnimationContext) {
    abstract val value: T
    operator fun getValue(nothing: Nothing?, property: KProperty<*>): T = value
}

class AnimatedPoint(first: Point, last: Point, context: AnimationContext) : AnimatedValue<Point>(context) {
    private val xAnim = AnimatedInt(first.x, last.x, context)
    private val yAnim = AnimatedInt(first.y, last.y, context)
    override val value: Point
        get() = xAnim.value to yAnim.value

}

class AnimatedInt(private val first: Int, private val last: Int, context: AnimationContext) :
    AnimatedValue<Int>(context) {
    private val size = (last - first).absoluteValue
    private val dir = if (last > first) 1 else -1
    override val value: Int
        get() {
            if (first == last) return first
            val f = context.currentFrame
            val l = context.totalFrames - 1
            return when (f) {
                0L -> first
                l -> last
                else -> (first + dir * size * f.toDouble() / l).roundToInt()
            }
        }

}

class AnimationContext(val totalFrames: Long) {
    var currentFrame = 0L
    fun animate(from: Int, to: Int) = AnimatedInt(from, to, this)
    fun animate(from: Point, to: Point) = AnimatedPoint(from, to, this)

    fun onStart(code: () -> Unit) {
        if (currentFrame == 0L) code()
    }

    fun onLastFrame(code: () -> Unit) {
        if (currentFrame == totalFrames - 1) code()
    }
}