package utils

import kotlin.math.absoluteValue
import kotlin.math.roundToInt
import kotlin.reflect.KProperty
import kotlin.time.Duration

fun KPixelGameEngine.animation(duration: Duration, code: AnimationContext.() -> Unit): Animation {
    return SingleAnimation(duration, limitFps, code)
}

fun action(code: () -> Unit): Animation = Action(code)

interface Animation {
    val ended: Boolean
    fun update()
}

val Animation?.active get() = this?.ended == false

infix fun Animation.then(next: Animation) = CombinedAnimation(this, next)

class Action(val code: () -> Unit) : Animation {
    private var hasRun = false
    override val ended: Boolean
        get() = hasRun

    override fun update() {
        if (!hasRun) code().also { hasRun = true }
    }
}

class CombinedAnimation(private val first: Animation, private val second: Animation) : Animation {
    override val ended: Boolean
        get() = second.ended

    override fun update() {
        if (!first.ended) first.update() else second.update()
    }
}

class SingleAnimation(duration: Duration, fps: Int, val code: AnimationContext.() -> Unit) : Animation {
    override val ended: Boolean get() = context.currentFrame >= totalFrames
    private val totalFrames = (fps * duration.inWholeMilliseconds / 1000).coerceAtLeast(1)
    private val context = AnimationContext(totalFrames)

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