package ru.basharov.alarmgps

import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.media.Ringtone
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat

class RingtoneService : Service() {
    companion object{
        lateinit var ring: Ringtone
    }

    var id: Int = 0
    var isRunning: Boolean = false
    private var CHANNEL_ID = "53"

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
            fireNotification()
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

    private fun fireNotification() {
        var mainActivityIntent: Intent = Intent(this, MainActivity::class.java)
        var pi: PendingIntent = PendingIntent.getActivity(this, 0, mainActivityIntent, 0)
        var defSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        var notifyManager: NotificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_LOW
            val notificationChannel =
                NotificationChannel("53","my_channel", importance)
            notificationChannel.enableLights(true)
            notificationChannel.lightColor = Color.RED
            notificationChannel.enableVibration(true)
            notificationChannel.vibrationPattern = longArrayOf(
                100,
                200,
                300,
                400,
                500,
                400,
                300,
                200,
                400
            )
            notifyManager.createNotificationChannel(notificationChannel)
        }

        var notification: Notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Будильник сработал")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setSound(defSoundUri)
            .setContentText("Нажми сюда")
            .setContentIntent(pi)
            .setAutoCancel(true)
            .build()
        notifyManager.notify(0, notification)
    }

    private fun playAlarm() {
        var alarmUri: Uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
        if (alarmUri == null){
            alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        }
        ring = RingtoneManager.getRingtone(baseContext, alarmUri)
        ring.play()
    }

    override fun onDestroy() {
        super.onDestroy()
        this.isRunning = false
    }
}