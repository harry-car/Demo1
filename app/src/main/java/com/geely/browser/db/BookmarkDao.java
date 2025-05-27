package com.geely.browser.db;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import com.geely.browser.model.BookmarkEntity;
import java.util.List;

@Dao
public interface BookmarkDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(BookmarkEntity bookmark);

    @Query("SELECT * FROM bookmarks ORDER BY timestamp DESC")
    LiveData<List<BookmarkEntity>> getAllBookmarks();

    @Query("SELECT * FROM bookmarks WHERE url = :url LIMIT 1")
    LiveData<BookmarkEntity> getBookmarkByUrl(String url);
    
    @Query("SELECT COUNT(*) FROM bookmarks WHERE url = :url")
    LiveData<Integer> isBookmarked(String url); // Returns 0 or 1

    @Delete
    void delete(BookmarkEntity bookmark);

    @Query("DELETE FROM bookmarks WHERE url = :url")
    void deleteByUrl(String url);
}
