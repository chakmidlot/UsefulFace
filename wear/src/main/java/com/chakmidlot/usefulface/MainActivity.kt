package com.chakmidlot.usefulface

import android.Manifest
import android.os.Bundle
import android.support.wearable.activity.WearableActivity
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.content.pm.PackageManager
import android.support.v4.app.ActivityCompat
import android.support.v4.app.ActivityCompat.requestPermissions
import android.util.Log


class MainActivity : WearableActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Enables Always-on
        setAmbientEnabled()

        val calendarGranted = checkSelfPermission(Manifest.permission.READ_CALENDAR) == PackageManager.PERMISSION_GRANTED
        if (checkSelfPermission(Manifest.permission.READ_CALENDAR) != PackageManager.PERMISSION_GRANTED) {
            Log.d("UsefulFace", "Ask sms")
            requestPermissions(arrayOf(Manifest.permission.READ_CALENDAR),
                    125)
            return
        }

        NearestEvents.readCalendar(contentResolver)

    }

}
