package com.example.dreamkeeper;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "dreams.db";
    private static final int DB_VERSION = 13;

    // Таблица категорий тегов
    public static final String TABLE_CATEGORIES = "tag_categories";
    public static final String COLUMN_CATEGORY_ID = "id";
    public static final String COLUMN_CATEGORY_NAME = "name";

    // Таблица тегов
    public static final String TABLE_TAGS = "tags";
    public static final String COLUMN_TAG_ID = "id";
    public static final String COLUMN_TAG_NAME = "name";
    public static final String COLUMN_TAG_DESCRIPTION = "description";
    public static final String COLUMN_TAG_CATEGORY_ID = "category_id";
//    public static final String COLUMN_TAG_EMOJI = "emoji";

    // Таблица снов

    public static final String TABLE_DREAMS = "dreams";
    public static final String COLUMN_DREAM_ID = "id";
    public static final String COLUMN_DREAM_NAME = "name";
    public static final String COLUMN_DREAM_DESCRIPTION = "description";
    public static final String COLUMN_DREAM_LUCIDITY = "lucidity_level";
    public static final String COLUMN_DREAM_DATE = "date";

    // Связующая таблица (сны-теги)
    public static final String TABLE_DREAM_TAG_LINK = "dream_tag_link";
    public static final String COLUMN_DREAM_ID_FK = "dream_id";
    public static final String COLUMN_TAG_ID_FK = "tag_id";

    private final Context context;

    public DatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
        this.context = context;
    }


    @Override
    public void onCreate(SQLiteDatabase db) {

        // Таблица категорий тегов
        db.execSQL("CREATE TABLE " + TABLE_CATEGORIES + " ("
                + COLUMN_CATEGORY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COLUMN_CATEGORY_NAME + " TEXT NOT NULL)");

        // Добавление категорий по умолчанию
        String[] categories = {"Места", "Персонажи", "Действия", "Настроение", "Прочее"};
        ContentValues values = new ContentValues();
        for (String category : categories) {
            values.clear();
            values.put(COLUMN_CATEGORY_NAME, category);
            db.insert(TABLE_CATEGORIES, null, values);
        }

        // Таблица тегов
        db.execSQL("CREATE TABLE " + TABLE_TAGS + " ("
                + COLUMN_TAG_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COLUMN_TAG_NAME + " TEXT NOT NULL UNIQUE, "
                + COLUMN_TAG_DESCRIPTION + " TEXT, "
                + COLUMN_TAG_CATEGORY_ID + " INTEGER NOT NULL, "
//                + COLUMN_TAG_EMOJI + " TEXT, "
                + "FOREIGN KEY(" + COLUMN_TAG_CATEGORY_ID + ") REFERENCES "
                + TABLE_CATEGORIES + "(" + COLUMN_CATEGORY_ID + "))");

        // Таблица снов
        db.execSQL("CREATE TABLE " + TABLE_DREAMS + " ("
                + COLUMN_DREAM_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COLUMN_DREAM_NAME + " TEXT NOT NULL, "
                + COLUMN_DREAM_DESCRIPTION + " TEXT, "
                + COLUMN_DREAM_LUCIDITY + " INTEGER NOT NULL CHECK(" + COLUMN_DREAM_LUCIDITY + " BETWEEN 0 AND 4), "
                + COLUMN_DREAM_DATE + " TEXT NOT NULL)");

        // Связующая таблица сны-теги
        db.execSQL("CREATE TABLE " + TABLE_DREAM_TAG_LINK + " ("
                + COLUMN_DREAM_ID_FK + " INTEGER NOT NULL, "
                + COLUMN_TAG_ID_FK + " INTEGER NOT NULL, "
                + "PRIMARY KEY (" + COLUMN_DREAM_ID_FK + ", " + COLUMN_TAG_ID_FK + "), "
                + "FOREIGN KEY(" + COLUMN_DREAM_ID_FK + ") REFERENCES "
                + TABLE_DREAMS + "(" + COLUMN_DREAM_ID + "), "
                + "FOREIGN KEY(" + COLUMN_TAG_ID_FK + ") REFERENCES "
                + TABLE_TAGS + "(" + COLUMN_TAG_ID + "))");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_DREAM_TAG_LINK);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_DREAMS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TAGS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CATEGORIES);
        onCreate(db);
    }

    public void saveDream(Dream dream) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_DREAM_NAME, dream.getName());
        values.put(COLUMN_DREAM_DESCRIPTION, dream.getDescription());
        values.put(COLUMN_DREAM_LUCIDITY, dream.getLucidityLevel());
        values.put(COLUMN_DREAM_DATE, dream.getDate().toString());

        Integer dreamId = dream.getId();

        if (dreamId == null) {
            // Новый сон
            dreamId = (int) db.insert(TABLE_DREAMS, null, values);
        } else {
            // Обновление существующего сна
            db.update(TABLE_DREAMS, values, COLUMN_DREAM_ID + " = ?", new String[]{String.valueOf(dreamId)});
        }

        // Удаление всех выбранных тегов для данного сна
        db.delete(TABLE_DREAM_TAG_LINK, COLUMN_DREAM_ID_FK + " = ?", new String[]{String.valueOf(dreamId)});

        // Сохранение тегов
        for (Tag tag : dream.getTags()) {
            Integer tagId = saveTag(tag);
            saveDreamTagRelation(dreamId, tagId);
        }
    }

    public void deleteDream(int dreamId) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_DREAMS, COLUMN_DREAM_ID + " = ?", new String[]{String.valueOf(dreamId)});
        db.delete(TABLE_DREAM_TAG_LINK, COLUMN_DREAM_ID_FK + " = ?", new String[]{String.valueOf(dreamId)});
    }

    public Integer saveTag(Tag tag) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_TAG_NAME, tag.getName());
        values.put(COLUMN_TAG_DESCRIPTION, tag.getDescription());
        values.put(COLUMN_TAG_CATEGORY_ID, tag.getCategoryId());

        Integer tagId = tag.getId();

        if (tagId == null) {
            tagId = (int) db.insert(TABLE_TAGS, null, values);
        } else {
            db.update(TABLE_TAGS, values, COLUMN_TAG_ID + " = ?", new String[]{String.valueOf(tagId)});
        }
        return tagId;
    }

    public void deleteTag(int dreamId) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_TAGS, COLUMN_TAG_ID + " = ?", new String[]{String.valueOf(dreamId)});
        db.delete(TABLE_DREAM_TAG_LINK, COLUMN_TAG_ID_FK + " = ?", new String[]{String.valueOf(dreamId)});
    }

    private void saveDreamTagRelation(long dreamId, long tagId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_DREAM_ID_FK, dreamId);
        values.put(COLUMN_TAG_ID_FK, tagId);
        // Так как в таблие первичный ключ (dream_id, tag_id), то при попытке добавить уже существующие в таблице значения возникнет ошибка, дубликатов не будет
        db.insertWithOnConflict("dream_tag_link", null, values, SQLiteDatabase.CONFLICT_IGNORE);
    }

    public List<Dream> getAllDreams() {
        List<Dream> dreamList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM dreams ORDER BY date DESC", null);
        if (cursor != null && cursor.moveToFirst()) {
            do {
                Dream dream = new Dream();
                dream.setId(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_DREAM_ID)));
                dream.setName(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DREAM_NAME)));
                dream.setDescription(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DREAM_DESCRIPTION)));
                dream.setLucidityLevel(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_DREAM_LUCIDITY)));
                dream.setDate(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DREAM_DATE)));
                dreamList.add(dream);
                // Загрузка тегов для сна
                List<Tag> tags = getTagsByDreamId(dream.getId());
                dream.setTags(tags);
            } while (cursor.moveToNext());
        }
        cursor.close();

        return dreamList;
    }

    private List<Tag> createTagListFromCursor(Cursor cursor) {
        List<Tag> tags = new ArrayList<>();
        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_TAG_ID));
                String name = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TAG_NAME));
                String description = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TAG_DESCRIPTION));
                int categoryId = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_TAG_CATEGORY_ID));
