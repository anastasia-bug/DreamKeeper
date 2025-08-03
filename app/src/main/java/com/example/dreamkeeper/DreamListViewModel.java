package com.example.dreamkeeper;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class DreamListViewModel extends ViewModel implements FilterableViewModel {

    private final DreamRepository dreamRepository;
    private final MutableLiveData<Filter> filter = new MutableLiveData<>(new Filter());
    private final MediatorLiveData<List<Dream>> filteredDreams = new MediatorLiveData<>();

    private final TagRepository tagRepository;
    public LiveData<List<Tag>> allTags;
    public LiveData<List<TagCategory>> categories;

    public DreamListViewModel() {
        dreamRepository = DreamRepository.getInstance();
        tagRepository = TagRepository.getInstance();

        LiveData<List<Dream>> allDreams = dreamRepository.getAllDreams();

        allTags = tagRepository.getAllTags();
        categories = tagRepository.getAllCategories();

        filteredDreams.addSource(allDreams, dreams -> applyFilter(dreams, filter.getValue()));

        filteredDreams.addSource(filter, filter -> applyFilter(allDreams.getValue(), filter));

        filteredDreams.addSource(allTags, tags -> applyFilter(allDreams.getValue(), filter.getValue()));

    }

    @Override
    public LiveData<List<Tag>> getAllTags() {
        return allTags;
    }

    @Override
    public LiveData<List<TagCategory>> getCategories() {
        return categories;
    }

    public LiveData<List<Dream>> getFilteredDreams() {
        return filteredDreams;
    }

    public boolean hasActiveFilters() {
        return filter != null && filter.getValue().hasActiveFilters();
    }

    public LiveData<Filter> getFilter() {
        return filter;
    }

    public void updateFilter(Function<Filter, Filter> updater) {
        Filter current = new Filter(filter.getValue());
        if (current != null) {
            filter.setValue(updater.apply(current));
        }
    }

    private void applyFilter(List<Dream> dreams, Filter filter) {

        if (dreams == null || filter == null) {
            filteredDreams.setValue(new ArrayList<>());
            return;
        }

        List<Tag> currentTags = allTags.getValue();
        List<Tag> filterTags = new ArrayList<>(filter.getSelectedTags());
        if (currentTags != null) {
            filterTags.retainAll(currentTags);
        }
        filter.setSelectedTags(filterTags);

        List<Dream> result = new ArrayList<>();
        for (Dream dream : dreams) {
            if (filter.matches(dream)) {
                result.add(dream);
            }
        }

        filteredDreams.setValue(result);
    }

}
