package com.example.dreamkeeper;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.time.LocalDate;
import java.util.ArrayList;

public class DreamViewModel extends ViewModel {

    private final DreamRepository dreamRepository;
    private final MutableLiveData<Dream> dream = new MutableLiveData<>();

    public DreamViewModel() {
        dreamRepository = DreamRepository.getInstance();
    }

    public LiveData<Dream> getDream() {
        return dream;
    }

    public void loadDream(int dreamId) {
        Dream loadedDream = dreamRepository.getDreamById(dreamId);
        dream.setValue(loadedDream);
    }

    public void saveDream(Dream dreamToSave) {
        dreamRepository.saveDream(dreamToSave);
        dream.setValue(dreamToSave);
        dream.setValue(null);
    }

    public void createNewDream() {
        Dream newDream = new Dream(
                "", "", 0, LocalDate.now(),  new ArrayList<>()
        );
        dream.setValue(newDream);
    }


    public void deleteDream(Dream dream) {
        dreamRepository.deleteDream(dream);
    }

}


