package com.chakmidlot.usefulface.timer

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.Handler
import android.util.Log
import com.chakmidlot.usefulface.Drawing
import com.chakmidlot.usefulface.Drawing.Companion.createTextPaint


object FaceTimer {

    private var isCounting = false
    var isMaximized = false
    private var countingStartTime: Long = 0
    private var countingStopTime: Long = 0
    private var timerSetting: String = ""

    private var timerPaint: Paint

    private var datePaintGrey: Paint
    private var blackRect = Drawing.createTextPaint(Color.BLACK)
    private var whiteRect = Drawing.createTextPaint(Color.WHITE)

    private var keyboardRect = listOf(40f, 110f, 248f, 266f)

    var timerSettingMillis = 0

    private val keyboard = Keyboard(keyboardRect)

    private var handler: Handler

    init {
        timerPaint = createTextPaint(Color.WHITE, 17f, true)
        timerPaint = createTextPaint(Color.WHITE, 17f, true)
        datePaintGrey = createTextPaint(Color.parseColor("#AAAAAA"), 17f, true)
        handler = Handler()
    }

    fun click(x: Int, y: Int, longClick: Boolean = false) {
        if (isMaximized) {
            if (!checkMaximizedView(x, y)) {
                isMaximized = false
            }
            else {
                keyboard.click(Pair((x - keyboardRect[0]).toInt() / 52,
                                    (y - keyboardRect[1]).toInt() / 52)
                )
            }
        }
        else {
            if (checkShortView(x, y)) {
                if (longClick) {
                    isMaximized = true
                }
                else {
                    operateTimer()
                }
            }
        }
    }

    fun draw(canvas: Canvas) {
        if (isMaximized) {

            canvas.drawRect(keyboardRect[0], keyboardRect[1] - 20,
                    keyboardRect[2], keyboardRect[3], whiteRect)
            canvas.drawRect(keyboardRect[0] + 1, keyboardRect[1] + 1 - 20,
                    keyboardRect[2] - 1, keyboardRect[3] - 1, blackRect)


            drawTimer(canvas, keyboardRect[0] + 10, keyboardRect[1])

            val setValue = "%d%d:%d%d:%d%d".format(keyboard.timerNumbers[0], keyboard.timerNumbers[1],
                    keyboard.timerNumbers[2], keyboard.timerNumbers[3], keyboard.timerNumbers[4], keyboard.timerNumbers[5])

            canvas.drawText(setValue,keyboardRect[0] + 110, keyboardRect[1], timerPaint)

            keyboard.drawKeyboard(canvas)
        }
        else {
            drawTimer(canvas, 10f, 150f)
        }
    }

    fun drawTimer(canvas: Canvas, x: Float, y: Float) {
        if (countingStartTime == 0L) {
            canvas.drawText("00:00:00", x, y, datePaintGrey)
        }
        else if (countingStopTime == 0L) {
            val restTime = (countingStartTime + timerSettingMillis - System.currentTimeMillis()) / 1000

            val restTimeString = if (restTime >= 0) {
                "%02d:%02d:%02d".format(restTime / 3600, (restTime / 60) % 60, restTime % 60)
            }
            else {
                "-%02d:%02d:%02d".format((-restTime + 1) / 3600, (-(restTime + 1) / 60) % 60, (-restTime + 1) % 60)
            }

            canvas.drawText(restTimeString, x, y, timerPaint)
        }
        else {
            val restTime = (countingStartTime + timerSettingMillis - countingStopTime) / 1000

            val restTimeString = if (restTime >= 0) {
                "%02d:%02d:%02d".format(restTime / 3600, (restTime / 60) % 60, restTime % 60)
            }
            else {
                "-%02d:%02d:%02d".format((-restTime + 1) / 3600, (-(restTime + 1) / 60) % 60, (-restTime + 1) % 60)
            }

            canvas.drawText(restTimeString, x, y, datePaintGrey)
        }

    }

    private fun operateTimer() {
        when {
            countingStartTime == 0L -> startCounting()
            countingStopTime == 0L -> stopCounting()
            else -> reset()
        }
    }

    fun click2(x: Int, y: Int, longClick: Boolean = false) {
        if (longClick) {
            if (isMaximized) {
                if (checkShortView(x, y)) {
                    isMaximized = false
                } else if (checkMaximizedView(x, y)) {
                    isMaximized = true
                }
            }
        }
        else {
            if (isMaximized) {

            }
            else {

            }
        }
    }

    private fun reset() {
        countingStartTime = 0L
        countingStopTime = 0L
    }

    private fun startCounting() {
        countingStartTime = System.currentTimeMillis()
        scheduleWibration()
    }

    private fun stopCounting() {
        countingStopTime = System.currentTimeMillis()
    }

    fun checkShortView(x: Int, y: Int): Boolean {
        return (x < 150 && y > 120 && y < 170)
    }

    fun checkMaximizedView(x: Int, y: Int): Boolean {
        return (x > keyboardRect[0] && x < keyboardRect[2]
                && y > keyboardRect[1] && y < keyboardRect[3])
    }

    fun scheduleWibration() {
        val r = object : Runnable {
            override fun run() {
                handler.postDelayed(this, 1000)

            }
        }
    }
}
