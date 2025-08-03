package com.example.dreamkeeper;

import android.database.sqlite.SQLiteConstraintException;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.OptIn;
import androidx.appcompat.app.AlertDialog;
import androidx.core.view.MenuProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.badge.ExperimentalBadgeUtils;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class TagListFragment extends Fragment {

    private TagListViewModel viewModel;
    private RecyclerView tagsRecyclerView;
    private TagAdapter tagAdapter;
    private LinearLayout emptyTagsLayout;
    private TextView emptyTextView;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_tag_list, container, false);

        emptyTagsLayout = view.findViewById(R.id.emptyStateLayout);
        emptyTextView = view.findViewById(R.id.textViewEmpty);

        tagsRecyclerView = view.findViewById(R.id.recyclerViewTags);
        tagsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        viewModel = new ViewModelProvider(requireActivity()).get(TagListViewModel.class);

        tagAdapter = new TagAdapter(new ArrayList<>(), tag -> {
            showEditTagDialog(tag);
        });

        tagsRecyclerView.setAdapter(tagAdapter);

        FloatingActionButton fab = view.findViewById(R.id.fab_add_tag);
        fab.setOnClickListener(v -> {
            showEditTagDialog(null);
        });

        viewModel.getAllTagDisplayData().observe(getViewLifecycleOwner(), displayData -> {
            tagAdapter.updateTags(displayData);
            if (displayData == null || displayData.isEmpty()) {
                emptyTagsLayout.setVisibility(View.VISIBLE);
                tagsRecyclerView.setVisibility(View.GONE);
                emptyTextView.setText("Не добавлено ни одного тега");
            } else {
                emptyTagsLayout.setVisibility(View.GONE);
                tagsRecyclerView.setVisibility(View.VISIBLE);
            }
        });

        return view;
    }

    private void showEditTagDialog(@Nullable Tag tagToEdit) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_edit_tag, null);
        EditText nameInput = dialogView.findViewById(R.id.editTextTagName);
        Spinner categorySpinner = dialogView.findViewById(R.id.spinnerCategory);

        List<TagCategory> categories = viewModel.getCategories().getValue();
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item,
                categories.stream().map(TagCategory::getName).collect(Collectors.toList()));
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categorySpinner.setAdapter(adapter);

        if (tagToEdit != null) {
            nameInput.setText(tagToEdit.getName());
            int index = getCategoryIndex(categories, tagToEdit.getCategoryId());
            categorySpinner.setSelection(index);
        }

        builder.setView(dialogView);
        builder.setTitle(tagToEdit == null ? "Добавить тег" : "Редактировать тег");
        builder.setPositiveButton("Сохранить", null);
        if (tagToEdit != null) {
            builder.setNeutralButton("Удалить", (dialog, which) -> {
                new AlertDialog.Builder(requireContext())
                        .setTitle("Удалить тег?")
                        .setMessage("Вы уверены, что хотите удалить этот тег? Он также будет удален из всех снов, где он встречается.")
                        .setPositiveButton("Да", (confirmDialog, w) -> viewModel.deleteTag(tagToEdit))
                        .setNegativeButton("Отмена", null)
                        .show();
            });
        }
        builder.setNegativeButton("Отмена", null);
        AlertDialog dialog = builder.create();
        dialog.show();

        // Отдельно добавляем слушателя для кнопки "Сохранить", чтобы окно не закрывалось при ошибке
        Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
        positiveButton.setOnClickListener(v -> {
            String tagName = nameInput.getText().toString().trim();
            int selectedIndex = categorySpinner.getSelectedItemPosition();

            if (tagName.isEmpty()) {
                nameInput.setError("Введите имя тега");
                return;
            }

            TagCategory selectedCategory = categories.get(selectedIndex);
            Tag newTag = new Tag(tagToEdit != null ? tagToEdit.getId() : null, tagName, "", selectedCategory.getId());

            if (tagToEdit == null) {
                int id = viewModel.addTag(newTag);
                if (id == -1) {
                    nameInput.setError("Тег с таким именем уже существует");
                } else {
                    dialog.dismiss(); // Закрываем диалог, если нет ошибки при добавлении
                }
            } else {
                viewModel.updateTag(newTag);
                dialog.dismiss();
            }
        });
    }

    private int getCategoryIndex(List<TagCategory> categories, int categoryId) {
        for (int i = 0; i < categories.size(); i++) {
            if (categories.get(i).getId() == categoryId) return i;
        }
        return 0;
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