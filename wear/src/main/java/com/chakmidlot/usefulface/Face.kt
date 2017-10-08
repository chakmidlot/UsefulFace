/*
 * Copyright (C) 2017 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.chakmidlot.usefulface

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.Typeface
import android.os.BatteryManager
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.support.wearable.watchface.CanvasWatchFaceService
import android.support.wearable.watchface.WatchFaceService
import android.support.wearable.watchface.WatchFaceStyle
import android.util.Log
import android.view.SurfaceHolder
import android.view.WindowInsets
import android.widget.Toast
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.wearable.PutDataMapRequest
import com.google.android.gms.wearable.PutDataRequest
import com.google.android.gms.wearable.Wearable

import java.lang.ref.WeakReference
import java.util.Calendar
import java.util.TimeZone
import java.util.concurrent.TimeUnit

/**
 * Digital watch face with seconds. In ambient mode, the seconds aren't displayed. On devices with
 * low-bit ambient mode, the text is drawn without anti-aliasing in ambient mode.
 */
class Face : CanvasWatchFaceService(), GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {
    override fun onConnectionFailed(p0: ConnectionResult) {
        Log.w("UsefulFace", "Connection Failed")
    }

    override fun onConnectionSuspended(p0: Int) {
        Log.w("UsefulFace", "Connection Suspended")
    }

    override fun onConnected(p0: Bundle?) {
        Log.d("UsefulFace", "API Client connected")
    }

    override fun onCreateEngine(): Engine {
        return Engine()
    }

    private class EngineHandler(reference: Face.Engine) : Handler() {
        private val mWeakReference = WeakReference(reference)

        override fun handleMessage(msg: Message) {
            val engine = mWeakReference.get()
            if (engine != null) {
                when (msg.what) {
                    MSG_UPDATE_TIME -> engine.handleUpdateTimeMessage()
                }
            }
        }
    }

