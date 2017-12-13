package com.chakmidlot.usefulface

import android.Manifest
import android.app.Activity
import android.app.AlarmManager
import android.app.Service
import android.content.*
import android.content.pm.PackageManager
import android.os.BatteryManager
import android.os.Vibrator
import android.provider.CalendarContract
import android.support.v4.app.ActivityCompat
import android.util.Log
import com.chakmidlot.usefulface.english.Words
import com.chakmidlot.usefulface.timer.FaceTimer
import java.util.*
import android.content.Context.ALARM_SERVICE




data class Time (val hour_minute: String, val seconds: String,
                     val date: String, val dayOfWeek: String)
data class Charge (val value: String, val level: String)
data class Balance (val value: String, val level: String)
data class Currency (val value: String)
data class Stopwatch (val value: String, val state: Int)
data class DataStructure(val time: Time, val charge: List<Charge>,  val currency: Currency,
                         val bank: List<Balance>, val buses: List<Bus>,
                         val vocabulary: Pair<String, String>, val weather: List<String>,
                         val stopwatch: Stopwatch, val events: List<Pair<String, String>>)

class Data(private val service: Service, private val timer: FaceTimer) {

    private val dayOfWeek = hashMapOf(
            1 to "SU",
            2 to "MO",
            3 to "TU",
            4 to "WE",
            5 to "TH",
            6 to "FR",
            7 to "SA"
    )

    private val vocabulary = Words()
    private var nextWord: Pair<String, String>
    private var vocabularyState = 0
    private var vocabularyData: Pair<String, String>

    private val schedule = Schedule()

    private val calendar = Calendar.getInstance()
    private var stopwatchStart = 0L
    private var stopwatchStop = 0L

    private var calendarEvents = mutableListOf<Pair<String, String>>()

    init {
        nextWord = vocabulary.next()
        vocabularyData = Pair(nextWord.first, "")
    }

    fun prepare(): DataStructure {
        val settings = service.getSharedPreferences("balance", 0)

        val now = System.currentTimeMillis()
        calendar.timeInMillis = now

        return DataStructure(
                calendar(),
                charge(),
                currnecy(settings),
                bank(settings),
                bus(),
                vocabulary(),
                weather(settings),
                stopwatch(),
                calendarEvents
        )
    }

    fun touchUpdate(x: Int, y: Int, isLongTap: Boolean) {
        Log.d("UsefulFace", "Touch: ($x, $y)")
        if (y > 230) {
            parseVocabulary(x > 140)
        }
        if (x > 200 && y > 130 && y < 190) {
            clickStopwatch()
        }
        timer.click(x, y, isLongTap)
    }

    fun updateVisible() {
        DataIteming.save(service, "/requests/calendar", calendar.timeInMillis.toString())
//        Thread(Runnable {
//            calendarEvents = NearestEvents.readCalendar(service.contentResolver)
//            Log.d("UsefulFace", calendarEvents.toString())
//        }).start()
    }

    private fun parseVocabulary(clickedYes: Boolean) {
        Log.d("UsefulFace", "ClieckedYes: $clickedYes")
        if (vocabularyState == 0) {
            nextWord = vocabulary.next(clickedYes)
            vocabularyState = 1
            vocabularyData = Pair(nextWord.first, "")
        }
        else {
            vocabularyState = 0
            vocabularyData = nextWord
        }
    }

    private fun clickStopwatch() {
        when {
            stopwatchStart == 0L -> stopwatchStart = calendar.timeInMillis / 1000
            stopwatchStop == 0L -> stopwatchStop = calendar.timeInMillis / 1000
            else -> {
                stopwatchStop = 0L
                stopwatchStart = 0L
            }
        }
    }

    private fun calendar(): Time {

        val hourMinute = String.format("%02d:%02d",
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE))

        val seconds = String.format(":%02d", calendar.get(Calendar.SECOND))

        val date = String.format("%04d-%02d-%02d",
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH) + 1,
                calendar.get(Calendar.DAY_OF_MONTH))

        val dayOfWeek = dayOfWeek[calendar.get(Calendar.DAY_OF_WEEK)]!!

        return Time(hourMinute, seconds, date, dayOfWeek)
    }

    private fun charge(): List<Charge> {
        val batteryIntent = service.registerReceiver(
                null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))

        val level = batteryIntent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)

        val wearableCharge = if (level != -1)
            String.format("%3d%%", level)
        else
            "??%"

        val settings = service.getSharedPreferences("balance", 0)
        val mobileBattery = settings.getString("mobile", "??") + "%"

        return listOf(
                Charge(wearableCharge, "OK"),
                Charge(mobileBattery, "OK")
        )
    }

    private fun currnecy(settings: SharedPreferences): Currency {
        return Currency(" " + settings.getString("rate", "-.----"))
    }

    private fun bank(settings: SharedPreferences): List<Balance> {
        return listOf(
                Balance(settings.getString("belinvest_2", "----.--") + "p", "OK"),
                Balance(settings.getString("prior", "----.--") + "p", "OK"),
                Balance(settings.getString("prior_internet", "----.--") + "$", "OK")
        )
    }

    private fun bus(): List<Bus> {
        val current_minute = calendar.get(Calendar.HOUR_OF_DAY) * 60 +
                calendar.get(Calendar.MINUTE)

        return schedule.getNearests(current_minute)
    }

    private fun vocabulary(): Pair<String, String> {
        return vocabularyData
    }

    private fun weather(settings: SharedPreferences): List<String> {
        val weatherData = settings.getString("weather", "")

        return if (weatherData != "") {
            weatherData.split("\n")
        } else {
            listOf(
                    "-- --",
                    "-- --")
        }
    }

    private fun stopwatch(): Stopwatch {
        return if (stopwatchStart == 0L) {
            Stopwatch("00:00:00", 0)
        }
        else {
            val end: Long
            val state: Int
            if (stopwatchStop == 0L) {
                end = calendar.timeInMillis / 1000
                state = 1
            } else {
                end = stopwatchStop
                state = 2
            }
            val value = end - stopwatchStart
            Stopwatch(
                    "%02d:%02d:%02d".format(value / 3600, (value / 60) % 60, value % 60),
                    state)
        }
    }
}
