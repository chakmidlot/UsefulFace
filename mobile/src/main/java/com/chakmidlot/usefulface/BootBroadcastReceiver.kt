package com.chakmidlot.usefulface

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class BootBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        // BOOT_COMPLETED” start Service
        if (intent.getAction().equals(ACTION)) {
            //Service
            val serviceIntent = Intent(context, DataLayerListenerService::class.java)
            context.startService(serviceIntent)
        }
    }

    companion object {
        internal val ACTION = "android.intent.action.BOOT_COMPLETED"
    }
}