    inner class Engine : CanvasWatchFaceService.Engine() {
        private val mUpdateTimeHandler: Handler = EngineHandler(this)
        private var mRegisteredTimeZoneReceiver = false
        private lateinit var mBackgroundPaint: Paint
        private lateinit var hoursPaint: Paint
        private lateinit var secondsPaint: Paint
        private lateinit var datePaint: Paint
        private lateinit var battaryPaint: Paint

        private lateinit var dataPaint: Paint

        private var mAmbient: Boolean = false
        private lateinit var mCalendar: Calendar
        private val mTimeZoneReceiver: BroadcastReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                mCalendar.timeZone = TimeZone.getDefault()
                invalidate()
            }
        }
        private var mXOffset: Float = 0f
        private var mYOffset: Float = 0f

        private val dayOfWeek = hashMapOf(
                1 to "SU",
                2 to "MO",
                3 to "TU",
                4 to "WE",
                5 to "TH",
                6 to "FR",
                7 to "SA"
        )

        /**
         * Whether the display supports fewer bits for each color in ambient mode. When true, we
         * disable anti-aliasing in ambient mode.
         */
        private var mLowBitAmbient: Boolean = false

        override fun onCreate(holder: SurfaceHolder?) {
            super.onCreate(holder)

            setWatchFaceStyle(WatchFaceStyle.Builder(this@Face)
                    .build())
            val resources = this@Face.resources
            mYOffset = resources.getDimension(R.dimen.digital_y_offset)

            mBackgroundPaint = Paint()
            mBackgroundPaint.color = resources.getColor(R.color.background)

            mCalendar = Calendar.getInstance()

            hoursPaint = createTextPaint(Color.WHITE, 60f)
            secondsPaint = createTextPaint(Color.parseColor("#AAAAAA"), 40f)
            datePaint = createTextPaint(Color.WHITE, 30f)
            battaryPaint = createTextPaint(Color.WHITE, 17f)
            dataPaint = createTextPaint(Color.WHITE, 17f, true)
        }

        override fun onDestroy() {
            mUpdateTimeHandler.removeMessages(MSG_UPDATE_TIME)
            Log.d("UsefulFace", "Destroy")
            super.onDestroy()
        }

        private fun createTextPaint(textColor: Int, textSize: Float, monoSpace: Boolean=false): Paint {
            val paint = Paint()
            paint.color = textColor
            paint.typeface = NORMAL_TYPEFACE
            paint.isAntiAlias = true
            paint.textSize = textSize
            if (monoSpace) {
                paint.setTypeface(Typeface.MONOSPACE)
            }
            return paint
        }

        override fun onVisibilityChanged(visible: Boolean) {
            super.onVisibilityChanged(visible)

            if (visible) {
                registerReceiver()

                // Update time zone in case it changed while we weren't visible.
                mCalendar.timeZone = TimeZone.getDefault()
                invalidate()
            } else {
                unregisterReceiver()
            }

            // Whether the timer should be running depends on whether we're visible (as well as
            // whether we're in ambient mode), so we may need to start or stop the timer.
            updateTimer()
        }

        private fun registerReceiver() {
            if (mRegisteredTimeZoneReceiver) {
                return
            }
            mRegisteredTimeZoneReceiver = true
            val filter = IntentFilter(Intent.ACTION_TIMEZONE_CHANGED)
            this@Face.registerReceiver(mTimeZoneReceiver, filter)
        }

        private fun unregisterReceiver() {
            if (!mRegisteredTimeZoneReceiver) {
                return
            }
            mRegisteredTimeZoneReceiver = false
            this@Face.unregisterReceiver(mTimeZoneReceiver)
        }

        override fun onApplyWindowInsets(insets: WindowInsets) {
            super.onApplyWindowInsets(insets)

            // Load resources that have alternate values for round watches.
            val resources = this@Face.resources
            val isRound = insets.isRound
            mXOffset = resources.getDimension(if (isRound)
                R.dimen.digital_x_offset_round
            else
                R.dimen.digital_x_offset)
            val textSize = resources.getDimension(if (isRound)
                R.dimen.digital_text_size_round
            else
                R.dimen.digital_text_size)
        }

        override fun onPropertiesChanged(properties: Bundle?) {
            super.onPropertiesChanged(properties)
            mLowBitAmbient = properties!!.getBoolean(WatchFaceService.PROPERTY_LOW_BIT_AMBIENT, false)
        }

        override fun onTimeTick() {
            super.onTimeTick()
            invalidate()
        }

        override fun onAmbientModeChanged(inAmbientMode: Boolean) {
            super.onAmbientModeChanged(inAmbientMode)
            if (mAmbient != inAmbientMode) {
                mAmbient = inAmbientMode
                invalidate()
            }

            // Whether the timer should be running depends on whether we're visible (as well as
            // whether we're in ambient mode), so we may need to start or stop the timer.
            updateTimer()
        }

        /**
         * Captures tap event (and tap type) and toggles the background color if the user finishes
         * a tap.
         */
        override fun onTapCommand(tapType: Int, x: Int, y: Int, eventTime: Long) {
            when (tapType) {
                WatchFaceService.TAP_TYPE_TOUCH -> {
                }
                WatchFaceService.TAP_TYPE_TOUCH_CANCEL -> {
                }
                WatchFaceService.TAP_TYPE_TAP ->
                    // The user has completed the tap gesture.
                    // TODO: Add code to handle the tap gesture.
                    Toast.makeText(applicationContext, R.string.message, Toast.LENGTH_SHORT)
                            .show()
            }// The user has started touching the screen.
            // The user has started a different gesture or otherwise cancelled the tap.
            invalidate()
        }

        override fun onDraw(canvas: Canvas, bounds: Rect) {
            // Draw the background.
            if (isInAmbientMode) {
                canvas.drawColor(Color.BLUE)
            } else {
//                PhoneBatteryClient.requestCharge(this@Face)
                canvas.drawRect(0f, 0f,
                        bounds.width().toFloat(), bounds.height().toFloat(), mBackgroundPaint)
                drawCalendar(canvas)
                drawCharge(canvas)
                drawBank(canvas)
            }

//            // Draw H:MM in ambient mode or H:MM:SS in interactive mode.
//            val now = System.currentTimeMillis()
//            mCalendar.timeInMillis = now
//
//            val text = if (mAmbient)
//                String.format("%d:%02d", mCalendar.get(Calendar.HOUR),
//                        mCalendar.get(Calendar.MINUTE))
//            else
//                String.format("%d:%02d:%02d", mCalendar.get(Calendar.HOUR),
//                        mCalendar.get(Calendar.MINUTE), mCalendar.get(Calendar.SECOND))
//            canvas.drawText(text, mXOffset, mYOffset, mTextPaint)
        }

        /**
         * Starts the [.mUpdateTimeHandler] timer if it should be running and isn't currently
         * or stops it if it shouldn't be running but currently is.
         */
        private fun updateTimer() {
            mUpdateTimeHandler.removeMessages(MSG_UPDATE_TIME)
            if (shouldTimerBeRunning()) {
                mUpdateTimeHandler.sendEmptyMessage(MSG_UPDATE_TIME)
            }
        }

        /**
         * Returns whether the [.mUpdateTimeHandler] timer should be running. The timer should
         * only run when we're visible and in interactive mode.
         */
        private fun shouldTimerBeRunning(): Boolean {
            return isVisible && !isInAmbientMode
        }

        /**
         * Handle updating the time periodically in interactive mode.
         */
        fun handleUpdateTimeMessage() {
            invalidate()
            if (shouldTimerBeRunning()) {
                val timeMs = System.currentTimeMillis()
                val delayMs = INTERACTIVE_UPDATE_RATE_MS - (timeMs % INTERACTIVE_UPDATE_RATE_MS)
                mUpdateTimeHandler.sendEmptyMessageDelayed(MSG_UPDATE_TIME, delayMs)
            }
        }

        fun drawCalendar(canvas: Canvas) {
            val now = System.currentTimeMillis()
            mCalendar.timeInMillis = now

            var text = String.format("%02d:%02d", mCalendar.get(Calendar.HOUR_OF_DAY),
                    mCalendar.get(Calendar.MINUTE))
            canvas.drawText(text, 60f, 80f, hoursPaint)

            text = String.format(":%02d", mCalendar.get(Calendar.SECOND))
            canvas.drawText(text, 210f, 80f, secondsPaint)

            text = String.format("%04d-%02d-%02d", mCalendar.get(Calendar.YEAR),
                    mCalendar.get(Calendar.MONTH) + 1, mCalendar.get(Calendar.DAY_OF_MONTH))
            canvas.drawText(text, 60f, 120f, datePaint)

            text = dayOfWeek[mCalendar.get(Calendar.DAY_OF_WEEK)]!!
            canvas.drawText(text, 225f, 120f, datePaint)
        }

        fun drawCharge(canvas: Canvas) {

            val batteryIntent = registerReceiver(
                    null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))

            val level = batteryIntent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)

            val wearableCharge = if (level != -1)
                String.format("%3d%%", level)
            else
                "??%"

            canvas.drawText(wearableCharge, 8f, 120f, battaryPaint)

            val settings = getSharedPreferences("balance", 0)
            val mobile_battery = settings.getString("mobile", "??") + "%"

            canvas.drawText(mobile_battery, 275f, 120f, battaryPaint)
        }

        fun drawBank(canvas: Canvas) {
            val settings = getSharedPreferences("balance", 0)

            val rate = settings.getString("rate", " 2.34/$")
            canvas.drawText(rate, 0f, 150f, dataPaint)

            val belinvest = settings.getString("belinvest_2", "----.--") + "p"
            canvas.drawText(belinvest, 0f, 175f, dataPaint)

            val prior = settings.getString("prior", "----.--") + "p"
            canvas.drawText(prior, 0f, 195f, dataPaint)

            val prior_internet = settings.getString("prior_internet", "----.--") + "$"
            canvas.drawText(prior_internet, 0f, 215f, dataPaint)
        }

    }

    companion object {
        private val NORMAL_TYPEFACE = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)

        /**
         * Update rate in milliseconds for interactive mode. We update once a second since seconds are
         * displayed in interactive mode.
         */
        private val INTERACTIVE_UPDATE_RATE_MS = TimeUnit.SECONDS.toMillis(1)

        /**
         * Handler message id for updating the time periodically in interactive mode.
         */
        private val MSG_UPDATE_TIME = 0
    }
}
