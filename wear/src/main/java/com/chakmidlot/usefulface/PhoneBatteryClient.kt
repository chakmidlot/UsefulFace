package com.chakmidlot.usefulface

import android.content.Context
import android.util.Log
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.wearable.CapabilityInfo
import com.google.android.gms.wearable.Node
import com.google.android.gms.wearable.PutDataMapRequest
import com.google.android.gms.wearable.Wearable
import java.util.concurrent.TimeUnit


object PhoneBatteryClient {

    val CONNECTION_TIME_OUT_MS = 300L
    val MESSAGE = "/data-item-received"

    var charge = -1

    var lastCheckTimestamp = 0L

    private fun sendRequest(context: Context, nodeId: String?) {
        val client = getGoogleApiClient(context)
        if (nodeId != null) {
            Thread(Runnable {
                client.blockingConnect(CONNECTION_TIME_OUT_MS, TimeUnit.MILLISECONDS)
                Wearable.MessageApi.sendMessage(client, nodeId, MESSAGE, null)
                client.disconnect()
            }).start()
        }
    }

    fun requestCharge(context: Context) {
        lastCheckTimestamp = System.currentTimeMillis()
        Log.d("UsefulFace", "Send request to phone")

        val client = getGoogleApiClient(context)
        Thread(Runnable {
            client.blockingConnect(CONNECTION_TIME_OUT_MS, TimeUnit.MILLISECONDS)
            val result = Wearable.NodeApi.getConnectedNodes(client).await()
            val nodes = result.nodes
            Log.d("UsefulFace", nodes.toString())
            if (nodes.size > 0) {
                val nodeId = nodes[0].id
                Log.d("UsefulFace", nodeId)
//                Wearable.MessageApi.sendMessage(client, nodeId, MESSAGE, null)

                val putDataMapReq = PutDataMapRequest.create("/data-item-received")
                putDataMapReq.getDataMap().putInt("abc", 123)
                val putDataReq = putDataMapReq.asPutDataRequest()
                val result = Wearable.DataApi.putDataItem(client, putDataReq).await()
                Log.d("UsefulFace", result.toString())

            }
            client.disconnect()
        }).start()
    }

    private fun getGoogleApiClient(context: Context): GoogleApiClient {
        return GoogleApiClient.Builder(context)
                .addApi(Wearable.API)
                .build()
    }

}