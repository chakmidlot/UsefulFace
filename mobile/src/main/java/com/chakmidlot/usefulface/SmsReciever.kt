package com.chakmidlot.usefulface

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.telephony.SmsMessage
import android.util.Log


class SmsReciever : BroadcastReceiver() {

    private val ACTION = "android.provider.Telephony.SMS_RECEIVED"

    override fun onReceive(context: Context, intent: Intent?) {
        Log.d("UsefulFace", "sms")
        if (intent != null && intent.action != null &&
                ACTION.compareTo(intent.action, true) == 0) {
            val pduArray = intent.extras.get("pdus") as Array<Any>

            val message = SmsMessage.createFromPdu(pduArray[0] as ByteArray)
            val balance = SmsParser.parse(message.displayOriginatingAddress, message.messageBody)
            if (balance.first != "") {
                Data.save(context, "/balance/" + balance.first, balance.second)
            }
        }
    }
}
