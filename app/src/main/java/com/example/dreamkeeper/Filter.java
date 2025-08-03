package com.example.dreamkeeper;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Filter {

    private String query;
    private List<Tag> selectedTags;
    private List<Integer> lucidityLevels;
    private LocalDate startDate;
    private LocalDate endDate;

    public Filter() {
        this.query = "";
        this.selectedTags = new ArrayList<>();
        this.lucidityLevels = new ArrayList<>();
        this.startDate = null;
        this.endDate = null;
    }

    public Filter(Filter cachedFilter) {
        this.query = cachedFilter.query;
        this.selectedTags = cachedFilter.selectedTags;
        this.lucidityLevels = cachedFilter.lucidityLevels;
        this.startDate = cachedFilter.startDate;
        this.endDate = cachedFilter.endDate;
    }

    public Filter(String query, List<Tag> selectedTags, List<Integer> lucidityLevels, LocalDate startDate, LocalDate endDate) {
        this.query = query != null ? query : "";
        this.selectedTags = selectedTags != null ? selectedTags : new ArrayList<>();
        this.lucidityLevels = lucidityLevels != null ? lucidityLevels : new ArrayList<>();
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public List<Tag> getSelectedTags() {
        return selectedTags;
    }

    public void setSelectedTags(List<Tag> selectedTags) {
        this.selectedTags = selectedTags;
    }

    public List<Integer> getLucidityLevels() {
        return lucidityLevels;
    }

    public void setLucidityLevels(List<Integer> lucidityLevels) {
        this.lucidityLevels = lucidityLevels;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public boolean hasActiveFilters() {
        return (query != null && !query.isEmpty()) ||
                (selectedTags != null && !selectedTags.isEmpty()) ||
                (lucidityLevels != null && !lucidityLevels.isEmpty()) ||
                startDate != null || endDate != null;
    }

    public boolean matchesLucidity(Dream dream) {
        return getLucidityLevels().isEmpty() ||
                getLucidityLevels().contains(dream.getLucidityLevel());
    }

    public boolean matchesDate(Dream dream) {
        LocalDate date = dream.getDate();
        if (getStartDate() != null && date.isBefore(getStartDate())) return false;
        if (getEndDate() != null && date.isAfter(getEndDate())) return false;
        return true;
    }

    public boolean matchesTags(Dream dream) {
        if (!getSelectedTags().isEmpty()) {
            boolean hasRequiredTag = false;
            for (Tag tag : dream.getTags()) {
                if (getSelectedTags().contains(tag)) {
                    hasRequiredTag = true;
                    break;
                }
            }
            if (!hasRequiredTag) return false;
        }
        return true;
    }

    public boolean matchesQuery(Dream dream) {
        String query = getQuery().toLowerCase();
        if (!query.isEmpty()) {
            if (!(dream.getName().toLowerCase().contains(query) ||
                    dream.getDescription().toLowerCase().contains(query))) {
                return false;
            }
        }
        return true;
    }

    public boolean matches(Dream dream) {

        return matchesLucidity(dream) &&
                matchesDate(dream) &&
                matchesTags(dream) &&
                matchesQuery(dream);
    }
}
