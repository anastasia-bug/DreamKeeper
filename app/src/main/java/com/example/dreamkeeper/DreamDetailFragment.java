package com.example.dreamkeeper;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.view.MenuProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.List;

public class DreamDetailFragment extends Fragment {

    private TextView titleTextView, dateTextView, contentTextView, lucidityTextView;
    private ChipGroup chipGroupTags;
    private DreamViewModel dreamViewModel;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dream_detail, container, false);
        titleTextView = view.findViewById(R.id.textViewTitle);
        dateTextView = view.findViewById(R.id.textViewDate);
        contentTextView = view.findViewById(R.id.textViewContent);
        chipGroupTags = view.findViewById(R.id.chipGroupTags);
        lucidityTextView = view.findViewById(R.id.textViewLucidity);

        dreamViewModel = new ViewModelProvider(requireActivity()).get(DreamViewModel.class);


        if (getArguments() != null) {
            int dreamId = getArguments().getInt("dreamId");
            dreamViewModel.loadDream(dreamId);
        }

        observeViewModel();

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        requireActivity().addMenuProvider(new MenuProvider() {
            @Override
            public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {
                menuInflater.inflate(R.menu.menu_dream_detail, menu);
            }

            @Override
            public boolean onMenuItemSelected(@NonNull MenuItem menuItem) {
                if (menuItem.getItemId() == R.id.action_edit) {
                    editDream();
                    return true;
                } else if (menuItem.getItemId() == R.id.action_delete) {
                    confirmDelete();
                    return true;
                }
                return false;
            }
        }, getViewLifecycleOwner());

    }

    private void observeViewModel() {
        dreamViewModel.getDream().observe(getViewLifecycleOwner(), dream -> {
            if (dream != null) {
                titleTextView.setText(dream.getName());
                dateTextView.setText(dream.getFormattedDate());
                contentTextView.setText(dream.getDescription());
                lucidityTextView.setText(dream.getLucidityLevelName(requireContext()));

                // Добавляем теги
                chipGroupTags.removeAllViews();
                List<Tag> tags = dream.getTags();
                for (Tag tag : tags) {
                    Chip chip = new Chip(requireContext());
                    chip.setText(tag.getName());
                    chip.setTextStartPadding(10);
                    chip.setTextEndPadding(10);

                    chipGroupTags.addView(chip);
                }
            }
        });
    }

    private void editDream() {
        Bundle bundle = new Bundle();
        bundle.putBoolean("isEditing", true);

        NavController navController = Navigation.findNavController(requireView());
        navController.navigate(R.id.navigation_add_dream, bundle);
    }

    private void confirmDelete() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Удаление записи")
                .setMessage("Вы уверены, что хотите удалить этот сон?")
                .setPositiveButton("Удалить", (dialog, which) -> deleteDream())
                .setNegativeButton("Отмена", null)
                .show();
    }

    private void deleteDream() {
        Dream dream = dreamViewModel.getDream().getValue();
        if (dream != null) {
            dreamViewModel.deleteDream(dream);
            Navigation.findNavController(requireView()).popBackStack();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        // Скрываем нижнюю панель
        BottomNavigationView bottomNavigationView = requireActivity().findViewById(R.id.bottom_navigation);
        if (bottomNavigationView != null) {
            bottomNavigationView.setVisibility(View.GONE);
        }
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
