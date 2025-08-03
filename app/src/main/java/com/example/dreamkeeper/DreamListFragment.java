package com.example.dreamkeeper;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.OptIn;
import androidx.core.content.ContextCompat;
import androidx.core.view.MenuProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.badge.BadgeUtils;
import com.google.android.material.badge.ExperimentalBadgeUtils;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;

public class DreamListFragment extends Fragment {

    private DreamListViewModel viewModel;
    private RecyclerView dreamsRecyclerView;
    private DreamAdapter dreamAdapter;
    private LinearLayout emptyDatabaseLayout;
    private TextView emptyTextView;
    MenuItem filterItem;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dream_list, container, false);

        emptyDatabaseLayout = view.findViewById(R.id.emptyStateLayout);
        emptyTextView = view.findViewById(R.id.textViewEmpty);

        dreamsRecyclerView = view.findViewById(R.id.recyclerViewDreams);
        dreamsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        viewModel = new ViewModelProvider(requireActivity()).get(DreamListViewModel.class);

        dreamAdapter = new DreamAdapter(new ArrayList<>(), dream -> {
            Bundle bundle = new Bundle();
            bundle.putInt("dreamId", dream.getId());
            NavController navController = Navigation.findNavController(getView());
            navController.navigate(R.id.navigation_dream_detail, bundle);
        });

        dreamsRecyclerView.setAdapter(dreamAdapter);

        FloatingActionButton fab = view.findViewById(R.id.fab_add_dream);
        fab.setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            bundle.putBoolean("isEditing", false);
            NavController navController = Navigation.findNavController(requireView());
            navController.navigate(R.id.navigation_add_dream, bundle);
        });

        viewModel.getFilteredDreams().observe(getViewLifecycleOwner(), dreams -> {
            dreamAdapter.updateDreams(dreams);

            if (dreams == null || dreams.isEmpty()) {
                emptyDatabaseLayout.setVisibility(View.VISIBLE);
                dreamsRecyclerView.setVisibility(View.GONE);
                if (viewModel.hasActiveFilters()) {
                    emptyTextView.setText("Не найдено снов с установленными фильтрами");
                } else {
                    emptyTextView.setText("Добро пожаловать! \nПришло время добавить свой первый сон");
                }
            } else {
                emptyDatabaseLayout.setVisibility(View.GONE);
                dreamsRecyclerView.setVisibility(View.VISIBLE);
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
        FilterFragment bottomSheet = FilterFragment.newInstance(FilterSource.DREAMS);
        bottomSheet.show(getChildFragmentManager(), "filter_selection");
    }

}

