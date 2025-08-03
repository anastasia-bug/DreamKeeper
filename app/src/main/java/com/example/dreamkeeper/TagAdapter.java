package com.example.dreamkeeper;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class TagAdapter extends RecyclerView.Adapter<TagAdapter.TagViewHolder> {

    private List<TagDisplayData> tagList;
    private OnTagClickListener onTagClickListener;

    public interface OnTagClickListener {
        void onTagClick(Tag tag);
    }

    public TagAdapter(List<TagDisplayData> tagList, OnTagClickListener onTagClickListener) {
        if (tagList == null) {
            this.tagList = new ArrayList<>();
        } else {
            this.tagList = tagList;
        }
        this.onTagClickListener = onTagClickListener;
    }

    @NonNull
    @Override
    public TagViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_tag, parent, false);
        return new TagViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TagViewHolder holder, int position) {
        TagDisplayData data = tagList.get(position);
        holder.bind(data, onTagClickListener);
    }

    @Override
    public int getItemCount() {
        return tagList.size();
    }

    // Обновить список данных
    public void updateTags(List<TagDisplayData> newTagList) {
        if (newTagList != null) {
            tagList.clear();
            tagList.addAll(newTagList);
            notifyDataSetChanged();
        }
    }

    public static class TagViewHolder extends RecyclerView.ViewHolder {
        private TextView nameTextView;
        private TextView categoryTextView;
        private TextView dreamCountTextView;

        public TagViewHolder(@NonNull View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.textViewName);
            categoryTextView = itemView.findViewById(R.id.textViewCategory);
            dreamCountTextView = itemView.findViewById(R.id.textViewDreamCount);
        }

        public void bind(final TagDisplayData data, final OnTagClickListener listener) {
            nameTextView.setText(data.getTag().getName());
            categoryTextView.setText(data.getCategoryName());
            dreamCountTextView.setText(data.getDreamCount() + " (" + data.getPercentageText() + ")");

            itemView.setOnClickListener(v -> listener.onTagClick(data.getTag()));
        }
    }
}