package com.example.dreamkeeper;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class TagRepository {
    private static TagRepository instance;
    private final DatabaseHelper dbHelper;
    private final MutableLiveData<List<Tag>> tagsLiveData = new MutableLiveData<>();
    private final MutableLiveData<List<TagCategory>> categoriesLiveData = new MutableLiveData<>();

    public static TagRepository getInstance() {
        if (instance == null) {
            instance = new TagRepository();
        }
        return instance;
    }

    private TagRepository() {
        dbHelper = DreamKeeperApplication.getDreamDatabaseHelper();
        loadTags();
        loadCategories();
    }

    public void loadTags() {
        List<Tag> tags = dbHelper.getAllTags();
        tagsLiveData.setValue(tags);
    }

    private void loadCategories() {
        List<TagCategory> categories = dbHelper.getAllCategories();
        categoriesLiveData.setValue(categories);
    }

    public LiveData<List<Tag>> getAllTags() {
        return tagsLiveData;
    }

    public LiveData<List<TagCategory>> getAllCategories() {
        return categoriesLiveData;
    }

    public int addTag(Tag tag) {
        Integer id = dbHelper.saveTag(tag);
        tag.setId(id);
        loadTags();

        return id;
    }

    public void updateTag(Tag tag) {
        dbHelper.saveTag(tag);
        loadTags();
    }

    public void deleteTag(Tag tag) {
        dbHelper.deleteTag(tag.getId());
        loadTags();
    }

    public Map<Integer, Integer> getDreamCountPerTag() {
        return dbHelper.getDreamCountPerTag();
    }

    public int getTotalDreamCount() {
        return dbHelper.getTotalDreamCount();
    }

    public String getCategoryNameById(int categoryId) {
        return dbHelper.getCategoryNameById(categoryId);
    }

    public List<TagDisplayData> getAllTagDisplayData() {
        List<TagDisplayData> result = new ArrayList<>();

        List<Tag> allTags = dbHelper.getAllTags();  // получаем все теги
        Map<Integer, Integer> dreamCounts = dbHelper.getDreamCountPerTag();  // мапа: tagId -> кол-во снов
        int totalDreams = dbHelper.getTotalDreamCount();  // общее кол-во снов

        for (Tag tag : allTags) {
            int count = dreamCounts.getOrDefault(tag.getId(), 0);
            String categoryName = dbHelper.getCategoryNameById(tag.getCategoryId());
            result.add(new TagDisplayData(tag, count, totalDreams, categoryName));
        }

        return result;
    }

}

