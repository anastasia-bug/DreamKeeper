package com.example.dreamkeeper;

import androidx.lifecycle.LiveData;

import java.util.List;
import java.util.function.Function;

public interface FilterableViewModel {
    LiveData<Filter> getFilter();
    void updateFilter(Function<Filter, Filter> updater);
    LiveData<List<Tag>> getAllTags();
    LiveData<List<TagCategory>> getCategories();
}
