package com.chakmidlot.usefulface

import android.Manifest
import android.annotation.SuppressLint
import android.app.job.JobInfo
import android.app.job.JobScheduler
import android.content.*
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.util.Log

import kotlinx.android.synthetic.main.activity_main.*
import android.content.pm.PackageManager
import android.net.Uri
import android.os.BatteryManager
import android.provider.CalendarContract
import android.provider.CalendarContract.Events
import android.provider.CalendarContract.Calendars
import android.provider.CalendarContract.Instances
import android.widget.Toast
import com.chakmidlot.usefulface.periodic.BatteryService
import com.chakmidlot.usefulface.periodic.CurrencyRate
import com.chakmidlot.usefulface.periodic.WeatherService
import com.chakmidlot.usefulface.periodic.get_forecast
import java.util.*


class MainActivity : AppCompatActivity() {

    private lateinit var mJobScheduler: JobScheduler

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)
        Log.d("UsefulFace", "Start my super activity")

        mJobScheduler = getSystemService( Context.JOB_SCHEDULER_SERVICE ) as JobScheduler

        val hasPermission = checkSelfPermission(Manifest.permission.READ_SMS)
        if (hasPermission != PackageManager.PERMISSION_GRANTED) {
            Log.d("UsefulFace", "Ask sms")
            requestPermissions(arrayOf(Manifest.permission.READ_SMS),
                    123)
            return
        }

        val granted = checkSelfPermission(Manifest.permission.RECEIVE_SMS) == PackageManager.PERMISSION_GRANTED
        Log.d("UsefulFace", "Can recieve: " + granted)
        if (checkSelfPermission(Manifest.permission.RECEIVE_SMS) != PackageManager.PERMISSION_GRANTED) {
            Log.d("UsefulFace", "Ask sms")
            requestPermissions(arrayOf(Manifest.permission.RECEIVE_SMS),
                    124)
            return
        }

        val calendarGranted = checkSelfPermission(Manifest.permission.READ_CALENDAR) == PackageManager.PERMISSION_GRANTED
        Log.d("UsefulFace", "Can recieve: " + granted)
        if (checkSelfPermission(Manifest.permission.READ_CALENDAR) != PackageManager.PERMISSION_GRANTED) {
            Log.d("UsefulFace", "Ask sms")
            requestPermissions(arrayOf(Manifest.permission.READ_CALENDAR),
                    125)
            return
        }


        val serviceIntent = Intent(this, DataLayerListenerService::class.java)
        startService(serviceIntent)

        val batteryIntent = registerReceiver(
                null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))

        val level = batteryIntent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)

        Log.d("UsefulFace", level.toString())

//        readSmsBelinvest()
//        readSmsPrior()
//        readSmsPriorInternet()
        calendarData()
        Thread(Runnable {
            Log.d("UsefulFace", get_forecast())
            Data.save(this, "/balance/weather", get_forecast())
        }).start()

        schedule()

//        fab.setOnClick{ view ->
//            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                    .setAction("Action", null).show()
//        }

    }

    @SuppressLint("MissingPermission")
    private fun calendarData() {

        val now = Calendar.getInstance()

        val calendars = contentResolver.query(Calendars.CONTENT_URI,
                arrayOf(Calendars._ID, Calendars.ACCOUNT_NAME, Calendars.CALENDAR_DISPLAY_NAME),
                null , null, null)

        while (calendars.moveToNext()) {
            Log.d("UsefulFace", "${calendars.getInt(0)}, " +
                    "${calendars.getString(1)}, ${calendars.getString(2)}")
            calendars.moveToNext()
        }
        calendars.close()

        val uriBuilder = Instances.CONTENT_URI.buildUpon()
        ContentUris.appendId(uriBuilder, now.timeInMillis)
        ContentUris.appendId(uriBuilder, now.timeInMillis + (3600_000 * 24 * 7))

        val events = contentResolver.query(uriBuilder.build(),
                arrayOf(Instances.TITLE, Instances.BEGIN, Instances.CALENDAR_DISPLAY_NAME),
                "${Instances.CALENDAR_DISPLAY_NAME} in ('d_tolkach@indatalabs.com', 'chakmidlot@gmail.com')",
                null, Instances.BEGIN)
        Log.d("UsefulFace", events.columnNames.joinToString(", "))

        while (events.moveToNext()) {
            Log.d("UsefulFace", "${events.getLong(1)} ${events.getString(2)} ${events.getString(0)}")
        }
        events.close()
    }

    private fun readSmsBelinvest() {
        val cursor = contentResolver.query(Uri.parse("content://sms/inbox"),
                null, "address = 'Belinvest'", null, null)

        while (cursor.moveToNext()) {
            val message = cursor.getString(cursor.getColumnIndex("body"))
            Log.d("UsefulFace", message)
            val balance = SmsParser.belinvest(message)
            if (balance.first != "") {
                Log.d("UsefulFace", "Balance: ${balance}")
                Data.save(this, "/balance/${balance.first}", balance.second)
                break
            }
        }
        cursor.close()
    }

    private fun readSmsPrior() {
        val cursor = contentResolver.query(Uri.parse("content://sms/inbox"),
                null, "address = 'Priorbank'", null, null)

        while (cursor.moveToNext()) {
            val message = cursor.getString(cursor.getColumnIndex("body"))
            Log.d("UsefulFace", message)
            val balance = SmsParser.prior(message)
            if (balance.first == "prior_main") {
                Log.d("UsefulFace", "Balance: ${balance}")
                Data.save(this, "/balance/${balance.first}", balance.second)
                break
            }
        }
        cursor.close()
    }

    private fun readSmsPriorInternet() {
        val cursor = contentResolver.query(Uri.parse("content://sms/inbox"),
                null, "address = 'Priorbank'", null, null)

        while (cursor.moveToNext()) {
            val message = cursor.getString(cursor.getColumnIndex("body"))
            Log.d("UsefulFace", message)
            val balance = SmsParser.prior(message)
            if (balance.first == "prior_internet") {
                Log.d("UsefulFace", "Balance: ${balance}")
                Data.save(this, "/balance/${balance.first}", balance.second)
                break
            }
        }
        cursor.close()
    }

    private fun schedule() {
        Log.d("UsefulFace", "scheduling")
        val builder = JobInfo.Builder(1,
                ComponentName(packageName, BatteryService::class.java!!.name))

        builder.setPeriodic(600_000)

        if (mJobScheduler.schedule(builder.build()) != JobScheduler.RESULT_SUCCESS) {
            Log.d("UsefulFace", "failed scheduling")
        }

        val scheduled = mJobScheduler.schedule(JobInfo.Builder(2, ComponentName(this, WeatherService::class.java))
                .setPeriodic(3600_000)
                .build())

        Log.d("UsefulFace", "Weather scheduled: $scheduled")

    }

    private fun scheduleRate() {
        Log.d("UsefulFace", "scheduling")
        val builder = JobInfo.Builder(1,
                ComponentName(packageName, CurrencyRate::class.java!!.name))

        builder.setPeriodic(10 * 1000)


        if (mJobScheduler.schedule(builder.build()) != JobScheduler.RESULT_SUCCESS) {
            Log.d("UsefulFace", "failed scheduling")
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            123 -> if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                readSmsBelinvest()
            }
            124 -> if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d("UsefulFace", "ReadingSMS")
            }
            125 -> if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d("UsefulFace", "ReadingCalendar")
                calendarData()
            }
            else {
                // Permission Denied
                Toast.makeText(this@MainActivity, "READ_SMS Denied", Toast.LENGTH_SHORT)
                        .show()
            }
            else -> super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }

}
