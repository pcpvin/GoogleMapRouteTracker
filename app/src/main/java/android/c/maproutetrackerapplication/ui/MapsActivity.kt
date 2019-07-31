package android.c.maproutetrackerapplication.ui

import android.Manifest
import android.annotation.SuppressLint
import android.c.maproutetrackerapplication.controller.MapsController
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import kotlinx.android.synthetic.main.activity_maps.*
import android.c.maproutetrackerapplication.api.RetrofitClient
import android.c.maproutetrackerapplication.model.google.directions.Directions
import android.c.maproutetrackerapplication.model.others.Route
import android.c.maproutetrackerapplication.utility.Constants
import com.samplediections.BuildConfig
import com.samplediections.R
import org.jetbrains.anko.toast
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class MapsActivity : AppCompatActivity(), OnMapReadyCallback {
    private lateinit var mGoogleMap: GoogleMap
    private lateinit var mMapsController: MapsController
    /**
     * Provides the entry point to the Fused Location Provider API.
     */
    private var mFusedLocationClient: FusedLocationProviderClient? = null

    /**
     * Represents a geographical location.
     */
    protected var mLastLocation: Location? = null
    private var mLatitudeLabel: String? = null
    private var mLongitudeLabel: String? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mGoogleMap = googleMap
        mMapsController = MapsController(this, mGoogleMap)
        mMapsController.setCustomMarker()


        usersStepsBtn.setOnClickListener {
            startActivity(Intent(applicationContext, UsersStepsActivity::class.java))
        }


        directionsToggleButton.setOnCheckedChangeListener { buttonView, isChecked ->
            progressLayout.visibility = View.VISIBLE

            if (isChecked) {
                val directionsCall = RetrofitClient.googleMethods().getDirections(
                    getString(R.string.source),
                    getString(R.string.destination),
                    Constants.GOOGLE_API_KEY
                )
                directionsCall.enqueue(object : Callback<Directions> {
                    override fun onResponse(call: Call<Directions>, response: Response<Directions>) {
                        val directions = response.body()!!

                        if (directions.status.equals("OK")) {
                            val legs = directions.routes[0].legs[0]
                            val route = Route(
                                getString(R.string.source),
                                getString(R.string.destination),
                                legs.startLocation.lat,
                                legs.startLocation.lng,
                                legs.endLocation.lat,
                                legs.endLocation.lng,
                                directions.routes[0].overviewPolyline.points
                            )

                            mMapsController.setMarkersAndRoute(route)
                        } else {
                            toast(directions.status)
                            directionsToggleButton.isChecked = false
                        }

                        progressLayout.visibility = View.GONE
                    }

                    override fun onFailure(call: Call<Directions>, t: Throwable) {
                        toast(t.toString())
                        progressLayout.visibility = View.GONE
                        directionsToggleButton.isChecked = false
                    }
                })
            } else {
                progressLayout.visibility = View.GONE
                mMapsController.clearMarkersAndRoute()
            }
        }
    }


    public override fun onStart() {
        super.onStart()
        if (!checkPermissions()) {
            requestPermissions()
        } else {
            getLastLocation()
        }
    }

    /**
     * Provides a simple way of getting a device's location and is well suited for
     * applications that do not require a fine-grained location and that do not need location
     * updates. Gets the best and most recent location currently available, which may be null
     * in rare cases when a location is not available.
     *
     *
     * Note: this method should be called after location permission has been granted.
     */
    @SuppressLint("MissingPermission")
    private fun getLastLocation() {
        mFusedLocationClient!!.lastLocation
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful && task.result != null) {
                    mLastLocation = task.result
                    mLongitudeLabel = (mLastLocation)!!.longitude.toString()
                    mLatitudeLabel = (mLastLocation)!!.latitude.toString()

                } else {
                    Log.w(TAG, "getLastLocation:exception", task.exception)
                    //showMessage(getString(R.string.no_location_detected))
                }
            }
    }

    /**
     * Shows a [] using `text`.

     * @param text The Snackbar text.
     */
//    private fun showMessage(text: String) {
//        val container = findViewById<View>(R.id.main_activity_container)
//        if (container != null) {
//            Toast.makeText(this@MapsActivity, text, Toast.LENGTH_LONG).show()
//        }
//    }

    /**
     * Shows a [].

     * @param mainTextStringId The id for the string resource for the Snackbar text.
     * *
     * @param actionStringId   The text of the action item.
     * *
     * @param listener         The listener associated with the Snackbar action.
     */
    private fun showSnackbar(
        mainTextStringId: Int, actionStringId: Int,
        listener: View.OnClickListener
    ) {

        Toast.makeText(this@MapsActivity, getString(mainTextStringId), Toast.LENGTH_LONG).show()
    }

    /**
     * Return the current state of the permissions needed.
     */
    private fun checkPermissions(): Boolean {
        val permissionState = ActivityCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
        return permissionState == PackageManager.PERMISSION_GRANTED
    }

    private fun startLocationPermissionRequest() {
        ActivityCompat.requestPermissions(
            this@MapsActivity,
            arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION),
            REQUEST_PERMISSIONS_REQUEST_CODE
        )
    }

    private fun requestPermissions() {
        val shouldProvideRationale = ActivityCompat.shouldShowRequestPermissionRationale(
            this,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )

        // Provide an additional rationale to the user. This would happen if the user denied the
        // request previously, but didn't check the "Don't ask again" checkbox.
        if (shouldProvideRationale) {
            Log.i(TAG, "Displaying permission rationale to provide additional context.")

            showSnackbar(R.string.permission_rationale, android.R.string.ok,
                View.OnClickListener {
                    // Request permission
                    startLocationPermissionRequest()
                })

        } else {
            Log.i(TAG, "Requesting permission")
            // Request permission. It's possible this can be auto answered if device policy
            // sets the permission in a given state or the user denied the permission
            // previously and checked "Never ask again".
            startLocationPermissionRequest()
        }
    }

    /**
     * Callback received when a permissions request has been completed.
     */
    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>,
        grantResults: IntArray
    ) {
        Log.i(TAG, "onRequestPermissionResult")
        if (requestCode == REQUEST_PERMISSIONS_REQUEST_CODE) {
            if (grantResults.size <= 0) {
                // If user interaction was interrupted, the permission request is cancelled and you
                // receive empty arrays.
                Log.i(TAG, "User interaction was cancelled.")
            } else if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted.
                getLastLocation()
            } else {
                // Permission denied.

                // Notify the user via a SnackBar that they have rejected a core permission for the
                // app, which makes the Activity useless. In a real app, core permissions would
                // typically be best requested during a welcome-screen flow.

                // Additionally, it is important to remember that a permission might have been
                // rejected without asking the user for permission (device policy or "Never ask
                // again" prompts). Therefore, a user interface affordance is typically implemented
                // when permissions are denied. Otherwise, your app could appear unresponsive to
                // touches or interactions which have required permissions.
                showSnackbar(R.string.permission_denied_explanation, R.string.settings,
                    View.OnClickListener {
                        // Build intent that displays the App settings screen.
                        val intent = Intent()
                        intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                        val uri = Uri.fromParts(
                            "package",
                            BuildConfig.APPLICATION_ID, null
                        )
                        intent.data = uri
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        startActivity(intent)
                    })
            }
        }
    }

    companion object {
        private val TAG = "LocationProvider"
        private val REQUEST_PERMISSIONS_REQUEST_CODE = 34
    }
}
