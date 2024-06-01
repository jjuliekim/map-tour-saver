package com.example.kim_j_project4;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class TourAdapter extends RecyclerView.Adapter<TourAdapter.TourViewHolder> {
    private ArrayList<Tour> tourList;
    private OnItemClickListener listener;

    public TourAdapter(ArrayList<Tour> tourList, OnItemClickListener listener) {
        this.tourList = tourList;
        this.listener = listener;
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
    }

    @Override
    public int getItemCount() {
        return tourList.size();
    }

    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    public class TourViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public TextView tourNameText;
        public TextView tourDescText;

        public TourViewHolder(@NonNull View itemView) {
            super(itemView);
            tourNameText = itemView.findViewById(R.id.tour_name_text);
            tourDescText = itemView.findViewById(R.id.tour_desc_text);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (listener != null) {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    listener.onItemClick(position);
                }
            }
        }
    }
}
