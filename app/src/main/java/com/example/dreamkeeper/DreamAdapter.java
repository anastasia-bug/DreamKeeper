package com.example.dreamkeeper;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class DreamAdapter extends RecyclerView.Adapter<DreamAdapter.DreamViewHolder> {

    private List<Dream> dreamList;
    private OnDreamClickListener onDreamClickListener;

    public interface OnDreamClickListener {
        void onDreamClick(Dream dream);
    }

    public DreamAdapter(List<Dream> dreamList, OnDreamClickListener onDreamClickListener) {
        if (dreamList == null) {
            this.dreamList = new ArrayList<>();
        } else {
            this.dreamList = dreamList;
        }
        this.onDreamClickListener = onDreamClickListener;
    }

    @NonNull
    @Override
    public DreamViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_dream, parent, false);
        return new DreamViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DreamViewHolder holder, int position) {
        Dream dream = dreamList.get(position);
        holder.bind(dream, onDreamClickListener);
    }

    @Override
    public int getItemCount() {
        return dreamList.size();
    }

    public void updateDreams(List<Dream> newDreamList) {
        if (newDreamList != null) {
            dreamList.clear();
            dreamList.addAll(newDreamList);
            notifyDataSetChanged();
        }
    }

    public static class DreamViewHolder extends RecyclerView.ViewHolder {
        private TextView titleTextView;
        private TextView dateTextView;
        private TextView lucidityTextView;
        private TextView contentTextView;

        public DreamViewHolder(@NonNull View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.textViewTitle);
            dateTextView = itemView.findViewById(R.id.textViewDate);
            lucidityTextView = itemView.findViewById(R.id.textViewLucidity);
            contentTextView = itemView.findViewById(R.id.textViewContent);
        }

        public void bind(final Dream dream, final OnDreamClickListener onDreamClickListener) {
            titleTextView.setText(dream.getName());
            dateTextView.setText(dream.getFormattedDate());
            lucidityTextView.setText(dream.getLucidityLevel() > 0 ? "Осознанный" : "");
            contentTextView.setText(dream.getDescription());

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onDreamClickListener.onDreamClick(dream);
                }
            });
        }

    }

}
