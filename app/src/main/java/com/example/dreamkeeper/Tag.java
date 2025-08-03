package com.example.dreamkeeper;

import java.util.Objects;

public class Tag {
    private Integer id;
    private String name;
    private String description;
    private int categoryId;

    public Tag(Integer id, String name, String description, int categoryId) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.categoryId = categoryId;
    }

    public Tag(String name, String description, int categoryId) {
        this.name = name;
        this.description = description;
        this.categoryId = categoryId;
    }

    public Tag() {
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

    public int getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(int categoryId) {
        this.categoryId = categoryId;
    }

    // Нужно переопределять equals и hashCode, чтобы корректно сравнивались объекты (если это два разных экземпляра одного сна, например)
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Tag)) return false;
        Tag tag = (Tag) o;

        // Если оба id заданы, то сравниваем по id
        if (id != null && tag.id != null) {
            return id.equals(tag.id);
        }

        // Если нет, то по name
        return Objects.equals(name, tag.name);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : name != null ? name.hashCode() : 0;
    }


}
