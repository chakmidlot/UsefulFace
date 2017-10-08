package com.chakmidlot.usefulface

import android.util.Log
import android.widget.Toast
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.wearable.DataEventBuffer
import com.google.android.gms.wearable.DataMapItem
import com.google.android.gms.wearable.Wearable
import com.google.android.gms.wearable.WearableListenerService
import java.util.concurrent.TimeUnit


class ListenerService : WearableListenerService() {

    val CONNECTION_TIME_OUT_MS = 300L

    override fun onDataChanged(dataEvents: DataEventBuffer) {
        Log.d("UsefulFace", "onDataChanged: " + dataEvents);
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    private fun reply(nodeId: String, message: String) {
        val client = GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .build()

        client.blockingConnect(CONNECTION_TIME_OUT_MS, TimeUnit.MILLISECONDS)
        Wearable.MessageApi.sendMessage(client, nodeId, message, null)
        client.disconnect()
    }

}
