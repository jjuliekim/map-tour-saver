package com.example.kim_j_project4;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.lifecycle.ViewModelProvider;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.example.kim_j_project4.databinding.ActivityDashboardBinding;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Scanner;

public class DashboardActivity extends AppCompatActivity implements OnMapReadyCallback {
    private GoogleMap mMap;
    private String username;
    private DashboardViewModel dashboardViewModel;
    private LocationManager locationManager;
    private String mediaPath;
    private ArrayList<Tour> tourList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Toast.makeText(this, "Loading", Toast.LENGTH_SHORT).show();
        super.onCreate(savedInstanceState);
        com.example.kim_j_project4.databinding.ActivityDashboardBinding binding = ActivityDashboardBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        // set greeting text
        Intent myIntent = getIntent();
        username = myIntent.getStringExtra("username");
        TextView welcomeText = findViewById(R.id.welcome_text);
        welcomeText.setText(String.format("Welcome, %s!", username));

        dashboardViewModel = new ViewModelProvider(this).get(DashboardViewModel.class);
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        mediaPath = "";
        loadTours();

        // add media button
        Button addMediaButton = findViewById(R.id.add_media_button);
        addMediaButton.setOnClickListener(v -> pickMedia());

        // save tour button
        Button saveButton = findViewById(R.id.save_tour_button);
        saveButton.setOnClickListener(v -> saveTour());

        // view tours button
        Button viewToursButton = findViewById(R.id.view_tours_button);
        viewToursButton.setOnClickListener(v -> {
            if (!tourList.isEmpty()) { // only view tours if there are tours to view
                Log.i("HERE DASHBOARD", "next intent");
                Intent nextIntent = new Intent(DashboardActivity.this, ViewToursActivity.class);
                nextIntent.putExtra("username", username);
                startActivity(nextIntent);
            } else {
                Log.i("HERE DASHBOARD", "tour list is empty");
                Toast.makeText(this, "No Tours Saved", Toast.LENGTH_SHORT).show();
            }
        });

