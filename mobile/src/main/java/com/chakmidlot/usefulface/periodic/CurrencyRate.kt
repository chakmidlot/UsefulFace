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
import java.io.BufferedInputStream
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL


class CurrencyRate : JobService() {

    private val mJobHandler = Handler(Handler.Callback { msg ->
        val url = URL("http://www.nbrb.by/API/ExRates/Currencies")
        val urlConnection = url.openConnection() as HttpURLConnection
        try {
            val input = BufferedReader(InputStreamReader(urlConnection.getInputStream()))
            val line = input.readLine()
            val rate = Regex("\"Cur_OfficialRate\":([\\d.]+)").find(line)!!.groups[1]!!.value
            Data.save(this, "/balance/rate", rate)
        } finally {
            urlConnection.disconnect()
        }
//        Data.save(this, "/balance/mobile", level.toString())

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
