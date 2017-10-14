package com.chakmidlot.usefulface

import android.content.Context
import android.util.Log
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.wearable.PutDataMapRequest
import com.google.android.gms.wearable.Wearable
import java.util.concurrent.TimeUnit


object Data {

    private val CONNECTION_TIME_OUT_MS = 300L

    fun save(context: Context, dataItem: String, value: String) {
        Thread(Runnable {
            val client = GoogleApiClient.Builder(context)
                    .addApi(Wearable.API)
                    .build()

            client.blockingConnect(CONNECTION_TIME_OUT_MS, TimeUnit.MILLISECONDS)
            val result = Wearable.NodeApi.getConnectedNodes(client).await()
            val nodes = result.nodes
            Log.d("UsefulFace", nodes.toString())
            if (nodes.size > 0) {
                val nodeId = nodes[0].id
                Log.d("UsefulFace", nodeId)
                val putDataMapReq = PutDataMapRequest.create(dataItem)
                putDataMapReq.getDataMap().putString("value", value)
                val putDataReq = putDataMapReq.asPutDataRequest()
                val result = Wearable.DataApi.putDataItem(client, putDataReq).await()
                Log.d("UsefulFace", result.toString())
            }
            client.disconnect()
        }).start()
    }
}