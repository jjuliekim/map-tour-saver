package com.example.kim_j_project4;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;

// store marked locations of interests on the map
// (location data in a LiveData object within a ViewModel)
public class DashboardViewModel extends ViewModel {
    public MutableLiveData<ArrayList<LatLng>> locations;     // tour locations

    // constructor
    public DashboardViewModel() {
        locations = new MutableLiveData<>();
        locations.setValue(new ArrayList<>());
    }

    public LiveData<ArrayList<LatLng>> getLocations() {
        return locations;
    }

    // add location to list
    public void addLocation(LatLng location) {
        ArrayList<LatLng> currentList = locations.getValue();
        if (currentList != null) {
            currentList.add(location);
            locations.setValue(currentList);
        }
    }

    // clear locations
    public void clearLocations() {
        locations.setValue(new ArrayList<>());
    }
}