//                String emoji = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TAG_EMOJI));
                tags.add(new Tag(id, name, description, categoryId));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return tags;
    }

    public List<Tag> getAllTags() {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM " + TABLE_TAGS +
                " ORDER BY " + COLUMN_TAG_CATEGORY_ID + ", " + COLUMN_TAG_NAME;
        Cursor cursor = db.rawQuery(query, null);

        return createTagListFromCursor(cursor);
    }


    public List<TagCategory> getAllCategories() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_CATEGORIES, null);

        List<TagCategory> categories = new ArrayList<>();
        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_CATEGORY_ID));
                String name = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CATEGORY_NAME));
                categories.add(new TagCategory(id, name));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return categories;
    }

    private List<Tag> getTagsByDreamId(int dreamId) {
        SQLiteDatabase db = this.getReadableDatabase();

        String query = "SELECT t." + COLUMN_TAG_ID + ", t." + COLUMN_TAG_NAME + ", t." + COLUMN_TAG_DESCRIPTION + ", " +
                "t." + COLUMN_TAG_CATEGORY_ID + /*", t." + COLUMN_TAG_EMOJI +*/
                " FROM " + TABLE_TAGS + " t " +
                "INNER JOIN " + TABLE_DREAM_TAG_LINK + " dt ON t." + COLUMN_TAG_ID + " = dt." + COLUMN_TAG_ID_FK +
                " WHERE dt." + COLUMN_DREAM_ID_FK + " = ?";
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(dreamId)});

        return createTagListFromCursor(cursor);
    }

    public Dream getDreamById(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_DREAMS, null, COLUMN_DREAM_ID + " = ?", new String[]{String.valueOf(id)}, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            Dream dream = new Dream();
            dream.setId(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_DREAM_ID)));
            dream.setName(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DREAM_NAME)));
            dream.setDescription(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DREAM_DESCRIPTION)));
            dream.setLucidityLevel(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_DREAM_LUCIDITY)));
            dream.setDate(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DREAM_DATE)));

            // Загрузка тегов
            List<Tag> tags = getTagsByDreamId(dream.getId());
            dream.setTags(tags);

            cursor.close();
            return dream;
        } else {
            return null;
        }
    }

    public List<Dream> getDreamsByDate(String date) {
        List<Dream> dreamList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_DREAMS, null, COLUMN_DREAM_DATE + " = ?", new String[]{date}, null, null, null);
        if (cursor.moveToFirst()) {
            do {
                Dream dream = new Dream();
                dream.setId(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_DREAM_ID)));
                dream.setName(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DREAM_NAME)));
                dream.setDescription(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DREAM_DESCRIPTION)));
                dream.setLucidityLevel(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_DREAM_LUCIDITY)));
                dream.setDate(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DREAM_DATE)));

                // Загрузка тегов для сна
                List<Tag> tags = getTagsByDreamId(dream.getId());
                dream.setTags(tags);

                dreamList.add(dream);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return dreamList;
    }

    public Map<Integer, Integer> getDreamCountPerTag() {
        SQLiteDatabase db = this.getReadableDatabase();
        Map<Integer, Integer> tagCounts = new HashMap<>();

        String query = "SELECT " + COLUMN_TAG_ID_FK + ", COUNT(*) AS count FROM " + TABLE_DREAM_TAG_LINK +
                " GROUP BY " + COLUMN_TAG_ID_FK;

        Cursor cursor = db.rawQuery(query, null);

        if (cursor.moveToFirst()) {
            do {
                int tagId = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_TAG_ID_FK));
                int count = cursor.getInt(cursor.getColumnIndexOrThrow("count"));
                tagCounts.put(tagId, count);
            } while (cursor.moveToNext());
        }

        cursor.close();
        return tagCounts;
    }


    public int getTotalDreamCount() {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT COUNT(*) FROM " + TABLE_DREAMS;

        Cursor cursor = db.rawQuery(query, null);
        int count = 0;

        if (cursor.moveToFirst()) {
            count = cursor.getInt(0);
        }

        cursor.close();
        return count;
    }

    public int getTotalDreams(String fromDate, String toDate) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT COUNT(*) FROM " + TABLE_DREAMS + " WHERE " + COLUMN_DREAM_DATE + " BETWEEN ? AND ?",
                new String[]{fromDate, toDate}
        );
        int count = 0;
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0);
        }
        cursor.close();
        return count;
    }

    public String getCategoryNameById(int categoryId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT name FROM tag_categories WHERE id = ?", new String[]{String.valueOf(categoryId)});
        String name = "Неизвестно";
        if (cursor.moveToFirst()) {
            name = cursor.getString(0);
        }
        cursor.close();
        return name;
    }


    public int getLucidDreamsCount(String fromDate, String toDate) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT COUNT(*) FROM " + TABLE_DREAMS +
                        " WHERE " + COLUMN_DREAM_LUCIDITY + " > 0 AND " + COLUMN_DREAM_DATE + " BETWEEN ? AND ?",
                new String[]{fromDate, toDate}
        );
        int count = 0;
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0);
        }
        cursor.close();
        return count;
    }

    public List<String> getTopTags(String fromDate, String toDate, int limit) {
        SQLiteDatabase db = this.getReadableDatabase();
        List<String> topTags = new ArrayList<>();
        Cursor cursor = db.rawQuery(
                "SELECT t." + COLUMN_TAG_NAME + ", COUNT(*) AS count " +
                        "FROM " + TABLE_TAGS + " t " +
                        "INNER JOIN " + TABLE_DREAM_TAG_LINK + " dt ON t." + COLUMN_TAG_ID + " = dt." + COLUMN_TAG_ID_FK + " " +
                        "INNER JOIN " + TABLE_DREAMS + " d ON dt." + COLUMN_DREAM_ID_FK + " = d." + COLUMN_DREAM_ID + " " +
                        "WHERE d." + COLUMN_DREAM_DATE + " BETWEEN ? AND ? " +
                        "GROUP BY t." + COLUMN_TAG_NAME + " " +
                        "ORDER BY count DESC " +
                        "LIMIT ?",
                new String[]{fromDate, toDate, String.valueOf(limit)}
        );
        if (cursor.moveToFirst()) {
            do {
                topTags.add(cursor.getString(0));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return topTags;
    }

}
