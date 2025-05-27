package com.geely.browser.model;

import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;
import androidx.annotation.NonNull;

@Entity(tableName = "bookmarks", indices = {@Index(value = "url", unique = true)})
public class BookmarkEntity {
    @PrimaryKey(autoGenerate = true)
    public int id;

    @NonNull
    public String url;

    public String title;
    public long timestamp;

    public BookmarkEntity(@NonNull String url, String title, long timestamp) {
        this.url = url;
        this.title = title;
        this.timestamp = timestamp;
    }
}
