package com.chakmidlot.usefulface

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.Log

import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.Node
import com.google.android.gms.wearable.WearableListenerService


class DataLayerListenerService : WearableListenerService() {

    override fun onCreate() {
        super.onCreate()
        Log.d("UsefulFace", "Listener started")
    }

    override fun onMessageReceived(messageEvent: MessageEvent) {
        super.onMessageReceived(messageEvent)
        Log.d("UsefulFace", "received a message from wear: " + messageEvent.path)
        // save the new heartbeat value
        currentValue = Integer.parseInt(messageEvent.path)
//        if (handler != null) {
//            // if a handler is registered, send the value as new message
//            handler!!.sendEmptyMessage(currentValue)
//        }
    }

    companion object {

//        private var handler: Handler? = null
        private var currentValue = 0
//
//        fun getHandler(): Handler? {
//            return handler
//        }
//
//        fun setHandler(handler: Handler?) {
//            DataLayerListenerService.handler = handler
//            // send current value as initial value.
//            handler?.sendEmptyMessage(currentValue)
//        }
    }


}