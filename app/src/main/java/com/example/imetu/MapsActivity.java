package com.example.imetu;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private FusedLocationProviderClient client;
    private static final String TAG = "MapsActivity";
    private double lat;
    private double lng;
    private double clat;
    private double clng;
    private String address;
    private String userid;
    private double proximity =0;

    private static DecimalFormat df = new DecimalFormat("0.00");

    static Boolean flag_stop_sharing_location = true;



    // Create a new user with a first and last name
    Map<String, Object> user = new HashMap<>();

    // To save neighbours lat long
    Map<String, double[]> get_latlng = new HashMap<>();

    int flagLocation=0;
    EditText editText1;
    EditText editText_status;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        editText1 = (EditText) findViewById(R.id.location);

        editText_status = (EditText) findViewById(R.id.status);



        // FIREBASE CONNECTION AND Start sharing Location Data

        final FirebaseFirestore db = FirebaseFirestore.getInstance();


        // Add a new document with a generated ID
        Button button_submit = (Button) findViewById(R.id.start_share_location);
        button_submit.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("MissingPermission")
            @Override
            public void onClick(View v) {

                flag_stop_sharing_location = false;

                if (!flag_stop_sharing_location) {

                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy-hh-mm-ss");
                    String format = simpleDateFormat.format(new Date());

                user.put("address", address);
                user.put("lat", clat);
                user.put("long", clng);
                user.put("timestamp", format);
                user.put("userid", "default");

                db.collection("users")
                        .add(user)
                        .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                            @Override
                            public void onSuccess(DocumentReference documentReference) {
                                Log.d(TAG, "DocumentSnapshot added with ID: " + documentReference.getId());
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.w(TAG, "Error adding document", e);
                            }
                        });
                    } // if flag
                }
            });

        // Stop sharing location data

        Button button_stop = (Button) findViewById(R.id.stop_share_location);
        button_stop.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("MissingPermission")
            @Override
            public void onClick(View v) {

                flag_stop_sharing_location = true;
            }
        });


        // GET LOCATION
        client = LocationServices.getFusedLocationProviderClient(this);

        Button button = (Button) findViewById(R.id.button_get_address);
        button.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("MissingPermission")
            @Override
            public void onClick(View v) {
                client.getLastLocation().addOnSuccessListener(MapsActivity.this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                        if (location != null) {
                            address = getCityName(latLng);
                            clat = latLng.latitude;
                            clng = latLng.longitude;
                            editText1.setText(address);


                        }

                    }
                });
            }
        });


        // GET and SHOW NEIGHBOURS
        Button button_get_neighbours = (Button) findViewById(R.id.button_get_neighbours);
        button_get_neighbours.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("MissingPermission")
            @Override
            public void onClick(View v) {

                // GET NEIGHBOURS DATA
                db.collection("users")
                        .get()
                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                proximity = 0;
                                if (task.isSuccessful()) {
                                    for (QueryDocumentSnapshot document : task.getResult()) {
//                                        System.out.println(document.get("lat"));
                                        Log.d(TAG, document.getId() + " => " + document.getData());
                                        System.out.println(clat);
                                        System.out.println(clng);
                                        // putting values in neighbours hashmap
                                        get_latlng.put(document.getId(), new double[]{(double) document.get("lat"), (double) document.get("long")} );

                                        // Calculating distance
                                        proximity = proximity + distance(clat,clng,(double)document.get("lat"),(double)document.get("long"));

                                        System.out.println(proximity);
                                    }
                                    double avg_proxinity = proximity / get_latlng.size();
                                    editText_status.setText(String.format("Total %s neighbours with average proximity of %s kilometers", Integer.toString(get_latlng.size()),
                                            df.format(avg_proxinity)));

                                    if (avg_proxinity > 1 | get_latlng.size() > 10 ){
                                        AlertDialog alertDialog = new AlertDialog.Builder(MapsActivity.this).create();
                                        alertDialog.setTitle("Alert");
                                        alertDialog.setMessage(getString(R.string.AlertMessage));
                                        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                                                new DialogInterface.OnClickListener() {
                                                    public void onClick(DialogInterface dialog, int which) {
                                                        dialog.dismiss();
                                                    }
                                                });
                                        alertDialog.show();
                                    }


                                } else {
                                    Log.w(TAG, "Error getting documents.", task.getException());
                                }
                            }
                        });

                // SHOW NEIGHBOURS DATA

                SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                        .findFragmentById(R.id.map);
                assert mapFragment != null;
                mapFragment.getMapAsync(MapsActivity.this);


            }
        });


    }


    // Map display
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        mMap.setMyLocationEnabled(true);
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setScrollGesturesEnabled(true);
        mMap.isTrafficEnabled();
        Log.d("mylog", "Complete Address: " + clat);


        // creating markers for neighbours
        for (Map.Entry<String, double[]> entry : get_latlng.entrySet()) {

            LatLng marker_temp = new LatLng(entry.getValue()[0],entry.getValue()[1]);
            mMap.addMarker(new MarkerOptions().position(marker_temp).title("Marker in Sydney"));
            mMap.moveCamera(CameraUpdateFactory.newLatLng(marker_temp));
        }

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {


            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
    }


    private String getCityName(LatLng myCoordinates) {
        String myCity = "";
        Geocoder geocoder = new Geocoder(MapsActivity.this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(myCoordinates.latitude, myCoordinates.longitude, 1);
            String address = addresses.get(0).getAddressLine(0);
            // myCity = addresses.get(0).getAdminArea();  //california
            // myCity = addresses.get(0).getLocality();   //Mountain View
            // myCity = addresses.get(0).getSubAdminArea(); //Santa Clara County
            //myCity = addresses.get(0).getFeatureName(); //1600
            //myCity = addresses.get(0).getThoroughfare(); //Amphitheatre Parkway
            myCity = addresses.get(0).getAddressLine(0);
            Log.d("mylog", "Complete Address: " + addresses.toString());
            Log.d("mylog", "Address: " + address);
        } catch (IOException e) {
            e.printStackTrace();
        }
        flagLocation=1;
        return myCity;
    }

    private double distance(double lat1, double lon1, double lat2, double lon2) {
        double theta = lon1 - lon2;
        double dist = Math.sin(deg2rad(lat1))
                * Math.sin(deg2rad(lat2))
                + Math.cos(deg2rad(lat1))
                * Math.cos(deg2rad(lat2))
                * Math.cos(deg2rad(theta));
        dist = Math.acos(dist);
        dist = rad2deg(dist);
        dist = dist * 60 * 1.1515;
        return (dist);
    }

    private double deg2rad(double deg) {
        return (deg * Math.PI / 180.0);
    }

    private double rad2deg(double rad) {
        return (rad * 180.0 / Math.PI);
    }


}


