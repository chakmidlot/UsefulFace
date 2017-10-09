package com.chakmidlot.usefulface

import android.app.Service
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import android.os.BatteryManager
import java.util.*

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

    private val calendar = Calendar.getInstance()

    fun prepare(): Map<String, String> {
        val settings = service.getSharedPreferences("balance", 0)

        return calendar() +
                charge() +
                bank(settings)
    }

    private fun calendar(): Map<String, String> {
        val now = System.currentTimeMillis()
        calendar.timeInMillis = now

        val hourMinute = String.format("%02d:%02d",
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE))

        val seconds = String.format(":%02d", calendar.get(Calendar.SECOND))

        val date = String.format("%04d-%02d-%02d",
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH) + 1,
                calendar.get(Calendar.DAY_OF_MONTH))

        val dayOfWeek = dayOfWeek[calendar.get(Calendar.DAY_OF_WEEK)]!!

        return mapOf(
                "current_hour_minute" to hourMinute,
                "current_seconds" to seconds,
                "current_date" to date,
                "current_day_of_week" to dayOfWeek
        )
    }

    private fun charge(): Map<String, String> {
        val batteryIntent = service.registerReceiver(
                null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))

        val level = batteryIntent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)

        val wearableCharge = if (level != -1)
            String.format("%3d%%", level)
        else
            "??%"

        val settings = service.getSharedPreferences("balance", 0)
        val mobileBattery = settings.getString("mobile", "??") + "%"

        return mapOf(
                "charge_wear" to wearableCharge,
                "charge_mobile" to mobileBattery
        )
    }

    private fun bank(settings: SharedPreferences): Map<String, String> {
        return mapOf(
                "currnecy_rate" to " " + settings.getString("rate", "-.----"),
                "balance_belinvest_main" to settings.getString("belinvest_2", "----.--") + "p",
                "balance_prior_main" to settings.getString("prior", "----.--") + "p",
                "prior_internet" to settings.getString("prior_internet", "----.--") + "$"
        )
    }
}
