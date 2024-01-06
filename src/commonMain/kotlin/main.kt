import korlibs.audio.sound.*
import korlibs.image.color.*
import korlibs.korge.*
import korlibs.korge.input.*
import korlibs.korge.view.*
import korlibs.event.*
import korlibs.io.file.std.*
import korlibs.korge.tween.*
import korlibs.korge.view.Circle
import korlibs.math.geom.*
import korlibs.math.interpolation.*
import korlibs.math.random.*
import korlibs.time.*
import kotlinx.coroutines.*
import kotlin.random.*

val buffer = 64f
val y=buffer*5
val width = 640
val height = 480
val LEFT = buffer
val RIGHT = width - 3*buffer
val MIDDLE = (LEFT + RIGHT)/2
suspend fun main() = Korge(Korge(windowSize = Size(width, height))) { // this:
    val music = resourcesVfs["nastyv2.mp3"].readSound().play()
    val text = text("0", textSize = 32f)
    val obstacle = solidRect(200, 100)
    val ball = circle(buffer, Colors.PURPLE)
    val startButton = text("Старт", textSize = 32f, fill = Colors.GREEN)
    val c = 0

    val scope = CoroutineScope(Dispatchers.Default)
    val job = launch {
        delay(1000)
    }
    delay(500)
    job.cancel()
    job.join()

    val job2 = async(scope.coroutineContext) {
        delay(1000)
    }
    delay(500)
    scope.coroutineContext.cancelChildren()
    try {
        job2.join()
    } catch (e: Throwable) {
        println(e)
    }


    setupStage(text, obstacle, ball, startButton)
    setupControls(ball)

    // start game on click
    startButton.onClick {
        gameLoop(obstacle, ball, text, startButton, c)
    }
}


private suspend fun Stage.gameLoop(obstacle: SolidRect, ball: Circle, text: Text, startButton: Text, c: Int) {
    setupStage(text, obstacle, ball, startButton)
    var c1 = c
    while (ball.fill == Colors.PURPLE) {
        text.text = c1.toString()
        tryCatch {
            val job = obstacle.tweenAsync(obstacle::y[height]) // drop down
            ball.onCollision({ it == obstacle }) {
                if (ball.fill == Colors.PURPLE)
                    ball.fill = Colors.BLUE
                if (job.isActive)
                    job.cancel()
            }
            if (!job.isCancelled)
                job.await()
            obstacle.tween(obstacle::y[-buffer], time = 0.milliseconds)
            val x = Random[buffer, width - 3 * buffer]
            obstacle.tween(obstacle::x[x], time = 0.milliseconds)
            c1 += 1
        }

    }
    toggleStartButton(startButton)
}

private suspend fun tryCatch(doStuff: suspend () -> Unit) {
    try {
        doStuff()
    } catch (e: Throwable) {
        if (e is CancellationException) println(e)
        else throw e
    }
}

private fun toggleStartButton(restartButton: Text) {
    if (restartButton.y != buffer) {
        restartButton.xy(RIGHT-buffer, buffer)
    } else {
        restartButton.xy(RIGHT-buffer, -buffer)
    }
}

private fun setupStage(text: Text, obstacle: SolidRect, ball: Circle, restartButton: Text) {
    toggleStartButton(restartButton)
    text.xy(20, 20)
    obstacle.xy(RIGHT-buffer, -2*buffer)
    ball.xy(buffer, buffer * 5).fill = Colors.PURPLE
}

private fun Stage.setupControls(ball: Circle) {
    keys {
        down {
            if (it.key == Key.SPACE) {
                ballLogic(ball)
            }
        }
    }
    onClick {
        ballLogic(ball)
    }
}

private suspend fun ballLogic(ball: Circle) {
    if (ball.x <= MIDDLE) {
        ball.tween(*ballPath(ball, MIDDLE, RIGHT), easing = Easing.SMOOTH, time = 100.milliseconds)
    } else {
        ball.tween(*ballPath(ball, MIDDLE, LEFT), easing = Easing.SMOOTH, time = 100.milliseconds)
    }
}

private fun ballPath(ball: Circle, middle: Float, goal: Float): Array<out V2<*>> {
    return arrayOf(
        ball::x[ball.x, middle],
        ball::y[ball.y, y+20],
        ball::x[ball.x, goal],
        ball::y[ball.y, y],
    )
}
