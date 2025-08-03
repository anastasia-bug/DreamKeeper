package com.example.dreamkeeper;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.OptIn;
import androidx.core.content.ContextCompat;
import androidx.core.view.MenuProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.android.material.badge.ExperimentalBadgeUtils;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.jolenechong.wordcloud.Word;
import com.jolenechong.wordcloud.WordCloud;

public class StatisticsFragment extends Fragment {

    private TextView totalDreamsTextView, lucidDreamsPercentageTextView;
    private ChipGroup topTagsChipGroup;

    private PieChart lucidityPieChart;
    StatisticsViewModel viewModel;
    MenuItem filterItem;
    FrameLayout wordCloudView;

    MaterialButtonToggleGroup toggleGroup;

    private LinearLayout emptyStateLayout;
    private TextView emptyTextView;
    private ScrollView statisticsContent;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_statistics, container, false);

        totalDreamsTextView = view.findViewById(R.id.textViewTotalDreams);
        lucidDreamsPercentageTextView = view.findViewById(R.id.textViewLucidDreamsPercentage);
        topTagsChipGroup = view.findViewById(R.id.chipGroupTopTags);
        lucidityPieChart = view.findViewById(R.id.pieChartLucidity);
        wordCloudView = view.findViewById(R.id.wordCloudView);
        toggleGroup = view.findViewById(R.id.toggleGroupViewMode);
        emptyStateLayout = view.findViewById(R.id.emptyStateLayout);
        emptyTextView = view.findViewById(R.id.textViewEmpty);
        statisticsContent = view.findViewById(R.id.contentLayout);


        toggleGroup.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (!isChecked) return;

            TypedValue typedValue = new TypedValue();
            TypedValue typedValue2 = new TypedValue();
            Context context = requireContext();
            context.getTheme().resolveAttribute(com.google.android.material.R.attr.colorPrimary, typedValue, true);
            context.getTheme().resolveAttribute(com.google.android.material.R.attr.colorOnPrimary, typedValue2, true);

            ColorStateList colorPrimary = ContextCompat.getColorStateList(context, typedValue.resourceId);
            ColorStateList transparent = ContextCompat.getColorStateList(context, android.R.color.transparent);
            ColorStateList colorOnPrimary = ContextCompat.getColorStateList(context, R.color.white);

            for (int i = 0; i < group.getChildCount(); i++) {
                View child = group.getChildAt(i);
                if (child instanceof MaterialButton)  {
                    MaterialButton button = (MaterialButton) child;

                    boolean isSelected = button.getId() == checkedId;

                    button.setBackgroundTintList(isSelected ? colorPrimary : transparent);
                    button.setTextColor(isSelected ? colorOnPrimary : colorPrimary);

                    if (isSelected) {
                        boolean showChips = button.getId() == R.id.btnChipsView;
                        topTagsChipGroup.setVisibility(showChips ? View.VISIBLE : View.GONE);
                        wordCloudView.setVisibility(showChips ? View.GONE : View.VISIBLE);
                    }
                }
            }

        });
        toggleGroup.check(R.id.btnCloudView);


        viewModel = new ViewModelProvider(requireActivity()).get(StatisticsViewModel.class);

        viewModel.getStatistics().observe(getViewLifecycleOwner(), stats -> {
            if (stats == null || stats.getTotalDreams() == 0) {
                emptyStateLayout.setVisibility(View.VISIBLE);
                statisticsContent.setVisibility(View.GONE);

                if (viewModel.hasActiveFilters()) {
                    emptyTextView.setText("Не найдено снов с установленными фильтрами");
                } else {
                    emptyTextView.setText("Недостаточно данных для отображения статистики");
                }

            } else {
                emptyStateLayout.setVisibility(View.GONE);
                statisticsContent.setVisibility(View.VISIBLE);
                loadStatistics(stats);
            }

            updateFilterIcon();
        });


        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        requireActivity().addMenuProvider(new MenuProvider() {
            @OptIn(markerClass = ExperimentalBadgeUtils.class)
            @Override
            public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {
                menu.clear();
                menuInflater.inflate(R.menu.menu_filter, menu);

                filterItem = menu.findItem(R.id.action_filter);
                Log.d("icon", "addedMenuIcon");

                updateFilterIcon();
            }

            @Override
            public boolean onMenuItemSelected(@NonNull MenuItem menuItem) {
                if (menuItem.getItemId() == R.id.action_filter) {
                    openFilter();
                    return true;
                }
                return false;
            }
        }, getViewLifecycleOwner());
    }

    private void loadStatistics(DreamStatistics stats) {

        totalDreamsTextView.setText(String.valueOf(stats.getTotalDreams()));
        lucidDreamsPercentageTextView.setText(String.format(Locale.getDefault(), "%.1f%%", stats.getLucidDreamsPercentage()));

        topTagsChipGroup.removeAllViews();
        for (TagCount tagCount : stats.getTopTags()) {
            Chip chip = new Chip(getContext());
            chip.setText(tagCount.getTag().getName());
            chip.setTextStartPadding(10);
            chip.setTextEndPadding(10);
            chip.setOnClickListener(v -> {});
            topTagsChipGroup.addView(chip);
        }

        drawLucidityPieChart(stats.getLucidityLevelsCount());

        setupWordCloud(stats.getTopTags());
    }

    private void setupWordCloud(List<TagCount> tagCounts) {
        wordCloudView.removeAllViews();

        WordCloud wordCloud = new WordCloud(requireContext(), null);
        List<String> words = new ArrayList<>();
        for (TagCount tagCount : tagCounts) {
            for (int i = 0; i < tagCount.getCount(); i++) {
                words.add(tagCount.getTag().getName());
            }
        }

        wordCloud.setWords(words, 15);

        wordCloudView.addView(wordCloud);

    }

    private void drawLucidityPieChart(Map<Integer, Integer> levels) {
        List<PieEntry> entries = new ArrayList<>();
        for (Map.Entry<Integer, Integer> entry : levels.entrySet()) {
            String label = Dream.getLucidityLevelName(entry.getKey(), requireContext());
            entries.add(new PieEntry(entry.getValue(), label));
        }

        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setColors(ColorTemplate.VORDIPLOM_COLORS);
        dataSet.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return String.format(Locale.getDefault(), "%.1f%%", value);
            }
        });
        dataSet.setSliceSpace(3f);
        PieData data = new PieData(dataSet);
        data.setValueTextSize(14f);
        data.setValueTextColor(Color.BLACK);

        lucidityPieChart.setData(data);
        lucidityPieChart.setUsePercentValues(true);
        lucidityPieChart.setDrawEntryLabels(false);
        lucidityPieChart.setDrawHoleEnabled(false);
        lucidityPieChart.getDescription().setEnabled(false);

        Legend legend = lucidityPieChart.getLegend();
        legend.setTextColor(Color.GRAY);
        legend.setTextSize(14f);
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.CENTER);
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.RIGHT);
        legend.setOrientation(Legend.LegendOrientation.VERTICAL);
        legend.setDrawInside(false);

        lucidityPieChart.invalidate();
    }

    private void updateFilterIcon() {
        if (filterItem != null) {
            if (viewModel.hasActiveFilters()) {
                filterItem.setIcon(R.drawable.ic_menu_search_with_dot);
                Log.d("icon", "has filters");
            } else {
                filterItem.setIcon(android.R.drawable.ic_menu_search);
                Log.d("icon", "no filters");
            }
        } else {
            Log.d("icon", "filterItem is null");
        }
    }


    private void openFilter() {
        FilterFragment bottomSheet = FilterFragment.newInstance(FilterSource.STATISTICS);
        bottomSheet.show(getChildFragmentManager(), "filter_selection");
    }
}
