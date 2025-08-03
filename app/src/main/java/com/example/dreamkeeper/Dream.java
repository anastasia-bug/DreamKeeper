package com.example.dreamkeeper;

import android.content.Context;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class Dream {
    private Integer id;
    private String name;
    private String description;
    private int lucidityLevel; // 0-4
    private LocalDate date;
    private List<Tag> tags;
    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG).withLocale(Locale.getDefault());

    public Dream() {}

    public Dream(int id, String name, String description, int lucidityLevel, LocalDate date, List<Tag> tags) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.lucidityLevel = lucidityLevel;
        this.date = date;
        this.tags = tags;
    }

    public Dream(String name, String description, int lucidityLevel, LocalDate date, List<Tag> tags) {
        this.name = name;
        this.description = description;
        this.lucidityLevel = lucidityLevel;
        this.date = date;
        this.tags = tags;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getLucidityLevel() {
        return lucidityLevel;
    }

    public void setLucidityLevel(int lucidityLevel) {
        this.lucidityLevel = lucidityLevel;
    }

    public boolean isLucid() {
        return lucidityLevel > 0;
    }

    public static String getLucidityLevelName(int level, Context context) {
        String[] levels = context.getResources().getStringArray(R.array.lucidity_levels);
        return levels[level];
    }

    public String getLucidityLevelName(Context context) {
        return getLucidityLevelName(lucidityLevel, context);
    }

    public static List<String> getAllLucidityLevels(Context context) {
        List<String> lucidityLevels = new ArrayList<>();

        for (int i = 0; i < 5; i++) {
            lucidityLevels.add(getLucidityLevelName(i, context));
        }

        return lucidityLevels;
    }

    public String getFormattedDate() {
        return date == null ? "" : date.format(FORMATTER);
    }

    public static String getFormattedDate(LocalDate date) {
        return date == null ? "" : date.format(FORMATTER);
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public void setDate(String date) {
        this.date = LocalDate.parse(date);
    }

    public List<Tag> getTags() {
        return tags;
    }

    public void setTags(List<Tag> tags) {
        this.tags = tags;
    }

}
