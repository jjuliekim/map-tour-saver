package com.example.kim_j_project4;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Scanner;

public class ViewToursActivity extends AppCompatActivity {
    private String username;
    private ArrayList<Tour> tourList;
    private RecyclerView recyclerView;
    private TourAdapter tourAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_view_tours);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        tourAdapter = new TourAdapter(tourList);
        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(tourAdapter);

        Intent myIntent = getIntent();
        username = myIntent.getStringExtra("username");
        loadTours();
    }

    // load tours from json file
    private void loadTours() {
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
                Log.i("HERE VIEW TOURS", "loaded " + tourName);
            }
            Toast.makeText(this, "Tours loaded", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, "Failed to load tours", Toast.LENGTH_SHORT).show();
            Log.i("HERE VIEW TOURS", "Failed to load tours", e);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1 && resultCode == RESULT_OK && data != null) {
            Tour updatedTour = (Tour) data.getSerializableExtra("updatedTour");
            int position = data.getIntExtra("position", -1);

            if (position != -1 && updatedTour != null) {
                tourAdapter.updateTour(position, updatedTour);
            }
        }
    }

}