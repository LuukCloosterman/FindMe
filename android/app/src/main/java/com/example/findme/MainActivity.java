package com.example.findme;
import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.GeomagneticField;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;


import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;

import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.findme.character.CharacterProvider;
import com.example.findme.mission.MissionProvider;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;


import org.apache.commons.math3.util.Precision;
import org.json.JSONException;
import org.json.JSONObject;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static com.android.volley.Request.Method.POST;

public class MainActivity extends AppCompatActivity
        implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener, SensorEventListener{

    private Location location;
    private TextView locationTv;
    private GoogleApiClient googleApiClient;
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    private LocationRequest locationRequest;
    private static final long UPDATE_INTERVAL = 5000, FASTEST_INTERVAL = 5000; // = 5 seconds
    // lists for permissions
    private ArrayList<String> permissionsToRequest;
    private ArrayList<String> permissionsRejected = new ArrayList<>();
    private ArrayList<String> permissions = new ArrayList<>();
    // integer for permissions results request
    private static final int ALL_PERMISSIONS_RESULT = 1011;
    private int userNumber;
    private double longi;
    private double lati;
    private RequestQueue queue;
    private Location toGoTo;
    private Location myLocation;
    SensorManager sensorManager;
    private ConstraintLayout turningThing;
    private TextView searchingTV;
    float heading =0f;
    private ImageView imgv;
    private String status;
    private String character;
    GeomagneticField geoField;
    float myBearing;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE); //will hide the title
        getSupportActionBar().hide(); // hide the title bar
        setContentView(R.layout.activity_main);
        // we add permissions we need to request location of the users
        permissions.add(Manifest.permission.ACCESS_FINE_LOCATION);
        permissions.add(Manifest.permission.ACCESS_COARSE_LOCATION);
        permissionsToRequest = permissionsToRequest(permissions);
        turningThing = findViewById(R.id.turningthing);
        searchingTV = findViewById(R.id.searchingTV);
        userNumber =0;
        CharacterProvider characterProvider = new CharacterProvider();
        status = characterProvider.getStatus();
        final ArrayAdapter<String> adp = new ArrayAdapter<String>(MainActivity.this,
                android.R.layout.simple_spinner_item, characterProvider.getCharacters());
        final Spinner sp = new Spinner(MainActivity.this);
        sp.setAdapter(adp);
        final AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        TextView spinnerDialTV = new TextView(MainActivity.this);
        spinnerDialTV.setText("For this session your character is a: " + status + " ....");
        spinnerDialTV.setTextSize(18);

        LinearLayout layout = new LinearLayout(MainActivity.this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.addView(spinnerDialTV);
        layout.addView(sp);
        builder.setCancelable(false);
        builder.setPositiveButton("Continue...", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                character = sp.getSelectedItem().toString();
                Log.i("test", character);

            }
        });
        builder.setView(layout);
        builder.create().show();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (permissionsToRequest.size() > 0) {
                requestPermissions(permissionsToRequest.toArray(
                        new String[permissionsToRequest.size()]), ALL_PERMISSIONS_RESULT);
            }
        }

        // we build google api client
        googleApiClient = new GoogleApiClient.Builder(this).
                addApi(LocationServices.API).
                addConnectionCallbacks(this).
                addOnConnectionFailedListener(this).build();
        googleApiClient.connect();
        queue = Volley.newRequestQueue(this);
        getUserName();

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        imgv = findViewById(R.id.imgv);
        toGoTo = new Location("");


            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if((toGoTo.getLatitude() ==0 || toGoTo.getLongitude() ==0) && userNumber!=0){
                        getAVGLoc();
                        handler.postDelayed(this, 5000);
                    }
                }
            }, 5000);  //the time



    }


    private ArrayList<String> permissionsToRequest(ArrayList<String> wantedPermissions) {
        ArrayList<String> result = new ArrayList<>();

        for (String perm : wantedPermissions) {
            if (!hasPermission(perm)) {
                result.add(perm);
            }
        }

        return result;
    }

    private boolean hasPermission(String permission) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED;
        }

        return true;
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (googleApiClient != null) {
            googleApiClient.connect();
        }


    }

    @Override
    protected void onResume() {
        super.onResume();

        if (!checkPlayServices()) {
            searchingTV.setText("You need to install Google Play Services to use the App properly");
        }
        sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION),
                SensorManager.SENSOR_DELAY_GAME);
    }

    @Override
    protected void onPause() {
        super.onPause();

        // stop location updates
        if (googleApiClient != null  &&  googleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, this);
            googleApiClient.disconnect();
        }
    }

    private boolean checkPlayServices() {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(this);

        if (resultCode != ConnectionResult.SUCCESS) {
            if (apiAvailability.isUserResolvableError(resultCode)) {
                apiAvailability.getErrorDialog(this, resultCode, PLAY_SERVICES_RESOLUTION_REQUEST);
            } else {
                finish();
            }

            return false;
        }

        return true;
    }

    @Override
    public void onConnected( Bundle bundle) {
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                &&  ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        // Permissions ok, we get last location
        location = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);

        if (location != null) {
            myLocation = location;
        }

        startLocationUpdates();
    }

    private void startLocationUpdates() {
        locationRequest = new LocationRequest();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(UPDATE_INTERVAL);
        locationRequest.setFastestInterval(FASTEST_INTERVAL);

        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                &&  ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "You need to enable permissions to display location !", Toast.LENGTH_SHORT).show();
        }

        LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);

    }

    @Override
    public void onConnectionSuspended(int i) {
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
    }

    @Override
    public void onLocationChanged(Location location) {
        if (location != null) {
            myLocation = location;

            longi = Precision.round(location.getLongitude(), 6);
            lati = Precision.round(location.getLatitude(), 6);
            final JSONObject body = new JSONObject();
            try {
                body.put("id", userNumber);
                body.put("longi", longi);
                body.put("lat", lati);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            String url ="http://79.137.37.198:3000/";
            StringRequest request = new StringRequest(POST, url, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    Log.i("update", response);
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {

                }
            }) {
                @Override
                public byte[] getBody() {
                    return body.toString().getBytes();
                }

                @Override
                public String getBodyContentType() {
                    return "application/json";
                }
                @Override
                public Map<String, String> getHeaders(){
                    Map<String, String> headers = new HashMap<>();
                    headers.put("Content-Type","application/json");
                    return headers;
                }
            };
            if(toGoTo.getLongitude()!=0) {
                int distance = Math.round(myLocation.distanceTo(toGoTo));
                if (distance<50){
                    if(distance<5){

                        AlertDialog.Builder dlgAlert  = new AlertDialog.Builder(this);
                        dlgAlert.setMessage(R.string.openCameraMessage);
                        dlgAlert.setTitle(R.string.openCamera);
                        dlgAlert.setPositiveButton(R.string.open, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                onCameraClick();
                            }
                        });
                        dlgAlert.setCancelable(true);
                        dlgAlert.create().show();
                        locationRequest.setNumUpdates(0);
                    } else {
                        searchingTV.setText(distance + " " + R.string.meter);

                    }

                } else {
//                    searchingTV.setVisibility(View.INVISIBLE);
                }
            }
            queue.add(request);
            geoField = new GeomagneticField(
                    Double.valueOf(location.getLatitude()).floatValue(),
                    Double.valueOf(location.getLongitude()).floatValue(),
                    Double.valueOf(location.getAltitude()).floatValue(),
                    System.currentTimeMillis());

        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch(requestCode) {
            case ALL_PERMISSIONS_RESULT:
                for (String perm : permissionsToRequest) {
                    if (!hasPermission(perm)) {
                        permissionsRejected.add(perm);
                    }
                }

                if (permissionsRejected.size() > 0) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        if (shouldShowRequestPermissionRationale(permissionsRejected.get(0))) {
                            new AlertDialog.Builder(MainActivity.this).
                                    setMessage("These permissions are mandatory to get your location. You need to allow them.").
                                    setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                                requestPermissions(permissionsRejected.
                                                        toArray(new String[permissionsRejected.size()]), ALL_PERMISSIONS_RESULT);
                                            }
                                        }
                                    }).setNegativeButton("Cancel", null).create().show();

                            return;
                        }
                    }
                } else {
                    if (googleApiClient != null) {
                        googleApiClient.connect();
                    }
                }

                break;
        }
    }
    private void getUserName(){
        final JSONObject body = new JSONObject();
        try {
            body.put("longi", longi);
            body.put("lat", lati);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        String url ="http://79.137.37.198:3000/id";
        StringRequest request = new StringRequest(POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.i("user", response);
                userNumber = Integer.parseInt(response);
                Log.i("userNumber", String.valueOf(userNumber));
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        }) {
            @Override
            public byte[] getBody() {
                return body.toString().getBytes();
            }

            @Override
            public String getBodyContentType() {
                return "application/json";
            }
            @Override
            public Map<String, String> getHeaders(){
                Map<String, String> headers = new HashMap<>();
                headers.put("Content-Type","application/json");
                return headers;
            }
        };
        Log.i("test", request.toString());
        queue.add(request);
        queue.start();

    }

    @Override
    protected void onStop() {
        String url ="http://79.137.37.198:3000/delete";
        final JSONObject body = new JSONObject();
        try {
            body.put("id", userNumber);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        StringRequest request = new StringRequest(POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        }) {
            @Override
            public byte[] getBody() {
                return body.toString().getBytes();
            }

            @Override
            public String getBodyContentType() {
                return "application/json";
            }
            @Override
            public Map<String, String> getHeaders(){
                Map<String, String> headers = new HashMap<>();
                headers.put("Content-Type","application/json");
                return headers;
            }
        };
        queue.add(request);
        queue.start();
        super.onStop();

    }
    void getAVGLoc(){
        String url = "http://79.137.37.198:3000/getgoto";
        final JSONObject body = new JSONObject();
        try {
            body.put("id", userNumber);
            body.put("longi", longi);
            body.put("lat", lati);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        JsonObjectRequest request = new JsonObjectRequest(POST, url, body, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Log.i("avgloc", response.toString());
                if (!response.toString().contains("no point found yet")){
                    try {
                        Log.i("test", "point found");
                        toGoTo.setLatitude(Double.parseDouble(response.get("latitude").toString()));
                        toGoTo.setLongitude(Double.parseDouble(response.get("longitude").toString()));
                        searchingTV.setText("");
                        MissionProvider mp = new MissionProvider(searchingTV);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
            }
        });
        queue.add(request);
        queue.start();
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (toGoTo.getLongitude()!=0) {
            float degree = Math.round(event.values[0]);
            heading += geoField.getDeclination();
            myBearing = location.bearingTo(toGoTo);
            heading = myBearing - (myBearing + heading);
            heading = Math.round(-heading / 360 + 180);
            // create a rotation animation (reverse turn degree heading)
            Log.i("test" , String.valueOf(heading));
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
    public void onVibrateClick(View view){
        if (toGoTo.getLongitude()!=0) {
            Vibrator vb = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
            vb.vibrate(500);
        }
    }
    public void onCameraClick(){
        Log.i("", "This should be in the camera now....");
        Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
        startActivity(intent);

    }


}
