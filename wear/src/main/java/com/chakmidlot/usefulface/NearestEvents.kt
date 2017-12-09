package com.chakmidlot.usefulface

import android.annotation.SuppressLint
import android.content.ContentResolver
import android.content.ContentUris
import android.provider.CalendarContract
import android.util.Log
import java.util.*


object NearestEvents {

    @SuppressLint("MissingPermission")
    fun readCalendar(contentResolver: ContentResolver): MutableList<Pair<String, String>> {
        val now = Calendar.getInstance()

        val calendars = contentResolver.query(CalendarContract.Calendars.CONTENT_URI,
                arrayOf(CalendarContract.Calendars._ID, CalendarContract.Calendars.ACCOUNT_NAME, CalendarContract.Calendars.CALENDAR_DISPLAY_NAME),
                null , null, null)

        while (calendars.moveToNext()) {
            Log.d("UsefulFace", "${calendars.getInt(0)}, " +
                    "${calendars.getString(1)}, ${calendars.getString(2)}")
        }
        calendars.close()


        val uriBuilder = CalendarContract.Instances.CONTENT_URI.buildUpon()
        ContentUris.appendId(uriBuilder, now.timeInMillis - 300_000)
        ContentUris.appendId(uriBuilder, now.timeInMillis + (3600_000 * 24 * 7))

        val events = contentResolver.query(uriBuilder.build(),
                arrayOf(CalendarContract.Instances.TITLE, CalendarContract.Instances.BEGIN, CalendarContract.Instances.CALENDAR_DISPLAY_NAME),
                "${CalendarContract.Instances.CALENDAR_DISPLAY_NAME} in ('d_tolkach@indatalabs.com', 'chakmidlot@gmail.com')",
                null, CalendarContract.Instances.BEGIN)

        val mutableList = mutableListOf<Pair<String, String>>()
        var i = 0
        while (events.moveToNext() && i++ < 5) {
            now.timeInMillis = events.getLong(1)
            val time = "{:02}:{:02}".format(now.get(Calendar.HOUR_OF_DAY), now.get(Calendar.MINUTE))
            mutableList.add(Pair(time, events.getString(0)))
        }
        events.close()

        return mutableList
    }
}
