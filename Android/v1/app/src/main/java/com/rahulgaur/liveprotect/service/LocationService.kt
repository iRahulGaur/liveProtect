package com.rahulgaur.liveprotect.service

import android.annotation.SuppressLint
import android.app.*
import android.content.Context
import android.content.Intent
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.rahulgaur.liveprotect.MainActivity
import com.rahulgaur.liveprotect.R

class LocationService : Service() {

    companion object {
        fun startService(context: Context, message: String) {
            val startIntent = Intent(context, LocationService::class.java)
            startIntent.putExtra("inputExtra", message)
            ContextCompat.startForegroundService(context, startIntent)
        }

        fun stopService(context: Context) {
            val stopIntent = Intent(context, LocationService::class.java)
            context.stopService(stopIntent)
        }

        private const val CHANNEL_ID = "Current Location"
        private const val TAG = "LocationService"
        private const val LOCATION_GAP = 10000
    }

    private lateinit var locationManager: LocationManager
    private lateinit var firebaseFirestore: FirebaseFirestore
    private lateinit var handler: Handler

    @SuppressLint("MissingPermission")
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        firebaseFirestore = FirebaseFirestore.getInstance()
        handler = Handler()

        //do heavy ork on another thread
        Log.e(TAG, "inside onStartCommand: ")
        handler.post { getLocation() }

        val input = intent?.getStringExtra("inputExtra")
        createNotificationChannel()
        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            notificationIntent,
            0
        )

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Getting Location")
            .setContentText(input)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(pendingIntent)
            .build()

        startForeground(1, notification)

        return START_STICKY
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                CHANNEL_ID,
                "Location Channel",
                NotificationManager.IMPORTANCE_DEFAULT
            )

            val manager = getSystemService(NotificationManager::class.java)
            manager!!.createNotificationChannel(serviceChannel)
        }
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    @SuppressLint("MissingPermission")
    private fun getLocation() {
        val providers: List<String> = locationManager.getProviders(true)
        var location: Location? = null
        for (i in providers.size - 1 downTo 0) {
            location = locationManager.getLastKnownLocation(providers[i])
            if (location != null)
                break
        }
        if (location != null) {
            uploadLocation(location)
        } else {
            Log.e(TAG, "location is null")
        }
    }

    private fun uploadLocation(location: Location?) {

        val locationMap: HashMap<String, String> = HashMap()

        val latitude: String = location!!.latitude.toString()
        val longitude: String = location.longitude.toString()
        val accuracy: String = location.accuracy.toString()

        locationMap["latitude"] = latitude
        locationMap["longitude"] = longitude
        locationMap["accuracy"] = accuracy
        locationMap["time"] = FieldValue.serverTimestamp().toString()

        Log.e(TAG, "this is locationMap : $locationMap")

        firebaseFirestore.collection("Locations").document("user1")
            .set(locationMap)
            .addOnSuccessListener(Activity()) {
                Log.e(TAG, "location uploaded to firebase")
                handler.postDelayed({
                    getLocation()
                }, LOCATION_GAP.toLong())
            }.addOnFailureListener { exception ->
                Log.e(
                    TAG,
                    "error in uploading location to firebase ${exception.localizedMessage}"
                )
                Toast.makeText(
                    applicationContext,
                    "Some error uploading data ${exception.localizedMessage}",
                    Toast.LENGTH_SHORT
                ).show()
                handler.postDelayed({
                    getLocation()
                }, LOCATION_GAP.toLong())
            }
    }
}
