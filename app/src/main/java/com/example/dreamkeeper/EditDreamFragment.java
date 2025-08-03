package com.example.dreamkeeper;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.view.MenuProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.slider.Slider;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class EditDreamFragment extends Fragment {

    private EditText titleEditText, contentEditText;
    private Button saveButton, selectDateButton;
    private LocalDate selectedDate;
    private ArrayList<Tag> selectedTags;
    private DreamViewModel dreamViewModel;
    TagSelectionViewModel tagViewModel;
    private ChipGroup chipGroupTags;

    private Slider luciditySlider;

    private List<String> lucidityLevels;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_edit_dream, container, false);

        titleEditText = view.findViewById(R.id.editTextTitle);
        contentEditText = view.findViewById(R.id.editTextContent);
        chipGroupTags = view.findViewById(R.id.chipGroupTags);
        selectDateButton = view.findViewById(R.id.buttonSelectDate);
        saveButton = view.findViewById(R.id.buttonSave);
        Button openTagSelectionButton = view.findViewById(R.id.buttonOpenTagSelection);

        luciditySlider = view.findViewById(R.id.sliderLucidity);

        dreamViewModel = new ViewModelProvider(requireActivity()).get(DreamViewModel.class);
        tagViewModel = new ViewModelProvider(requireActivity()).get(TagSelectionViewModel.class);

        boolean isEditing = false;

        if (getArguments() != null) {
            isEditing = getArguments().getBoolean("isEditing", false);
        }

        Dream dream;

        if (!isEditing) {
            dreamViewModel.createNewDream();
        }
        dream = dreamViewModel.getDream().getValue();

        titleEditText.setText(dream.getName());
        contentEditText.setText(dream.getDescription());
        selectedDate = dream.getDate();
        selectDateButton.setText(dream.getFormattedDate());
        selectedTags = new ArrayList<>(dream.getTags());

        tagViewModel.setSelectedTags(selectedTags);

        showSelectedTags();

        selectDateButton.setOnClickListener(v -> showDatePickerDialog());

        openTagSelectionButton.setOnClickListener(v -> openTagSelection());

        lucidityLevels = Dream.getAllLucidityLevels(requireContext());

        luciditySlider.setValue(dream.getLucidityLevel());

        luciditySlider.setLabelFormatter(value -> {
            int index = Math.round(value);
            if (index >= 0 && index < lucidityLevels.size()) {
                return lucidityLevels.get(index);
            }
            return "";
        });

        saveButton.setOnClickListener(v -> {
            boolean isSaved = saveDream();

            if (isSaved) {
                NavController navController = Navigation.findNavController(view);
                navController.navigateUp();
            }
        });

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Это все просто ради того, чтобы добавить подтверждение при нажатии "назад"

        requireActivity().addMenuProvider(new MenuProvider() {
            @Override
            public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {
            }

            @Override
            public boolean onMenuItemSelected(@NonNull MenuItem menuItem) {
                if (menuItem.getItemId() == android.R.id.home) {
                    showExitConfirmationDialog();
                    return true;
                }
                return false;
            }
        }, getViewLifecycleOwner(), Lifecycle.State.RESUMED);

        requireActivity().getOnBackPressedDispatcher().addCallback(
                getViewLifecycleOwner(),
                new OnBackPressedCallback(true) {
                    @Override
                    public void handleOnBackPressed() {
                        showExitConfirmationDialog();
                    }
                });

    }

    private void showSelectedTags() {
        chipGroupTags.removeAllViews();
        List<Tag> tags = selectedTags;
        for (Tag tag : tags) {
            Chip chip = new Chip(requireContext());
            chip.setText(tag.getName());
            chip.setTextStartPadding(10);
            chip.setTextEndPadding(10);

            chipGroupTags.addView(chip);
        }
    }

    private void showExitConfirmationDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Выйти без сохранения?")
                .setMessage("Все изменения будут утеряны")
                .setPositiveButton("Да", (dialog, which) -> {
                    NavController navController = Navigation.findNavController(requireView());
                    navController.popBackStack();
                })
                .setNegativeButton("Отмена", null)
                .show();
    }

    private void showDatePickerDialog() {
        LocalDate date = selectedDate;


        DatePickerDialog datePickerDialog = new DatePickerDialog(requireContext(),
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    selectedDate = LocalDate.of(selectedYear, selectedMonth + 1, selectedDay);
                    selectDateButton.setText(Dream.getFormattedDate(selectedDate));
                }, date.getYear(),
                date.getMonthValue() - 1,
                date.getDayOfMonth()
        );
        ZoneId zoneId = ZoneId.systemDefault();
        datePickerDialog.getDatePicker().setMaxDate(LocalDate.now().atStartOfDay(zoneId).toEpochSecond() * 1000);
        datePickerDialog.show();
    }

    private void openTagSelection() {
        tagViewModel.setSelectedTags(selectedTags);

        TagSelectionFragment bottomSheet = TagSelectionFragment.newInstance();
        bottomSheet.show(getChildFragmentManager(), "tag_selection");
    }


    private boolean saveDream() {
        String title = titleEditText.getText().toString();
        String content = contentEditText.getText().toString();
        int lucidity = (int) luciditySlider.getValue();

        if (title.isEmpty()) {
            titleEditText.setError("Введите название сна");
            return false;
        }

        if (selectedDate.isAfter(LocalDate.now())) {
            new AlertDialog.Builder(requireContext())
                    .setTitle("Указана неверная дата")
                    .setMessage("Пожалуйста, выберите дату не позднее сегодняшней")
                    .setPositiveButton("Ок", null)
                    .show();
            return false;
        }

        Dream dream = dreamViewModel.getDream().getValue();
        if (dream == null) {
            dream = new Dream();
        }

        dream.setName(title);
        dream.setDescription(content);
        dream.setTags(selectedTags);
        dream.setLucidityLevel(lucidity);
        dream.setDate(selectedDate);

        dreamViewModel.saveDream(dream);
        tagViewModel.setSelectedTags(new ArrayList<>());

        return true;
    }

    @Override
    public void onResume() {
        super.onResume();
        // Скрываем нижнюю панель
        BottomNavigationView bottomNavigationView = requireActivity().findViewById(R.id.bottom_navigation);
        if (bottomNavigationView != null) {
            bottomNavigationView.setVisibility(View.GONE);
        }

        // Получаем изменения в тегах при возвращении с TagSelection
        tagViewModel.getSelectedTags().observe(getViewLifecycleOwner(), tags -> {
            selectedTags = new ArrayList<>(tags);
            showSelectedTags();
        });

    }

    @Override
    public void onPause() {
        super.onPause();
        // Возвращаем нижнюю панель обратно, когда уходим с фрагмента
        BottomNavigationView bottomNavigationView = requireActivity().findViewById(R.id.bottom_navigation);
        if (bottomNavigationView != null) {
            bottomNavigationView.setVisibility(View.VISIBLE);
        }
    }

}
