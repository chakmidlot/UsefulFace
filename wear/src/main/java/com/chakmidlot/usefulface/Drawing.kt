package com.chakmidlot.usefulface

import android.graphics.*

class Drawing {

    private val backgroundPaint: Paint = Paint(Color.BLACK)
    private val hoursPaint: Paint
    private val secondsPaint: Paint
    private val datePaint: Paint
    private val dayOfWeekPaint: Paint
    private val batteryPaint: Paint
    private val dataPaint: Paint
    private val dataPaintCenter: Paint

    init {
        hoursPaint = createTextPaint(Color.WHITE, 60f)
        secondsPaint = createTextPaint(Color.parseColor("#AAAAAA"), 40f)
        datePaint = createTextPaint(Color.WHITE, 30f)
        dayOfWeekPaint = createTextPaint(Color.WHITE, 30f, true)
        batteryPaint = createTextPaint(Color.WHITE, 17f)
        dataPaint = createTextPaint(Color.WHITE, 17f, true)
        dataPaintCenter = createTextPaint(Color.WHITE, 17f, true, true)
    }

    fun draw(canvas: Canvas, bounds: Rect, data: DataStructure) {
        canvas.drawRect(0f, 0f,
                bounds.width().toFloat(), bounds.height().toFloat(), backgroundPaint)

        drawWatch(canvas, data.time)
        drawCharge(canvas, data.charge)
        drawCurrency(canvas, data.currency)
        drawBank(canvas, data.bank)
        drawSchedule(canvas, data.buses)
        drawVocabulary(canvas, data.vocabulary)
        drawWeather(canvas, data.weather)
    }

    fun drawWatch(canvas: Canvas, data: Time) {
        canvas.drawText(data.hour_minute, 60f, 80f, hoursPaint)
        canvas.drawText(data.seconds, 210f, 80f, secondsPaint)
        canvas.drawText(data.date, 60f, 120f, datePaint)
        canvas.drawText(data.dayOfWeek, 225f, 120f, dayOfWeekPaint)
    }

    fun drawCharge(canvas: Canvas, data: List<Charge>) {
        canvas.drawText(data[0].value, 8f, 120f, batteryPaint)
        canvas.drawText(data[1].value, 275f, 120f, batteryPaint)
    }

    fun drawCurrency(canvas: Canvas, data: Currency) {
        canvas.drawText(data.value, 0f, 150f, dataPaint)
    }

    fun drawBank(canvas: Canvas, data: List<Balance>) {
        data.mapIndexed { index, balance -> canvas.drawText(balance.value, 0f, 175f + index * 20, dataPaint) }
    }

    fun drawSchedule(canvas: Canvas, data: List<Bus>) {
        canvas.drawText("\uD83D\uDE8C84", 100f, 150f, dataPaint)
        data.mapIndexed { index, bus ->  canvas.drawText(bus.remains, 100f, 170f + index * 20, dataPaint) }
    }

    fun drawVocabulary(canvas: Canvas, data: Pair<String, String>) {
        canvas.drawText(data.first, 160f, 270f, dataPaintCenter)
        canvas.drawText(data.second, 160f, 290f, dataPaintCenter)
    }

    fun drawWeather(canvas: Canvas, data: List<String>) {
        data.forEachIndexed { index, s -> canvas.drawText(s, 250f, 150f + index * 20, dataPaint) }
    }

    private fun createTextPaint(textColor: Int, textSize: Float, monoSpace: Boolean=false, center: Boolean=false): Paint {
        val paint = Paint()
        paint.color = textColor
        paint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
        paint.isAntiAlias = true
        paint.textSize = textSize
        if (monoSpace) {
            paint.typeface = Typeface.MONOSPACE
        }
        if (center) {
            paint.textAlign = Paint.Align.CENTER
        }
        return paint
    }
}
