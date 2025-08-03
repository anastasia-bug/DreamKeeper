package com.example.dreamkeeper;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.List;

public class DreamRepository {
    private static DreamRepository instance;
    private final DatabaseHelper dbHelper;
    private final MutableLiveData<List<Dream>> dreamsLiveData = new MutableLiveData<>();

    public DreamRepository() {
        dbHelper = DreamKeeperApplication.getDreamDatabaseHelper();
        loadDreams();
    }

    public static DreamRepository getInstance() {
        if (instance == null) {
            instance = new DreamRepository();
        }
        return instance;
    }

    private void loadDreams() {
        List<Dream> dreamList = dbHelper.getAllDreams();
        dreamsLiveData.setValue(dreamList);
    }

    public LiveData<List<Dream>> getAllDreams() {
        return dreamsLiveData;
    }

    public Dream getDreamById(int id) {
        return dbHelper.getDreamById(id);
    }

    public void saveDream(Dream dream) {
        dbHelper.saveDream(dream);
        loadDreams();
        TagRepository.getInstance().loadTags();
    }

    public void deleteDream(Dream dream) {
        dbHelper.deleteDream(dream.getId());
        loadDreams();
        TagRepository.getInstance().loadTags();
    }

}

