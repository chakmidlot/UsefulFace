package com.chakmidlot.usefulface

import android.util.Log
import com.google.android.gms.wearable.DataEvent
import com.google.android.gms.wearable.DataEventBuffer
import com.google.android.gms.wearable.DataMapItem
import com.google.android.gms.wearable.WearableListenerService

class DataItemsListener : WearableListenerService() {

    val BELINVEST_PATH = "/balance/belinvest_main"
    val PRIORBANK_MAIN_PATH = "/balance/prior_main"
    val PRIORBANK_INTERNET_PATH = "/balance/prior_internet"
    val PHONE_BATTERY = "/balance/phone_battery"

    override fun onDataChanged(dataEvents: DataEventBuffer) {
        Log.d("UsefulFace", "onDataChanged: " + dataEvents)

        for (event in dataEvents) {
            try {
                Log.d("UsefulFace", event.toString())
                if (event.getType() == DataEvent.TYPE_CHANGED) {
                    val path = event.getDataItem().getUri().getPath();
                    Log.d("UsefulFace", path)

                    val dataMap = DataMapItem.fromDataItem(event.getDataItem()).getDataMap();
                    Log.v("myTag", "DataMap received on watch: " + dataMap)

                    val settings = getSharedPreferences("balance", 0)
                    val editor = settings.edit()
                    val balance = dataMap.getString("value").padStart(6)
                    when (path) {
                        BELINVEST_PATH -> editor.putString("belinvest_2", balance)
                        PRIORBANK_MAIN_PATH -> editor.putString("prior", balance)
                        PRIORBANK_INTERNET_PATH -> editor.putString("prior_internet", balance)
                        PHONE_BATTERY -> editor.putString("mobile", balance)
                    }
                    editor.apply()
                }
            }
            catch (e: Exception) {
                Log.w("UsefulFace", e.toString())
            }
        }
    }
}
