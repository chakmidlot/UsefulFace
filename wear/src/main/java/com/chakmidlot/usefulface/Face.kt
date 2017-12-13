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
import com.chakmidlot.usefulface.timer.FaceTimer
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient

import java.lang.ref.WeakReference
import java.util.Calendar
import java.util.TimeZone
import java.util.concurrent.TimeUnit

class Face : CanvasWatchFaceService(), GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private val TAG = "UsefulFace"

    override fun onConnectionFailed(p0: ConnectionResult) {
        Log.w(TAG, "Connection Failed")
    }

    override fun onConnectionSuspended(p0: Int) {
        Log.w(TAG, "Connection Suspended")
    }

    override fun onConnected(p0: Bundle?) {
        Log.d(TAG, "API Client connected")
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
        private lateinit var data: Data
        private lateinit var drawing: Drawing

        private var mAmbient: Boolean = false
        private lateinit var mCalendar: Calendar
        private val mTimeZoneReceiver: BroadcastReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                mCalendar.timeZone = TimeZone.getDefault()
                invalidate()
            }
        }

        /**
         * Whether the display supports fewer bits for each color in ambient mode. When true, we
         * disable anti-aliasing in ambient mode.
         */
        private var mLowBitAmbient: Boolean = false

        private lateinit var drawingCase: HashMap<String, Paint>

        override fun onCreate(holder: SurfaceHolder?) {
            super.onCreate(holder)

            val timer = FaceTimer(this@Face)
            mCalendar = Calendar.getInstance()
            data = Data(this@Face, timer)
            drawing = Drawing(timer)

            setWatchFaceStyle(WatchFaceStyle.Builder(this@Face)
                    .setAcceptsTapEvents(true)
                    .build())
        }

        override fun onDestroy() {
            mUpdateTimeHandler.removeMessages(MSG_UPDATE_TIME)
            Log.d("UsefulFace", "Destroy")
            super.onDestroy()
        }

        override fun onVisibilityChanged(visible: Boolean) {
            super.onVisibilityChanged(visible)


            if (visible) {
                data.updateVisible()
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
//            val resources = this@Face.resources
//            val isRound = insets.isRound
//            mXOffset = resources.getDimension(if (isRound)
//                R.dimen.digital_x_offset_round
//            else
//                R.dimen.digital_x_offset)
//            val textSize = resources.getDimension(if (isRound)
//                R.dimen.digital_text_size_round
//            else
//                R.dimen.digital_text_size)
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
                WatchFaceService.TAP_TYPE_TOUCH -> { }
                WatchFaceService.TAP_TYPE_TOUCH_CANCEL -> data.touchUpdate(x, y, true)
                WatchFaceService.TAP_TYPE_TAP -> data.touchUpdate(x, y, false)
            }// The user has started touching the screen.
            // The user has started a different gesture or otherwise cancelled the tap.
            invalidate()
        }

        override fun onDraw(canvas: Canvas, bounds: Rect) {
            val settings = getSharedPreferences("balance", 0)
            if (isInAmbientMode) {
                canvas.drawColor(Color.BLUE)
            } else {
                val drawing_data = data.prepare()
                drawing.draw(canvas, bounds, drawing_data)
            }
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
    }

    companion object {
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
