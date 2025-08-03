package com.example.dreamkeeper;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class StatisticsViewModel extends ViewModel implements FilterableViewModel {

    private final DreamRepository dreamRepository;
    private final TagRepository tagRepository;
    private final MutableLiveData<Filter> filter = new MutableLiveData<>(new Filter());
    private final MediatorLiveData<List<Dream>> statDreams = new MediatorLiveData<>();
    private final MediatorLiveData<DreamStatistics> statistics = new MediatorLiveData<>();

    public LiveData<List<Tag>> allTags;
    public LiveData<List<TagCategory>> categories;

    @Override
    public LiveData<List<Tag>> getAllTags() {
        return allTags;
    }

    @Override
    public LiveData<List<TagCategory>> getCategories() {
        return categories;
    }

    public StatisticsViewModel() {
        dreamRepository = DreamRepository.getInstance();
        tagRepository = TagRepository.getInstance();

        LiveData<List<Dream>> allDreams = dreamRepository.getAllDreams();

        allTags = tagRepository.getAllTags();
        categories = tagRepository.getAllCategories();

        statDreams.addSource(allDreams, dreams -> applyFilter(dreams, filter.getValue()));
        statDreams.addSource(filter, filter -> applyFilter(allDreams.getValue(), filter));
        statDreams.addSource(allTags, tags -> applyFilter(allDreams.getValue(), filter.getValue()));

        statistics.addSource(statDreams, dreams -> updateStatistics(dreams));

    }

    private void updateStatistics(List<Dream> dreams) {
        if (dreams == null || dreams.isEmpty()) {
            statistics.setValue(null);
        } else {
            statistics.setValue(calculateStatistics(dreams));
        }
    }


    public LiveData<DreamStatistics> getStatistics() {
        return statistics;
    }

    public boolean hasActiveFilters() {
        return filter != null && filter.getValue().hasActiveFilters();
    }

    public LiveData<Filter> getFilter() {
        return filter;
    }

    public void updateFilter(Function<Filter, Filter> updater) {
        Filter current = new Filter(filter.getValue());
        filter.setValue(updater.apply(current));
    }

    private void applyFilter(List<Dream> dreams, Filter filter) {

        if (dreams == null || filter == null) {
            statDreams.setValue(new ArrayList<>());
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

        statDreams.setValue(result);
    }

    public DreamStatistics calculateStatistics(List<Dream> dreams) {

        int total = dreams.size();
        int lucidCount = (int) dreams.stream().filter(Dream::isLucid).count();

        Map<Tag, Long> tagFrequency = dreams.stream()
                .flatMap(dream -> dream.getTags().stream())
                .collect(Collectors.groupingBy(tag -> tag, Collectors.counting()));

        List<TagCount> topTags = tagFrequency.entrySet().stream()
                .sorted((a, b) -> Long.compare(b.getValue(), a.getValue()))
                .limit(20)
                .map(entry -> new TagCount(entry.getKey(), entry.getValue().intValue()))
                .collect(Collectors.toList());

        // Количество снов для каждого уровня осознанности (pieChart)
        Map<Integer, Integer> levels = new HashMap<>();
        for (Dream dream : dreams) {
            int level = dream.getLucidityLevel();
            levels.put(level, levels.getOrDefault(level, 0) + 1);
        }

        return new DreamStatistics(total, lucidCount * 100.0 / Math.max(1, total), topTags, levels);
    }

}
