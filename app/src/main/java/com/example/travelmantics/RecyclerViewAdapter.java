package com.example.travelmantics;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.example.travelmantics.databinding.RecyclerviewTravelBinding;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.DocumentSnapshot;


public class RecyclerViewAdapter extends FirestoreRecyclerAdapter<TravelDeal, RecyclerViewAdapter.ViewHolder> {

    private Context context;

    private OnItemClickListener listener;


    public RecyclerViewAdapter(@NonNull FirestoreRecyclerOptions<TravelDeal> options, Context context) {
        super(options);
        this.context = context;

    }

    @Override
    protected void onBindViewHolder(@NonNull ViewHolder viewHolder, int i, @NonNull TravelDeal item) {
        TravelDeal travelDeal = new TravelDeal(item.getTitle(), item.getDescription(), "$ " + item.getPrice(), item.getImageUrl());
        viewHolder.recyclerViewBinding.setTravel(travelDeal);

    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        RecyclerviewTravelBinding recyclerViewBindings = DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()), R.layout.recyclerview_travel, parent, false);
        return new ViewHolder(recyclerViewBindings);
    }


    public class ViewHolder extends RecyclerView.ViewHolder {

        RecyclerviewTravelBinding recyclerViewBinding;

        public ViewHolder(@NonNull RecyclerviewTravelBinding itemView) {
            super(itemView.getRoot());

            recyclerViewBinding = itemView;

            itemView.getRoot().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION && listener != null) {
                        listener.onItemClick(getSnapshots().getSnapshot(position), position);
                    }
                }
            });
        }
    }

    public interface OnItemClickListener {
        void onItemClick(DocumentSnapshot documentSnapshot, int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

}
