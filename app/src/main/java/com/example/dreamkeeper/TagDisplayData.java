package com.example.dreamkeeper;

public class TagDisplayData {
    private final Tag tag;
    private final int dreamCount;
    private final int totalCount;
    private final String categoryName;

    public TagDisplayData(Tag tag, int dreamCount, int totalCount, String categoryName) {
        this.tag = tag;
        this.dreamCount = dreamCount;
        this.totalCount = totalCount;
        this.categoryName = categoryName;
    }

    public Tag getTag() {
        return tag;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public int getDreamCount() {
        return dreamCount;
    }

    public String getPercentageText() {
        if (totalCount == 0) return "0%";
        int percent = (int) Math.round(dreamCount * 100.0 / totalCount);
        return percent + "%";
    }
}