        // refreshing/updating map
        dashboardViewModel.getLocations().observe(this, locations -> {
            if (mMap != null) {
                mMap.clear();
                for (LatLng location : locations) {
                    mMap.addMarker(new MarkerOptions().position(location));
                }
            }
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.i("HERE DASHBOARD", "map ready");
        mMap = googleMap;
        // check permissions
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            return;
        }
        // get user's current location and move camera there
        mMap.setMyLocationEnabled(true);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, new LocationListener() {
            @Override
            public void onLocationChanged(@NonNull Location location) {
                LatLng currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
                Log.i("HERE DASHBOARD", "curr loc: " + location.getLatitude() + ", " + location.getLongitude());
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 10));
                locationManager.removeUpdates(this);
            }
        });
        // restore marked locations
        ArrayList<LatLng> savedLocations = dashboardViewModel.getLocations().getValue();
        if (savedLocations != null) {
            for (LatLng location : savedLocations) {
                mMap.addMarker(new MarkerOptions().position(location));
            }
        }

        // allow users to mark locations of interest on the map (store that in ViewModel)
        mMap.setOnMapClickListener(location -> dashboardViewModel.addLocation(location));
    }

    // save tour information
    private void saveTour() {
        EditText nameEditText = findViewById(R.id.tour_name);
        EditText descriptionEditText = findViewById(R.id.tour_description);
        EditText webLinkEditText = findViewById(R.id.tour_link);
        String tourName = nameEditText.getText().toString();
        String tourDescription = descriptionEditText.getText().toString();
        String webLink = webLinkEditText.getText().toString();
        ArrayList<LatLng> locations = dashboardViewModel.getLocations().getValue();

        // check for valid entries
        if (tourName.isEmpty() || tourDescription.isEmpty() || locations == null || locations.isEmpty()) {
            Toast.makeText(this, "Empty Entries", Toast.LENGTH_SHORT).show();
            return;
        }

        if (webLink.isEmpty()) {
            webLink = "";
        }

        // save tour to json file
        Tour tour = new Tour(tourName, tourDescription, webLink, mediaPath, locations);
        tourList.add(tour);
        saveToursToJson();

        // clear page
        nameEditText.setText("");
        descriptionEditText.setText("");
        webLinkEditText.setText("");
        mMap.clear();
    }

    // pick media
    private void pickMedia() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        startActivityForResult(intent, 2);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 2 && resultCode == RESULT_OK && data != null) {
            Uri selectedMediaUri = data.getData();
            if (selectedMediaUri != null) {
                mediaPath = selectedMediaUri.toString();
            }
        }
    }

    // load previously saved tours
    private void loadTours() {
        // check if file exists
        File file = new File(getFilesDir(), username + ".json");
        Log.i("HERE DASHBOARD", file.getAbsolutePath());
        if (!file.exists()) {
            tourList = new ArrayList<>();
            Log.i("HERE DASHBOARD", "no json file yet");
            return;
        }
        try {
            FileInputStream fis = openFileInput(username + ".json");
            Scanner scanner = new Scanner(fis);
            StringBuilder stringBuilder = new StringBuilder();
            while (scanner.hasNextLine()) {
                stringBuilder.append(scanner.nextLine());
            }
            scanner.close();
            fis.close();

            String jsonString = stringBuilder.toString();
            JSONObject userJson = new JSONObject(jsonString);
            JSONArray tourArray = userJson.getJSONArray("tourList");

            tourList = new ArrayList<>();
            for (int i = 0; i < tourArray.length(); i++) {
                JSONObject tourJson = tourArray.getJSONObject(i);
                String tourName = tourJson.getString("tourName");
                String description = tourJson.getString("description");
                String webLink = tourJson.getString("webLink");
                String mediaPath = tourJson.getString("mediaPath");

                JSONArray locationArray = tourJson.getJSONArray("locations");
                ArrayList<LatLng> locations = new ArrayList<>();
                for (int j = 0; j < locationArray.length(); j++) {
                    JSONObject locationJson = locationArray.getJSONObject(j);
                    double latitude = locationJson.getDouble("latitude");
                    double longitude = locationJson.getDouble("longitude");
                    locations.add(new LatLng(latitude, longitude));
                }

                Tour tour = new Tour(tourName, description, webLink, mediaPath, locations);
                tourList.add(tour);
            }
        } catch (FileNotFoundException e) {
            tourList = new ArrayList<>();
        } catch (Exception e) {
            Toast.makeText(this, "Failed to load tours", Toast.LENGTH_SHORT).show();
            Log.i("HERE DASHBOARD", "Failed to load tours", e);
        }
    }

    // save tours to json file
    private void saveToursToJson() {
        try {
            JSONArray tourArray = new JSONArray();
            // Tour -> Location
            for (Tour tour : tourList) {
                JSONObject tourJson = new JSONObject();
                tourJson.put("tourName", tour.getName());
                tourJson.put("description", tour.getDescription());
                tourJson.put("webLink", tour.getWebLink());
                tourJson.put("mediaPath", tour.getMediaPath());
                // Location -> LatLng
                JSONArray locationArray = new JSONArray();
                for (LatLng location : tour.getLocations()) {
                    JSONObject locationJson = new JSONObject();
                    locationJson.put("latitude", location.latitude);
                    locationJson.put("longitude", location.longitude);
                    locationArray.put(locationJson);
                }
                tourJson.put("locations", locationArray);
                tourArray.put(tourJson);
            }
            // Json File: Username -> TourList
            JSONObject userJson = new JSONObject();
            userJson.put("username", username);
            userJson.put("tourList", tourArray);

            // save to file
            String json = userJson.toString();
            FileOutputStream fos = openFileOutput(username + ".json", Context.MODE_PRIVATE);
            fos.write(json.getBytes());
            fos.close();
            Toast.makeText(this, "Tour Saved", Toast.LENGTH_SHORT).show();
            Log.i("HERE DASHBOARD", "tour saved");
        } catch (Exception e) {
            Toast.makeText(this, "Failed to save tour", Toast.LENGTH_SHORT).show();
            Log.e("DashboardActivity", "Failed to save tour", e);
        }
    }
}