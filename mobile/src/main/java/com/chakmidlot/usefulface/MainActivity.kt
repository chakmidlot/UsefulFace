package com.chakmidlot.usefulface

import android.Manifest
import android.app.job.JobInfo
import android.app.job.JobScheduler
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.util.Log

import kotlinx.android.synthetic.main.activity_main.*
import android.content.pm.PackageManager
import android.net.Uri
import android.os.BatteryManager
import android.widget.Toast
import com.chakmidlot.usefulface.periodic.BatteryService
import com.google.android.gms.wearable.MessageApi
import com.google.android.gms.wearable.MessageEvent


class MainActivity : AppCompatActivity(), MessageApi.MessageListener {

    private lateinit var mJobScheduler: JobScheduler

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)
        Log.d("UsefulFace", "Start my super activity")

        mJobScheduler = getSystemService( Context.JOB_SCHEDULER_SERVICE ) as JobScheduler

        val hasPermission = checkSelfPermission(Manifest.permission.READ_SMS)
        if (hasPermission != PackageManager.PERMISSION_GRANTED) {
            Log.d("UsefulFace", "Ask sms")
            requestPermissions(arrayOf(Manifest.permission.READ_SMS),
                    123)
            return
        }

        val granted = checkSelfPermission(Manifest.permission.RECEIVE_SMS) == PackageManager.PERMISSION_GRANTED
        Log.d("UsefulFace", "Can recieve: " + granted)
        if (checkSelfPermission(Manifest.permission.RECEIVE_SMS) != PackageManager.PERMISSION_GRANTED) {
            Log.d("UsefulFace", "Ask sms")
            requestPermissions(arrayOf(Manifest.permission.RECEIVE_SMS),
                    124)
            return
        }

        val batteryIntent = registerReceiver(
                null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))

        val level = batteryIntent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)

        Log.d("UsefulFace", level.toString())

//        readSmsBelinvest()
//        readSmsPrior()
//        readSmsPriorInternet()
        schedule()

        fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show()
        }
    }

    private fun readSmsBelinvest() {
        val cursor = contentResolver.query(Uri.parse("content://sms/inbox"),
                null, "address = 'Belinvest'", null, null)

        while (cursor.moveToNext()) {
            val message = cursor.getString(cursor.getColumnIndex("body"))
            Log.d("UsefulFace", message)
            val balance = SmsParser.belinvest(message)
            if (balance.first != "") {
                Log.d("UsefulFace", "Balance: ${balance}")
                Data.save(this, "/balance/${balance.first}", balance.second)
                break
            }
        }
        cursor.close()
    }

    private fun readSmsPrior() {
        val cursor = contentResolver.query(Uri.parse("content://sms/inbox"),
                null, "address = 'Priorbank'", null, null)

        while (cursor.moveToNext()) {
            val message = cursor.getString(cursor.getColumnIndex("body"))
            Log.d("UsefulFace", message)
            val balance = SmsParser.prior(message)
            if (balance.first == "prior_main") {
                Log.d("UsefulFace", "Balance: ${balance}")
                Data.save(this, "/balance/${balance.first}", balance.second)
                break
            }
        }
        cursor.close()
    }

    private fun readSmsPriorInternet() {
        val cursor = contentResolver.query(Uri.parse("content://sms/inbox"),
                null, "address = 'Priorbank'", null, null)

        while (cursor.moveToNext()) {
            val message = cursor.getString(cursor.getColumnIndex("body"))
            Log.d("UsefulFace", message)
            val balance = SmsParser.prior(message)
            if (balance.first == "prior_internet") {
                Log.d("UsefulFace", "Balance: ${balance}")
                Data.save(this, "/balance/${balance.first}", balance.second)
                break
            }
        }
        cursor.close()
    }

    private fun schedule() {
        Log.d("UsefulFace", "scheduling")
        val builder = JobInfo.Builder(1,
                ComponentName(packageName, BatteryService::class.java!!.getName()))

        builder.setPeriodic(5 * 60 * 1000)


        if (mJobScheduler.schedule(builder.build()) != JobScheduler.RESULT_SUCCESS) {
            Log.d("UsefulFace", "failed scheduling")
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            123 -> if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission Granted
                readSmsBelinvest()
            }
            124 -> if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission Granted
                Log.d("UsefulFace", "ReadingSMS")
            }
            else {
                // Permission Denied
                Toast.makeText(this@MainActivity, "READ_SMS Denied", Toast.LENGTH_SHORT)
                        .show()
            }
            else -> super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }

    override fun onMessageReceived(messageEvent: MessageEvent) {
        Log.d("UsefulFace", "Message received")
//        if (messageEvent.path == "/phone_charge") {
//            val startIntent = Intent(this, MainActivity::class.java)
//            startIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
//            startIntent.putExtra("VOICE_DATA", messageEvent.data)
//            startActivity(startIntent)
//        }
    }

}