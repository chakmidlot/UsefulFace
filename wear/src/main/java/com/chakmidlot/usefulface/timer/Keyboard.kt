package com.chakmidlot.usefulface.timer

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.Log
import com.chakmidlot.usefulface.Drawing


class Keyboard(private val keyboardRect: List<Float>) {

    var timerNumbers = listOf(0, 0, 0, 5, 0, 0)
    private var timerValue = timerNumbers

    private var keyboardPaint: Paint
            = Drawing.createTextPaint(Color.WHITE, 30f, true, true)
    private var blackRect = Drawing.createTextPaint(Color.BLACK)
    private var whiteRect = Drawing.createTextPaint(Color.WHITE)

    private val keymap = mapOf(
            Pair(0, 0) to Pair("1", { clickNumber(1) }),
            Pair(1, 0) to Pair("2", { clickNumber(2) }),
            Pair(2, 0) to Pair("3", { clickNumber(3) }),
            Pair(0, 1) to Pair("4", { clickNumber(4) }),
            Pair(1, 1) to Pair("5", { clickNumber(5) }),
            Pair(2, 1) to Pair("6", { clickNumber(6) }),
            Pair(0, 2) to Pair("7", { clickNumber(7) }),
            Pair(1, 2) to Pair("8", { clickNumber(8) }),
            Pair(2, 2) to Pair("9", { clickNumber(9) }),
            Pair(3, 2) to Pair("0", { clickNumber(0) }),
            Pair(3, 0) to Pair("✓", { clickOk() }),
            Pair(3, 1) to Pair("←", { clickBackspace() })
    )

    fun openKeyboard() {
        timerNumbers = timerValue
    }

    fun click(index: Pair<Int, Int>) {
        keymap[index]!!.second.invoke()
    }

    fun clickNumber(number: Int) {
        timerNumbers = timerNumbers.slice(1..5) + listOf(number)
        Log.d("UsefulFace", "Number $number")
    }

    fun clickOk() {
        timerValue = timerNumbers
        FaceTimer.timerSettingMillis =
                (timerValue[0] * 10 + timerValue[1]) * 24 * 60_000 +
                        (timerValue[2] * 10 + timerValue[3]) * 60_000 +
                        (timerValue[4] * 10 + timerValue[5]) * 1000
        Log.d("UsefulFace", "Timer set: ${FaceTimer.timerSettingMillis}")

        FaceTimer.isMaximized = false
    }

    fun clickBackspace() {
        timerNumbers = listOf(0) + timerNumbers.slice(0..4)
        Log.d("UsefulFace", "Click Backspace")
    }

    fun drawKeyboard(canvas: Canvas) {
        (0..3).map { i ->
            (0..2).map { j ->
                canvas.drawText(keymap[Pair(i, j)]!!.first,
                        i * 52 + keyboardRect[0] + 26, j * 52 + keyboardRect[1] + 35,
                        keyboardPaint)
            }
        }
    }

    fun drawKeyboardCells(canvas: Canvas) {
        canvas.drawRect(keyboardRect[0], keyboardRect[1],
                keyboardRect[2], keyboardRect[3], whiteRect)
        canvas.drawRect(keyboardRect[0] + 1, keyboardRect[1] + 1,
                keyboardRect[2] - 1, keyboardRect[3] - 1, blackRect)

        for (i in 1 .. 3) {
            canvas.drawLine(i * 52 + keyboardRect[0], keyboardRect[1],
                    i * 52 + keyboardRect[0], keyboardRect[3], whiteRect)
        }

        for (i in 1 .. 2) {
            canvas.drawLine(keyboardRect[0], i * 52 + keyboardRect[1],
                    keyboardRect[2], i * 52 + keyboardRect[1], whiteRect)
        }
    }
}