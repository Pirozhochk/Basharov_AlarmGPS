package ru.basharov.alarmgps

import android.app.Service
import android.content.Intent
import android.media.Ringtone
import android.media.RingtoneManager
import android.net.Uri
import android.os.IBinder

class RingtoneService : Service() {

    companion object{
        lateinit var ring: Ringtone
    }

    var id: Int = 0
    var isRunning: Boolean = false

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        var state: String = intent!!.getStringExtra("extra").toString()
        assert(state != null)
        when(state) {
            "on" -> id = 1
            "off" -> id = 0
        }

        if (!this.isRunning && id == 1) {
            playAlarm()
            this.isRunning = true
            this.id = 0
        }
        else if (this.isRunning && id == 0) {
            ring.stop()
            this.isRunning = false
            this.id = 0
        }
        else if (!this.isRunning && id == 1) {
            this.isRunning = false
            this.id = 0
        }
        else if (this.isRunning && id == 1) {
            this.isRunning = true
            this.id = 1
        }
        else{
            //пусто
        }
        return START_NOT_STICKY
    }

    private fun playAlarm() {
        var alarmUri: Uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
        if (alarmUri == null){
            alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        }
        ring = RingtoneManager.getRingtone(baseContext, alarmUri)
        ring.play()
    }
}