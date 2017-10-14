package com.chakmidlot.usefulface

import android.app.Service
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import android.os.BatteryManager
import android.provider.UserDictionary
import com.chakmidlot.usefulface.english.Words
import java.util.*

data class Time (val hour_minute: String, val seconds: String,
                     val date: String, val dayOfWeek: String)
data class Charge (val value: String, val level: String)
data class Balance (val value: String, val level: String)
data class Currency (val value: String)
data class DataStructure(val time: Time, val charge: List<Charge>,  val currency: Currency,
                         val bank: List<Balance>, val buses: List<Bus>,
                         val vocabulary: Pair<String, String>, val weather: List<String>)

class Data(private val service: Service) {

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
                weather(settings)
        )
    }

    fun touchUpdate(x: Int, y: Int) {
        if (y > 230) {
            parseVocabulary()
        }
    }

    private fun parseVocabulary() {
        if (vocabularyState == 0) {
            vocabularyState = 1
            vocabularyData = Pair(nextWord.first, "")
        }
        else {
            vocabularyState = 0
            vocabularyData = nextWord
            nextWord = vocabulary.next()
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
}
