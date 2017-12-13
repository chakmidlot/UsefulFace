package com.chakmidlot.usefulface.timer

import android.app.IntentService
import android.content.Intent
import android.content.Context
import android.os.Vibrator

/**
 * An [IntentService] subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 *
 *
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
class Juj : IntentService("Juj") {

    override fun onHandleIntent(intent: Intent?) {

        val vibrator: Vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        vibrator.vibrate(longArrayOf(0, 500, 100, 500), -1)
    }
}
