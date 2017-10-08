package com.chakmidlot.usefulface.periodic

import android.app.job.JobParameters
import android.app.job.JobService
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Handler
import android.os.Message
import android.util.Log
import com.chakmidlot.usefulface.Data


class Weather : JobService() {

    private val mJobHandler = Handler(Handler.Callback { msg ->
        val batteryIntent = registerReceiver(
                null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))

        val level = batteryIntent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
        Data.save(this, "/balance/mobile", level.toString())

        jobFinished(msg.obj as JobParameters, true)
        Log.d("UsefulFace", "periodic tasks")
        true
    })

    override fun onStartJob(params: JobParameters): Boolean {
        mJobHandler.sendMessage(Message.obtain(mJobHandler, 1, params))
        return false
    }

    override fun onStopJob(params: JobParameters): Boolean {
        mJobHandler.removeMessages(1)
        return false
    }

}
