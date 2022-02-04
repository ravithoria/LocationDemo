package com.example.locationupdatedemo;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.content.IntentSender;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.text.DateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();
    private final Activity mActivity = MainActivity.this;

    private static final int REQUEST_CHECK_SETTINGS = 100;

    private static final long UPDATE_INTERVAL_IN_MILLISECONDS = 10000;//10 sec.

    public static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS =
            UPDATE_INTERVAL_IN_MILLISECONDS / 2;

    private FusedLocationProviderClient mFusedLocationClient;
    private SettingsClient mSettingsClient;
    private LocationRequest mLocationRequest;
    private LocationSettingsRequest mLocationSettingsRequest;
    private LocationCallback mLocationCallback;
    private Location mCurrentLocation;

    private Button btnReqLocation, btn_stop_location;
    private TextView tv_lat_lng, tv_time;

    private Boolean mRequestingLocationUpdates;

    //
    private String mLastUpdateTimeLabel;
    private String mLastUpdateTime;

    double latitude, longitude, lastLat, lastLng;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tv_lat_lng = findViewById(R.id.tv_lat_lng);
        tv_time = findViewById(R.id.tv_time);
        btnReqLocation = findViewById(R.id.btn_req_location);
        btn_stop_location = findViewById(R.id.btn_stop_location);
        mLastUpdateTimeLabel = "Last location update time";

        mRequestingLocationUpdates = false;
        mLastUpdateTime = "";

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(mActivity);
        mSettingsClient = LocationServices.getSettingsClient(mActivity);

        createLocationCallback();
        createLocationRequest();
        buildLocationSettingsRequest();

        btnReqLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!mRequestingLocationUpdates) {
                    mRequestingLocationUpdates = true;
                    //createLocationCallback();
                    //createLocationRequest();
                    //buildLocationSettingsRequest();
                    mFusedLocationClient = LocationServices.getFusedLocationProviderClient(mActivity);

                    startLocationUpdates();
                }

            }
        });

        btn_stop_location.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                stopLocationUpdates();
            }
        });
    }

    private void createLocationCallback() {
        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                Log.d(TAG, "createLocationCallback");
                super.onLocationResult(locationResult);

                //onLocationChanged(locationResult.getLastLocation());
                //mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());

                /*Location location = locationResult.getLastLocation();
                latitude = location.getLatitude();
                longitude = location.getLongitude();
                Log.d(TAG, "LAT:: " + latitude);
                Log.d(TAG, "Lng:: " + longitude);

                mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());
                updateLocationUI(location);*/

                for (Location location : locationResult.getLocations()) {
                    latitude = location.getLatitude();
                    longitude = location.getLongitude();
                    Log.d(TAG, "LAT:: " + latitude);
                    Log.d(TAG, "Lng:: " + longitude);

                    mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());
                    updateLocationUI(location);
                }

                //stopLocationUpdates();


                /*List<Location> locations = locationResult.getLocations();
                for (int i=0; i<locations.size(); i++) {
                    double latitude = locations.get(i).getLatitude();
                    double longitude = locations.get(i).getLongitude();
                    Log.d(TAG,"LAT:: "+latitude);
                    Log.d(TAG,"Lng:: "+longitude);
                }*/


                //mCurrentLocation = locationResult.getLastLocation();
                //mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());
            }
        };
    }

    private void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);
        mLocationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    private void buildLocationSettingsRequest() {
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(mLocationRequest);
        mLocationSettingsRequest = builder.build();
    }

    private void startLocationUpdates() {
        // Begin by checking if the device has the necessary location settings.
        mSettingsClient.checkLocationSettings(mLocationSettingsRequest)
                .addOnSuccessListener(this, new OnSuccessListener<LocationSettingsResponse>() {
                    @Override
                    public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                        Log.i(TAG, "All location settings are satisfied.");

                        //noinspection MissingPermission
                        mFusedLocationClient.requestLocationUpdates(mLocationRequest,
                                mLocationCallback, Looper.myLooper());

                        //updateLocationUI();
                    }
                })
                .addOnFailureListener(this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        int statusCode = ((ApiException) e).getStatusCode();
                        switch (statusCode) {
                            case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                                Log.i(TAG, "Location settings are not satisfied. Attempting to upgrade " +
                                        "location settings ");
                                try {
                                    // Show the dialog by calling startResolutionForResult(), and check the
                                    // result in onActivityResult().
                                    ResolvableApiException rae = (ResolvableApiException) e;
                                    rae.startResolutionForResult(MainActivity.this, REQUEST_CHECK_SETTINGS);
                                } catch (IntentSender.SendIntentException sie) {
                                    Log.i(TAG, "PendingIntent unable to execute request.");
                                }
                                break;
                            case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                                String errorMessage = "Location settings are inadequate, and cannot be " +
                                        "fixed here. Fix in Settings.";
                                Log.e(TAG, errorMessage);
                                Toast.makeText(MainActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                                mRequestingLocationUpdates = false;
                        }

                        //updateUI();
                    }
                });
    }

    private void updateLocationUI(Location location) {
        //if (mCurrentLocation != null) {
        // New location has now been determined
        String msg = "Updated Location: " +
                Double.toString(location.getLatitude()) + "," +
                Double.toString(location.getLongitude());
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
        // You can now create a LatLng Object for use with maps
        //LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());

        tv_lat_lng.setText("LatLng:: "+location.getLatitude()+
                location.getLongitude());

        tv_time.setText(String.format(Locale.ENGLISH, "%s: %s",
                mLastUpdateTimeLabel, mLastUpdateTime));


            /*tv_lat_lng.setText("LatLng:: "+mCurrentLocation.getLatitude()+
                    mCurrentLocation.getLongitude());*/



            /*mLongitudeTextView.setText(String.format(Locale.ENGLISH, "%s: %f", mLongitudeLabel,
                    mCurrentLocation.getLongitude()));*/
            /*mLastUpdateTimeTextView.setText(String.format(Locale.ENGLISH, "%s: %s",
                    mLastUpdateTimeLabel, mLastUpdateTime));*/

        //stopLocationUpdates();
        // }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // Check for the integer request code originally supplied to startResolutionForResult().
        if (requestCode == REQUEST_CHECK_SETTINGS) {
            switch (resultCode) {
                case Activity.RESULT_OK:
                    Log.i(TAG, "User agreed to make required location settings changes.");
                    // Nothing to do. startLocationupdates() gets called in onResume again.
                    break;
                case Activity.RESULT_CANCELED:
                    Log.i(TAG, "User chose not to make required location settings changes.");
                    mRequestingLocationUpdates = false;
                    //updateUI();
                    break;
            }
        }
    }

    private void stopLocationUpdates() {
        if (!mRequestingLocationUpdates) {
            Log.d(TAG, "stopLocationUpdates: updates never requested, no-op.");
            return;
        }

        // It is a good practice to remove location requests when the activity is in a paused or
        // stopped state. Doing so helps battery performance and is especially
        // recommended in applications that request frequent location updates.
        mFusedLocationClient.removeLocationUpdates(mLocationCallback)
                .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Log.d(TAG, "removeLocationUpdates");
                        mRequestingLocationUpdates = false;
                        //setButtonsEnabledState();
                    }
                });
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");
        if (mRequestingLocationUpdates /*&& checkPermissions()*/) {
            startLocationUpdates();
        } /*else if (!checkPermissions()) {
            requestPermissions();
        }*/
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause");
        //stopLocationUpdates();
    }

    public void onLocationChanged(Location location) {
        // New location has now been determined
        String msg = "Updated Location: " +
                Double.toString(location.getLatitude()) + "," +
                Double.toString(location.getLongitude());
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
        // You can now create a LatLng Object for use with maps
        //LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());

        tv_lat_lng.setText("LatLng:: "+location.getLatitude()+
                location.getLongitude());

        tv_time.setText(String.format(Locale.ENGLISH, "%s: %s",
                mLastUpdateTimeLabel, mLastUpdateTime));

        //stopLocationUpdates();
    }
}