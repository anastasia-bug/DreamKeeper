package com.example.dreamkeeper;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;
import java.util.List;

public class TagSelectionViewModel extends ViewModel {

    private final TagRepository repository;
    public LiveData<List<Tag>> allTags;
    public LiveData<List<TagCategory>> categories;
    private final MutableLiveData<List<Tag>> selectedTags = new MutableLiveData<>(new ArrayList<>());

    public TagSelectionViewModel() {
        repository = TagRepository.getInstance();
        allTags = repository.getAllTags();
        categories = repository.getAllCategories();
    }

    public LiveData<List<Tag>> getAllTags() {
        return allTags;
    }

    public LiveData<List<Tag>> getSelectedTags() {
        return selectedTags;
    }

    public void setSelectedTags(List<Tag> tags) {
        selectedTags.setValue(tags);
    }

    public LiveData<List<TagCategory>> getCategories() {
        return categories;
    }

    public void toggleTag(Tag tag) {
        List<Tag> current = new ArrayList<>(selectedTags.getValue());
        if (current.contains(tag)) {
            current.remove(tag);
        } else {
            current.add(tag);
        }
        selectedTags.setValue(current);
    }

    public void addNewTag(Tag newTag) {
        repository.addTag(newTag);

        List<Tag> newSelectedTags = new ArrayList<>(selectedTags.getValue());
        newSelectedTags.add(newTag);
        selectedTags.setValue(newSelectedTags);
    }
}


