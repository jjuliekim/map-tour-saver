package com.example.kim_j_project4;

import com.google.android.gms.maps.model.LatLng;
import java.util.ArrayList;

public class Tour {
    private String name;
    private String description;
    private ArrayList<LatLng> locations;

    // constructor
    public Tour(String name, String description, ArrayList<LatLng> locations) {
        this.name = name;
        this.description = description;
        this.locations = locations;
    }

    // getters and setters
    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public ArrayList<LatLng> getLocations() {
        return locations;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setLocations(ArrayList<LatLng> locations) {
        this.locations = locations;
    }
}
