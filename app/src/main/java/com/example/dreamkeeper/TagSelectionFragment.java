package com.example.dreamkeeper;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Typeface;
import android.os.Bundle;
import com.example.dreamkeeper.Tag;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TagSelectionFragment extends BottomSheetDialogFragment {

    private EditText searchEditText;
    private LinearLayout categoriesContainer;
    private Button confirmButton;
    private TagSelectionViewModel viewModel;

    public TagSelectionFragment() {
    }

    public static TagSelectionFragment newInstance() {
        return new TagSelectionFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(TagSelectionViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_tag_selection, container, false);
        searchEditText = view.findViewById(R.id.editTextSearch);
        categoriesContainer = view.findViewById(R.id.categoriesContainer);
        confirmButton = view.findViewById(R.id.buttonConfirm);

        viewModel.getAllTags().observe(getViewLifecycleOwner(), tags -> {
            filterTags(searchEditText.getText().toString().trim());
        });

        viewModel.getSelectedTags().observe(getViewLifecycleOwner(), selected -> {
            filterTags(searchEditText.getText().toString().trim());
        });

        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String query = s.toString().trim();
                filterTags(query);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        confirmButton.setOnClickListener(v -> {
            dismiss();
        });

        renderTagsGroupedByCategory(viewModel.getAllTags().getValue(), null);

        return view;

    }

    private void filterTags(String query) {

        List<Tag> allTags = viewModel.getAllTags().getValue();

        if (query.isEmpty()) {
            renderTagsGroupedByCategory(allTags, null);
            return;
        }

        List<Tag> filtered = new ArrayList<>();
        for (Tag tag : allTags) {
            if (tag.getName().toLowerCase().contains(query.toLowerCase())) {
                filtered.add(tag);
            }
        }

        renderTagsGroupedByCategory(filtered, query);
    }


    private void renderTagsGroupedByCategory(List<Tag> tags, String query) {
        categoriesContainer.removeAllViews();

        TypedValue typedValue = new TypedValue();
        TypedValue typedValue2 = new TypedValue();
        Context context = requireContext();
        context.getTheme().resolveAttribute(com.google.android.material.R.attr.colorPrimary, typedValue, true);
        context.getTheme().resolveAttribute(com.google.android.material.R.attr.colorOnPrimary, typedValue2, true);

        ColorStateList colorPrimary = ContextCompat.getColorStateList(context, typedValue.resourceId);
        ColorStateList transparent = ContextCompat.getColorStateList(context, android.R.color.transparent);
        ColorStateList colorOnPrimary = ContextCompat.getColorStateList(context, typedValue2.resourceId);

        Map<Integer, List<Tag>> grouped = new HashMap<>();
        for (Tag tag : tags) {
            grouped.computeIfAbsent(tag.getCategoryId(), k -> new ArrayList<>()).add(tag);
        }

        boolean exactMatchFound = true;

        if (query != null && !query.isEmpty()) {

            exactMatchFound = false;

            for (Tag tag : tags) {
                if (tag.getName().equalsIgnoreCase(query)) {
                    exactMatchFound = true;
                    break;
                }
            }
        }

        List<Tag> selectedTags = viewModel.getSelectedTags().getValue();
        List<TagCategory> categories = viewModel.getCategories().getValue();

        if (!exactMatchFound) {
            TextView addNewTagTextView = new TextView(requireContext());
            addNewTagTextView.setVisibility(View.VISIBLE);
            addNewTagTextView.setText("Добавить тег \"" + query + "\"...");
            addNewTagTextView.setTextColor(ContextCompat.getColor(requireContext(), com.google.android.material.R.color.design_default_color_primary));
            addNewTagTextView.setPadding(32, 16, 32, 16);
            addNewTagTextView.setOnClickListener(v -> {
                showCategoryPickerDialog(query);
            });
            categoriesContainer.addView(addNewTagTextView);
        }

        for (TagCategory category : categories) {
            Integer categoryId = category.getId();
            String categoryName = category.getName();


            TextView categoryNameTextView = new TextView(requireContext());
            categoryNameTextView.setText(categoryName);
            categoryNameTextView.setTypeface(null, Typeface.BOLD);
            categoryNameTextView.setPadding(16, 32, 16, 8);
            categoriesContainer.addView(categoryNameTextView);

            ChipGroup chipGroup = new ChipGroup(requireContext());
            chipGroup.setSingleLine(false);
            chipGroup.setChipSpacingVertical(-6);

            List<Tag> categoryTags = grouped.getOrDefault(categoryId, new ArrayList<>());

            if (categoryTags.isEmpty()) {
                TextView noTagFoundTextView = new TextView(requireContext());
                noTagFoundTextView.setVisibility(View.VISIBLE);
                noTagFoundTextView.setTextColor(ContextCompat.getColor(requireContext(), com.google.android.material.R.color.material_grey_600));
                noTagFoundTextView.setText("Теги в категории не найдены");
                noTagFoundTextView.setPadding(16, 0, 16, 0);
                categoriesContainer.addView(noTagFoundTextView);
                continue;
            }

            for (Tag tag : categoryTags) {
                Chip chip = new Chip(requireContext());
                chip.setText(tag.getName());
                chip.setCheckable(true);
                chip.setChecked(selectedTags.contains(tag));
                chip.setChipStrokeColor(colorPrimary);
                chip.setTextStartPadding(10);
                chip.setTextEndPadding(10);

                if (selectedTags.contains(tag)) {
                    chip.setChipBackgroundColor(colorPrimary);
                    chip.setTextColor(colorOnPrimary);
                } else {
                    chip.setChipBackgroundColor(transparent);
                    chip.setTextColor(colorPrimary);
                }
                chip.setOnClickListener(v -> viewModel.toggleTag(tag));
                chipGroup.addView(chip);
            }

            categoriesContainer.addView(chipGroup);

        }
    }

    private void showCategoryPickerDialog(String tagName) {
        List<TagCategory> categories = viewModel.getCategories().getValue();

        String[] categoryNames = new String[categories.size()];
        for (int i = 0; i < categories.size(); i++) {
            categoryNames[i] = categories.get(i).getName();
        }

        new AlertDialog.Builder(requireContext())
                .setTitle("Добавить тег в категорию:")
                .setItems(categoryNames, (dialog, which) -> {
                    TagCategory selectedCategory = categories.get(which);
                    Tag newTag = new Tag(null, tagName, "", selectedCategory.getId());
                    viewModel.addNewTag(newTag);

                     searchEditText.setText("");
                })
                .setNegativeButton("Отмена", null)
                .show();
    }

}