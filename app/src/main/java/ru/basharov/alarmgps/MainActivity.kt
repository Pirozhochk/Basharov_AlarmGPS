package ru.basharov.alarmgps

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.TimePicker
import androidx.appcompat.app.AppCompatActivity
import java.util.*

class MainActivity : AppCompatActivity() {

    lateinit var am: AlarmManager
    lateinit var tp: TimePicker
    lateinit var alarmTxt: TextView
    lateinit var con: Context
    lateinit var btnStart: Button
    lateinit var btnEnd: Button
    var hour: Int = 0
    var minute: Int = 0
    lateinit var pi:PendingIntent

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        this.con = this
        am = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        tp = findViewById(R.id.TimePicker)
        alarmTxt = findViewById(R.id.alarmTxt)
        alarmTxt.setText(R.string.txtAlarm)
        btnStart = findViewById(R.id.StartAlarm)
        btnEnd = findViewById(R.id.EndAlarm)

        var calendar: Calendar = Calendar.getInstance()
        var myIntent: Intent = Intent(this, AlarmReceiver::class.java)
        btnStart.setOnClickListener{
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                calendar.set(Calendar.HOUR_OF_DAY,tp.hour)
                calendar.set(Calendar.MINUTE, tp.minute)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
                hour = tp.hour
                minute = tp.minute
            }
            else{
                calendar.set(Calendar.HOUR_OF_DAY,tp.currentHour)
                calendar.set(Calendar.MINUTE, tp.currentMinute)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
                hour = tp.currentHour
                minute = tp.currentMinute
            }

            var hrStr: String = hour.toString()
            var minStr: String = minute.toString()
            if(hour > 12)
                hrStr = (hour - 12).toString()
            if (minute < 10)
                minStr = "0$minute"
            alarmTxt.setText("Будильник установлен на $hrStr:$minStr")

            myIntent.putExtra("extra","on")

            pi = PendingIntent.getBroadcast(this@MainActivity, 0, myIntent, PendingIntent.FLAG_UPDATE_CURRENT)
            am.setExact(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pi)
        }

        btnEnd.setOnClickListener{
            alarmTxt.setText(R.string.txtAlarm)
            pi = PendingIntent.getBroadcast(this@MainActivity, 0, myIntent, PendingIntent.FLAG_UPDATE_CURRENT)
            am.cancel(pi)
            myIntent.putExtra("extra","off")
            sendBroadcast(myIntent)
        }
    }
}