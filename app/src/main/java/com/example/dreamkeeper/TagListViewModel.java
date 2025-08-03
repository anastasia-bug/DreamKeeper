package com.example.dreamkeeper;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;
import java.util.List;

public class TagListViewModel extends ViewModel {

    private final TagRepository repository;
    public LiveData<List<Tag>> allTags;
    public LiveData<List<TagCategory>> categories;

    private final MediatorLiveData<List<TagDisplayData>> allTagDisplayData = new MediatorLiveData<>();

    public TagListViewModel() {
        repository = TagRepository.getInstance();
        allTags = repository.getAllTags();
        categories = repository.getAllCategories();

        allTagDisplayData.addSource(allTags, tags -> loadAllTagDisplayData());
        allTagDisplayData.addSource(categories, c -> loadAllTagDisplayData());
    }


    public LiveData<List<Tag>> getAllTags() {
        return allTags;
    }

    public LiveData<List<TagCategory>> getCategories() {
        return categories;
    }

    public void addNewTag(Tag newTag) {
        repository.addTag(newTag);
    }

    public LiveData<List<TagDisplayData>> getAllTagDisplayData() {
        if (allTagDisplayData.getValue() == null) {
            loadAllTagDisplayData();
        }
        return allTagDisplayData;
    }

    public void loadAllTagDisplayData() {
        List<TagDisplayData> data = repository.getAllTagDisplayData();
        allTagDisplayData.setValue(data);
    }

    public int addTag(Tag tag) {
        return repository.addTag(tag);
    }

    public void updateTag(Tag tag) {
        repository.updateTag(tag);
    }

    public void deleteTag(Tag tag) {
        repository.deleteTag(tag);
    }
}
