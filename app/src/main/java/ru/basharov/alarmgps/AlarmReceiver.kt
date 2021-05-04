package ru.basharov.alarmgps

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        var getresult: String = intent!!.getStringExtra("extra").toString()

        var serviceIntent: Intent = Intent(context, RingtoneService::class.java)
        serviceIntent.putExtra("extra", getresult)
        context!!.startService(serviceIntent)
    }
}