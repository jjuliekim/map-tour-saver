package com.example.kim_j_project4;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.lifecycle.ViewModelProvider;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
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
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

public class DashboardActivity extends AppCompatActivity implements OnMapReadyCallback {
    private GoogleMap mMap;
    private String username;
    private DashboardViewModel dashboardViewModel;
    private LocationManager locationManager;
    private String mediaPath;
    private ArrayList<Tour> tourList;
    private MediaRecorder recorder;
    private Button addAudioButton;
    private Button addVideoButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Toast.makeText(this, "Loading", Toast.LENGTH_SHORT).show();
        super.onCreate(savedInstanceState);
        ActivityDashboardBinding binding = ActivityDashboardBinding.inflate(getLayoutInflater());
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

        // add video button
        addVideoButton = findViewById(R.id.add_video_button);
        addVideoButton.setOnClickListener(v -> recordVideo());

        // add audio button
        addAudioButton = findViewById(R.id.add_audio_button);
        addAudioButton.setOnClickListener(v -> recordAudio());

        // save tour button
        Button saveButton = findViewById(R.id.save_tour_button);
        saveButton.setOnClickListener(v -> saveTour());

        // view tours button
        Button viewToursButton = findViewById(R.id.view_tours_button);
        viewToursButton.setOnClickListener(v -> {
            if (!tourList.isEmpty()) { // only view tours if there are tours to view
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
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
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

    // record video
    private void recordVideo() {
        if (getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)) {
            addAudioButton.setVisibility(View.INVISIBLE);
            Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
            if (intent.resolveActivity(getPackageManager()) != null) {
                File videoFile = null;
                try {
                    videoFile = createVideoFile();
                } catch (IOException e) {
                    Log.e("DashboardActivity", "Error creating video file", e);
                }
                if (videoFile != null) {
                    Uri videoUri = Uri.fromFile(videoFile);
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, videoUri);
                    startActivityForResult(intent, 101);
                }
            }
        } else {
            Log.i("HERE DASHBOARD", "no camera");
            Toast.makeText(this, "Camera Error", Toast.LENGTH_SHORT).show();
        }
    }

    // create file to save the video
    private File createVideoFile() throws IOException {
        String fileName = "video_" + System.currentTimeMillis();
        File storageDir = getExternalFilesDir(null);
        File videoFile = File.createTempFile(fileName, ".mp4", storageDir);
        mediaPath = videoFile.getAbsolutePath();
        Log.i("HERE DASHBOARD", "video path: " + mediaPath);
        return videoFile;
    }

    // record audio
    private void recordAudio() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 102);
        } else {
            addVideoButton.setVisibility(View.INVISIBLE);
            startAudioRecording();
        }
    }

    private void startAudioRecording() {
        String audioPath = getExternalCacheDir().getAbsolutePath() + username + "_audio.mp3";
        Log.i("HERE DASHBOARD", "audio path: " + audioPath);
        mediaPath = audioPath;
        recorder = new MediaRecorder();
        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        recorder.setOutputFile(audioPath);
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        try {
            recorder.prepare();
        } catch (Exception e) {
            Log.e("HERE DASHBOARD", "prepare() failed");
        }
        recorder.start();
        Toast.makeText(this, "Recording Audio...", Toast.LENGTH_LONG).show();
    }

    private void stopAudioRecording() {
        recorder.stop();
        recorder.release();
        recorder = null;
        Toast.makeText(this, "Audio Recorded", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 101 && resultCode == RESULT_OK && data != null) {
            Uri selectedMediaUri = data.getData();
            if (selectedMediaUri != null) {
                mediaPath = selectedMediaUri.toString();
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (recorder != null) {
            stopAudioRecording();
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

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 102) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startAudioRecording();
            } else {
                Toast.makeText(this, "Audio permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

}