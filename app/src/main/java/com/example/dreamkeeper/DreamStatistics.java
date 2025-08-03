package com.example.dreamkeeper;

import java.util.List;
import java.util.Map;

public class DreamStatistics {
    private int totalDreams;
    private double lucidDreamsPercentage;
    private List<TagCount> topTags;
    private Map<Integer, Integer> lucidityLevelsCount;

    public DreamStatistics(int totalDreams, double lucidDreamsPercentage, List<TagCount> topTags, Map<Integer, Integer> lucidityLevelsCount) {
        this.totalDreams = totalDreams;
        this.lucidDreamsPercentage = lucidDreamsPercentage;
        this.topTags = topTags;
        this.lucidityLevelsCount = lucidityLevelsCount;
    }

    public int getTotalDreams() {
        return totalDreams;
    }

    public void setTotalDreams(int totalDreams) {
        this.totalDreams = totalDreams;
    }

    public double getLucidDreamsPercentage() {
        return lucidDreamsPercentage;
    }

    public void setLucidDreamsPercentage(double lucidDreamsPercentage) {
        this.lucidDreamsPercentage = lucidDreamsPercentage;
    }

    public List<TagCount> getTopTags() {
        return topTags;
    }

    public void setTopTags(List<TagCount> topTags) {
        this.topTags = topTags;
    }

    public Map<Integer, Integer> getLucidityLevelsCount() {
        return lucidityLevelsCount;
    }

    public void setLucidityLevelsCount(Map<Integer, Integer> lucidityLevelsCount) {
        this.lucidityLevelsCount = lucidityLevelsCount;
    }
}
