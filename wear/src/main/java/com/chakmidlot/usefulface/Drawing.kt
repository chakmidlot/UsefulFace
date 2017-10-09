package com.chakmidlot.usefulface

import android.graphics.*

class Drawing {

    private val backgroundPaint: Paint = Paint(Color.BLACK)
    private val hoursPaint: Paint
    private val secondsPaint: Paint
    private val datePaint: Paint
    private val batteryPaint: Paint
    private val dataPaint: Paint

    init {
        hoursPaint = createTextPaint(Color.WHITE, 60f)
        secondsPaint = createTextPaint(Color.parseColor("#AAAAAA"), 40f)
        datePaint = createTextPaint(Color.WHITE, 30f)
        batteryPaint = createTextPaint(Color.WHITE, 17f)
        dataPaint = createTextPaint(Color.WHITE, 17f, true)
    }

    fun draw(canvas: Canvas, bounds: Rect, data: Map<String, String>) {
        canvas.drawRect(0f, 0f,
                bounds.width().toFloat(), bounds.height().toFloat(), backgroundPaint)

        drawWatch(canvas, data)
        drawCharge(canvas, data)
        drawBank(canvas, data)
    }

    fun drawWatch(canvas: Canvas, data: Map<String, String>) {
        canvas.drawText(data["current_hour_minute"], 60f, 80f, hoursPaint)
        canvas.drawText(data["current_seconds"], 210f, 80f, secondsPaint)
        canvas.drawText(data["current_date"], 60f, 120f, datePaint)
        canvas.drawText(data["current_day_of_week"], 225f, 120f, datePaint)
    }

    fun drawCharge(canvas: Canvas, data: Map<String, String>) {
        canvas.drawText(data["charge_wear"], 8f, 120f, batteryPaint)
        canvas.drawText(data["charge_mobile"], 275f, 120f, batteryPaint)
    }

    fun drawBank(canvas: Canvas, data: Map<String, String>) {
        canvas.drawText(data["currnecy_rate"], 0f, 150f, dataPaint)
        canvas.drawText(data["balance_belinvest_main"], 0f, 175f, dataPaint)
        canvas.drawText(data["balance_prior_main"], 0f, 195f, dataPaint)
        canvas.drawText(data["prior_internet"], 0f, 215f, dataPaint)
    }

    private fun createTextPaint(textColor: Int, textSize: Float, monoSpace: Boolean=false): Paint {
        val paint = Paint()
        paint.color = textColor
        paint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
        paint.isAntiAlias = true
        paint.textSize = textSize
        if (monoSpace) {
            paint.typeface = Typeface.MONOSPACE
        }
        return paint
    }
}