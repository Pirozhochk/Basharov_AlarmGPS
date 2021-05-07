package ru.basharov.alarmgps

import android.Manifest
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.widget.Button
import android.widget.TextView
import android.widget.TimePicker
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.*
import kotlinx.android.synthetic.main.activity_main.*
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

    private var PERMISSION_ID = 52
    lateinit var fusedLPC: FusedLocationProviderClient
    lateinit var locationRequest: LocationRequest
    lateinit var btngps: Button

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
        btngps = findViewById(R.id.btnGPS)

        fusedLPC = LocationServices.getFusedLocationProviderClient(this)

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

        btngps.setOnClickListener{
            getLastLocation()
            getNewLocation()
        }
    }

    private fun checkPermission(): Boolean{
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            return true
        }
        return false
    }

    private fun RequestPermission(){
        ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.ACCESS_COARSE_LOCATION,android.Manifest.permission.ACCESS_FINE_LOCATION), PERMISSION_ID)
    }

    private fun isLocationEnabled(): Boolean{
        var locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    private fun getLastLocation(){
        if(checkPermission()){
            if(isLocationEnabled()){
                fusedLPC.lastLocation.addOnCompleteListener{task ->
                    var location = task.result
                    if(location == null){
                        getNewLocation()
                    }else{
                        txtGPS.text = "Ваши координаты:\nШирота:" + location.latitude + " ; Долгота:" + location.longitude + "\nГород: " +
                                getCityName(location.latitude, location.longitude) + ", Страна: " + getCountryName(location.latitude, location.longitude)
                    }
                }
            }else{
                Toast.makeText(this,"Сервисы, определяющие местоположение отключены.Пожалуйста, включите их", Toast.LENGTH_SHORT).show()
            }
        }else{
            RequestPermission()
        }
    }

    private fun getNewLocation(){
        locationRequest = LocationRequest()
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        locationRequest.interval = 0
        locationRequest.fastestInterval = 0
        locationRequest.numUpdates = 2
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        fusedLPC!!.requestLocationUpdates(
            locationRequest,locationCallBack,Looper.myLooper()
        )
    }

    private val locationCallBack = object: LocationCallback(){
        override fun onLocationResult(p0: LocationResult) {
            var lastLocation = p0.lastLocation
            txtGPS.text = "Ваши координаты:\nШирота:" + lastLocation.latitude + " ; Долгота:" + lastLocation.longitude + "\nГород: " +
                    getCityName(lastLocation.latitude, lastLocation.longitude) + ", Страна: " + getCountryName(lastLocation.latitude, lastLocation.longitude)
        }
    }

    private fun getCityName(lat: Double, long: Double): String{
        var cityName = ""
        var geocoder = Geocoder(this, Locale.getDefault())
        var address = geocoder.getFromLocation(lat,long,1)
        cityName = address.get(0).locality

        return cityName
    }

    private fun getCountryName(lat: Double, long: Double): String{
        var countryName = ""
        var geocoder = Geocoder(this, Locale.getDefault())
        var address = geocoder.getFromLocation(lat,long,1)
        countryName = address.get(0).countryName

        return countryName
    }
}