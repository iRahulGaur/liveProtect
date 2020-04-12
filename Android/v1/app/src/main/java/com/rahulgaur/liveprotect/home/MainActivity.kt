package com.rahulgaur.liveprotect.home

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.Snackbar
import com.rahulgaur.liveprotect.R
import com.rahulgaur.liveprotect.service.LocationService
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    companion object {
        private const val REQUEST_LOCATION_PERMISSION = 1
        private const val TAG = "MainActivity"
    }

    private val locationPermission = Manifest.permission.ACCESS_FINE_LOCATION

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (checkLocationPermission()) {
            //we have location permission and now we can start location service
            callStartService()
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestLocationPermission()
            } else {
                Log.e(TAG, "no need to get location permission: ")
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun requestLocationPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, locationPermission)) {
            AlertDialog.Builder(this)
                .setMessage("Need location permission to get current location of the device")
                .setPositiveButton(android.R.string.ok) { _, _ ->
                    requestPermissions(
                        arrayOf(locationPermission),
                        REQUEST_LOCATION_PERMISSION
                    )
                }
                .setNegativeButton(android.R.string.cancel, null)
                .show()
        } else {
            requestPermissions(
                arrayOf(locationPermission),
                REQUEST_LOCATION_PERMISSION
            )
        }
    }

    private fun checkLocationPermission(): Boolean {
        return (ContextCompat.checkSelfPermission(
            this,
            locationPermission
        ) == PackageManager.PERMISSION_GRANTED)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //we have location permission now we can call location service
                callStartService()
            } else {

                val isNeverAskAgain = !ActivityCompat.shouldShowRequestPermissionRationale(
                    this,
                    locationPermission
                )

                if (isNeverAskAgain) {
                    Snackbar.make(
                        mainConstrainLayout,
                        "Location Permission required",
                        Snackbar.LENGTH_LONG
                    )
                        .setAction("Settings") {
                            Intent(
                                ACTION_APPLICATION_DETAILS_SETTINGS,
                                Uri.parse("package:${this.packageName}")
                            ).apply {
                                addCategory(Intent.CATEGORY_DEFAULT)
                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                startActivity(this)
                            }
                        }
                        .show()
                } else {
                    Log.e(TAG, "permission denied: ")
                }
            }
        }
    }

    private fun callStartService() {
        LocationService.startService(this, "Getting your location")
    }

    private fun callStopService() {
        LocationService.stopService(this)
    }
}
