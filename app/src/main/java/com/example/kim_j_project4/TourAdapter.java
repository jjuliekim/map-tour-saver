package com.example.kim_j_project4;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;

public class TourAdapter extends RecyclerView.Adapter<TourAdapter.TourViewHolder> {
    private ArrayList<Tour> tourList;

    public TourAdapter(ArrayList<Tour> tourList) {
        this.tourList = tourList;
    }

    @NonNull
    @Override
    public TourViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.tour_item, parent, false);
        return new TourViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull TourViewHolder holder, int position) {
        Tour tour = tourList.get(position);
        holder.tourNameText.setText(tour.getName());
        holder.tourDescText.setText(tour.getDescription());
        // Handle item click
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), TourDetailsActivity.class);
            intent.putExtra("tour", tour);
            intent.putExtra("position", position);
            ((ViewToursActivity) v.getContext()).startActivityForResult(intent, 1);
        });
    }

    @Override
    public int getItemCount() {
        return tourList.size();
    }

    // update tour
    public void updateTour(int position, Tour updatedTour) {
        tourList.set(position, updatedTour);
        notifyItemChanged(position);
    }

    public static class TourViewHolder extends RecyclerView.ViewHolder {
        public TextView tourNameText;
        public TextView tourDescText;

        public TourViewHolder(@NonNull View itemView) {
            super(itemView);
            tourNameText = itemView.findViewById(R.id.tour_name_text);
            tourDescText = itemView.findViewById(R.id.tour_desc_text);
        }
    }
}
