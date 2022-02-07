package br.alu.ufc.robertcabral.consultorio.activitys;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.annotation.NonNull;
import android.support.design.bottomappbar.BottomAppBar;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.common.collect.Maps;

import br.alu.ufc.robertcabral.consultorio.R;
import br.alu.ufc.robertcabral.consultorio.directionhelpers.FetchURL;
import br.alu.ufc.robertcabral.consultorio.directionhelpers.TaskLoadedCallback;
import br.alu.ufc.robertcabral.consultorio.entity.App;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback, TaskLoadedCallback, View.OnClickListener {

    private GoogleMap mMap;
    private MarkerOptions place1, place2;
    Button getDirection;
    private Polyline currentPolyline;
    BottomAppBar bottomAppBar;
    private static final int MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private FusedLocationProviderClient mFusedLocationClient;
    Double latittude = 0.0, longitude = 0.0;
    FloatingActionButton floatingActionButton;
    LocationManager mLocationManager;
    float distance_initial = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        bottomAppBar = findViewById(R.id.bottomAppBarMap);

        bottomAppBar.replaceMenu(R.menu.main_menu_maps);
        bottomAppBar.setOnMenuItemClickListener(this::onOptionsItemSelected);

        floatingActionButton = findViewById(R.id.btRefreshMaps);

        floatingActionButton.setOnClickListener(this);
        //27.658143,85.3199503
        //27.667491,85.3208583
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        if (ContextCompat.checkSelfPermission(MapsActivity.this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(MapsActivity.this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {

                new AlertDialog.Builder(this)
                        .setTitle("Required Location Permission")
                        .setMessage("You have to give this permission to acess this feature")
                        .setPositiveButton("OK", (dialogInterface, i) -> ActivityCompat.requestPermissions(MapsActivity.this,
                                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION))
                        .setNegativeButton("Cancel", (dialogInterface, i) -> dialogInterface.dismiss())
                        .create()
                        .show();


            } else {
                ActivityCompat.requestPermissions(MapsActivity.this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);

            }
        } else {
            Task<Location> task = mFusedLocationClient.getLastLocation();
            task.addOnCompleteListener(task1 -> {
                if(task1.isSuccessful()){
                    Location location = task1.getResult();
                    if (location != null) {
                        latittude = location.getLatitude();
                        longitude = location.getLongitude();

                        place1 = new MarkerOptions().position(new LatLng(latittude, longitude)).title("Localização atual").icon(BitmapDescriptorFactory.fromBitmap(resizeMapIcons("icon_location_user", 80, 104)));
                        place2 = new MarkerOptions().position(new LatLng(-4.965378, -39.006108)).title("Consultorio").icon(BitmapDescriptorFactory.fromBitmap(resizeMapIcons("icon_location", 80, 104)));
                        MapFragment mapFragment = (MapFragment) getFragmentManager()
                                .findFragmentById(R.id.mapNearBy);
                        mapFragment.getMapAsync(MapsActivity.this);

                    }
                }
            });

        }

        mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 10, mLocationListener);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu_maps, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.viewRouter:
                new FetchURL(MapsActivity.this).execute(getUrl(place1.getPosition(), place2.getPosition(), "driving"), "driving");
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private final LocationListener mLocationListener = new LocationListener() {
        @Override
        public void onLocationChanged(final Location location) {
            //Toast.makeText(getApplicationContext(), "Lat: " + latittude + "\nLong: " + longitude, Toast.LENGTH_LONG).show();
            if(mMap != null) {
                mMap.clear();

                place1 = new MarkerOptions().position(new LatLng(location.getLatitude(), location.getLongitude())).title("Localização atual").icon(BitmapDescriptorFactory.fromBitmap(resizeMapIcons("icon_location_user", 80, 104)));
                place2 = new MarkerOptions().position(new LatLng(-4.965378, -39.006108)).title("Consultorio").icon(BitmapDescriptorFactory.fromBitmap(resizeMapIcons("icon_location", 80, 104)));

                latittude = location.getLatitude();
                longitude = location.getLongitude();

                mMap.addMarker(place1);
                mMap.addMarker(place2);
//
//                LatLngBounds.Builder builder = new LatLngBounds.Builder();
//                builder.include(place1.getPosition());
//                builder.include(place2.getPosition());
//                LatLngBounds bounds = builder.build();
//
//                int padding = 200;
//                CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);
//
//                mMap.animateCamera(cu);
            }
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }
    };

    public Bitmap resizeMapIcons(String iconName,int width, int height){
        Bitmap imageBitmap = BitmapFactory.decodeResource(getResources(),getResources().getIdentifier(iconName, "drawable", getPackageName()));
        Bitmap resizedBitmap = Bitmap.createScaledBitmap(imageBitmap, width, height, false);
        return resizedBitmap;
    }

    @Override
    protected void onPause() {
        super.onPause();
        App.paused = true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        App.paused = false;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        Log.d("mylog", "Added Markers");
        mMap.addMarker(place1);
        mMap.addMarker(place2);

        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        builder.include(place1.getPosition());
        builder.include(place2.getPosition());
        LatLngBounds bounds = builder.build();

        int padding = 200; // offset from edges of the map in pixels
        CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);

        mMap.moveCamera(cu);

        //LatLng hcmus = new LatLng((place1.getPosition().latitude + place2.getPosition().latitude) / 2,(place1.getPosition().longitude + place2.getPosition().longitude) / 2);
        //mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(hcmus, 14));
    }

    private String getUrl(LatLng origin, LatLng dest, String directionMode) {
        // Origin of route
        String str_origin = "origin=" + origin.latitude + "," + origin.longitude;
        // Destination of route
        String str_dest = "destination=" + dest.latitude + "," + dest.longitude;
        // Mode
        String mode = "mode=" + directionMode;
        // Building the parameters to the web service
        String parameters = str_origin + "&" + str_dest + "&" + mode;
        // Output format
        String output = "json";
        // Building the url to the web service
        String url = "https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters + "&key=" + "AIzaSyCsetzDeomaMgAqUFA3vOIaFGiaCCMIpv8";
        return url;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode == MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION){
            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                //abc
            }else{

            }
        }
    }

    @Override
    public void onTaskDone(Object... values) {
        if (currentPolyline != null)
            currentPolyline.remove();
        currentPolyline = mMap.addPolyline((PolylineOptions) values[0]);
    }

    @Override
    public void onClick(View v) {
        if (v == findViewById(R.id.btRefreshMaps)){
            if(mMap != null) {
                mMap.clear();

                place1 = new MarkerOptions().position(new LatLng(latittude, longitude)).title("Localização atual").icon(BitmapDescriptorFactory.fromBitmap(resizeMapIcons("icon_location_user", 80, 104)));
                place2 = new MarkerOptions().position(new LatLng(-4.965378, -39.006108)).title("Consultorio").icon(BitmapDescriptorFactory.fromBitmap(resizeMapIcons("icon_location", 80, 104)));


                mMap.addMarker(place1);
                mMap.addMarker(place2);

                LatLngBounds.Builder builder = new LatLngBounds.Builder();
                builder.include(place1.getPosition());
                builder.include(place2.getPosition());
                LatLngBounds bounds = builder.build();

                int padding = 200;
                CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);

                mMap.animateCamera(cu);
            }
        }
    }
}
