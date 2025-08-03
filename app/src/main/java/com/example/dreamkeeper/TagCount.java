package com.example.dreamkeeper;

public class TagCount {
    private final Tag tag;
    private final int count;

    public TagCount(Tag tag, int count) {
        this.tag = tag;
        this.count = count;
    }

    public Tag getTag() {
        return tag;
    }

    public int getCount() {
        return count;
    }
}

