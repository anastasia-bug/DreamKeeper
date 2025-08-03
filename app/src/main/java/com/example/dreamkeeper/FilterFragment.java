package com.example.dreamkeeper;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.ColorStateList;
import android.graphics.Typeface;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FilterFragment extends BottomSheetDialogFragment {

    EditText queryEditText;
    Button startDateButton, endDateButton;
    ChipGroup lucidityChipGroup;
    LinearLayout tagsContainer;
    Button resetButton;

    FilterableViewModel viewModel;

    boolean isUpdatingUI = false;

    public static FilterFragment newInstance(FilterSource source) {
        FilterFragment fragment = new FilterFragment();
        Bundle args = new Bundle();
        args.putString("filter_source", source.name());
        fragment.setArguments(args);
        return fragment;
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_filter, container, false);

        FilterSource source = FilterSource.valueOf(getArguments().getString("filter_source"));
        if (source == FilterSource.STATISTICS) {
            viewModel = new ViewModelProvider(requireActivity()).get(StatisticsViewModel.class);
        } else {
            viewModel = new ViewModelProvider(requireActivity()).get(DreamListViewModel.class);
        }


        queryEditText = view.findViewById(R.id.queryEditText);
        startDateButton = view.findViewById(R.id.startDateButton);
        endDateButton = view.findViewById(R.id.endDateButton);
        lucidityChipGroup = view.findViewById(R.id.lucidityChipGroup);
        tagsContainer = view.findViewById(R.id.tagsContainer);
        resetButton = view.findViewById(R.id.resetButton);

        queryEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!isUpdatingUI) {
                viewModel.updateFilter(f -> { f.setQuery(s.toString()); return f; });
            }
                }
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}
        });

        startDateButton.setOnClickListener(v -> {
            showDatePickerDialog(true);
        });

        endDateButton.setOnClickListener(v -> {
            showDatePickerDialog(false);
        });

        resetButton.setOnClickListener(v -> {
            new AlertDialog.Builder(requireContext())
                    .setTitle("Очистка фильтров")
                    .setMessage("Вы уверены, что хотите очистить фильтры?")
                    .setPositiveButton("Очистить", (dialog, which) -> resetFilter())
                    .setNegativeButton("Отмена", null)
                    .show();
        });

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        viewModel.getAllTags().observe(getViewLifecycleOwner(), tags -> {
            Filter filter = viewModel.getFilter().getValue();
            List<TagCategory> categories = viewModel.getCategories().getValue();
            if (filter != null && categories != null) {
                updateUI(filter, tags, categories);
            }
        });

        viewModel.getFilter().observe(getViewLifecycleOwner(), filter -> {
            List<Tag> tags = viewModel.getAllTags().getValue();
            List<TagCategory> categories = viewModel.getCategories().getValue();
            if (tags != null && categories != null) {
                updateUI(filter, tags, categories);
            }
        });

    }

    private void resetFilter() {
        viewModel.updateFilter(f -> {
            f.setQuery("");
            f.setStartDate(null);
            f.setEndDate(null);
            f.setLucidityLevels(new ArrayList<>());
            f.setSelectedTags(new ArrayList<>());
            return f;
        });
    }

    private void updateUI(Filter filter, List<Tag> tags, List<TagCategory> categories) {
        isUpdatingUI = true;
        Log.d("updateUI", "updateUI called");

        if (filter == null) return;

        String newQuery = filter.getQuery();
        if (!queryEditText.getText().toString().equals(newQuery)) {
            int selectionStart = queryEditText.getSelectionStart();
            queryEditText.setText(newQuery);
            queryEditText.setSelection(Math.min(selectionStart, newQuery.length()));
        }

        addLucidityLevels();

        if (filter.getStartDate() != null) {
            startDateButton.setText(Dream.getFormattedDate(filter.getStartDate()));
        } else {
            startDateButton.setText("Выбрать...");
        }

        if (filter.getEndDate() != null) {
            endDateButton.setText(Dream.getFormattedDate(filter.getEndDate()));
        } else {
            endDateButton.setText("Выбрать...");
        }

        renderTags(tags, categories);

        isUpdatingUI = false;
    }

    private void addLucidityLevels() {
        lucidityChipGroup.removeAllViews();

        List<String> levels = Dream.getAllLucidityLevels(requireContext());

        List<Integer> selectedLevels = viewModel.getFilter().getValue() != null
                ? viewModel.getFilter().getValue().getLucidityLevels()
                : new ArrayList<>();

        for (int i = 0; i < levels.size(); i++) {
            final Integer level = i;
            Chip chip = new Chip(requireContext());
            chip.setText(levels.get(i));
            chip.setCheckable(true);
            chip.setChecked(selectedLevels.contains(level));
            chip.setTextStartPadding(10);
            chip.setTextEndPadding(10);

            chip.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (!isUpdatingUI) {
                    viewModel.updateFilter(f -> {
                        List<Integer> updated = new ArrayList<>(f.getLucidityLevels());
                        if (isChecked && !updated.contains(level)) {
                            updated.add(level);
                        } else if (!isChecked) {
                            updated.remove(level);
                        }
                        f.setLucidityLevels(updated);
                        return f;
                    });
                    }
            });


            lucidityChipGroup.addView(chip);
        }
    }

    private void showDatePickerDialog(final boolean isStartDate) {

        Filter filter = viewModel.getFilter().getValue();

        LocalDate date = isStartDate ? filter.getStartDate() : filter.getEndDate();

        if (date == null) {
            date = LocalDate.now();
        }

        DatePickerDialog datePickerDialog = new DatePickerDialog(getContext(),
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int selectedYear, int selectedMonth, int selectedDay) {
                        LocalDate selectedDate = LocalDate.of(selectedYear, selectedMonth + 1, selectedDay);
                        if (isStartDate) {
                            viewModel.updateFilter(f -> {
                                f.setStartDate(selectedDate);
                                return f;
                            });
                            startDateButton.setText(Dream.getFormattedDate(selectedDate));
                        } else {
                            viewModel.updateFilter(f -> {
                                f.setEndDate(selectedDate);
                                return f;
                            });
                            endDateButton.setText(Dream.getFormattedDate(selectedDate));
                        }
                    }
                }, date.getYear(),
                date.getMonthValue() - 1,
                date.getDayOfMonth()
        );

        // Кнопка для очистки даты
        datePickerDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Очистить", (dialog, which) -> {
            if (isStartDate) {
                viewModel.updateFilter(f -> {
                    f.setStartDate(null);
                    return f;
                });
                startDateButton.setText("Выбрать...");
            } else {
                viewModel.updateFilter(f -> {
                    f.setEndDate(null);
                    return f;
                });
                endDateButton.setText("Выбрать...");
            }
        });

        datePickerDialog.setButton(DialogInterface.BUTTON_POSITIVE, "ОК", datePickerDialog);
        datePickerDialog.setButton(DialogInterface.BUTTON_NEUTRAL, "Отмена", (dialog, which) -> {});

        ZoneId zoneId = ZoneId.systemDefault();

        if (isStartDate) {
            LocalDate endDate = filter.getEndDate();
            if (endDate != null) {
            datePickerDialog.getDatePicker().setMaxDate(endDate.atStartOfDay(zoneId).toEpochSecond() * 1000);
            }
        } else {
            LocalDate startDate = filter.getStartDate();
            if (startDate != null) {
                datePickerDialog.getDatePicker().setMinDate(filter.getStartDate().atStartOfDay(zoneId).toEpochSecond() * 1000);
            }
        }



        datePickerDialog.show();
    }

    private void renderTags(List<Tag> tags, List<TagCategory> categories) {
        tagsContainer.removeAllViews();

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

        List<Tag> selectedTags = viewModel.getFilter().getValue().getSelectedTags();


        for (TagCategory category : categories) {
            Integer categoryId = category.getId();
            String categoryName = category.getName();

            TextView categoryNameTextView = new TextView(requireContext());
            categoryNameTextView.setText(categoryName);
            categoryNameTextView.setTypeface(null, Typeface.BOLD);
            categoryNameTextView.setPadding(16, 32, 16, 8);
            tagsContainer.addView(categoryNameTextView);

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
                tagsContainer.addView(noTagFoundTextView);
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
                chip.setOnCheckedChangeListener((buttonView, isChecked) -> {
                    viewModel.updateFilter(f -> {
                        List<Tag> updatedTags = new ArrayList<>(f.getSelectedTags());
                        if (isChecked && !updatedTags.contains(tag)) {
                            updatedTags.add(tag);
                        } else if (!isChecked) {
                            updatedTags.remove(tag);
                        }
                        f.setSelectedTags(updatedTags);
                        return f;
                    });
                });
                chipGroup.addView(chip);
            }

            tagsContainer.addView(chipGroup);

        }

    }

}