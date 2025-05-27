package com.geely.browser.model;

import android.app.Application;
import androidx.lifecycle.LiveData;
import com.geely.browser.db.AppDatabase;
import com.geely.browser.db.BookmarkDao;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Repository for managing bookmark data operations.
 * This class abstracts the data source (Room database) from the ViewModels
 * and provides a clean API for bookmark-related actions.
 * All database operations are performed on a background thread using an ExecutorService.
 */
public class BookmarkRepository {
    private final BookmarkDao bookmarkDao; // Data Access Object for bookmarks
    private final LiveData<List<BookmarkEntity>> allBookmarks; // LiveData list of all bookmarks
    private final ExecutorService executorService; // For background execution of database operations

    /**
     * Constructs a new BookmarkRepository.
     * Initializes the database, DAO, LiveData for all bookmarks, and the executor service.
     *
     * @param application The application context, used to get the database instance.
     */
    public BookmarkRepository(Application application) {
        AppDatabase db = AppDatabase.getDatabase(application);
        bookmarkDao = db.bookmarkDao();
        allBookmarks = bookmarkDao.getAllBookmarks();
        // Using a single thread executor to ensure database operations are serialized
        executorService = Executors.newSingleThreadExecutor();
    }

    /**
     * Returns a LiveData list of all bookmarks, ordered by timestamp descending.
     * This LiveData can be observed by UI components for real-time updates.
     *
     * @return LiveData<List<BookmarkEntity>> containing all bookmarks.
     */
    public LiveData<List<BookmarkEntity>> getAllBookmarks() {
        return allBookmarks;
    }

    /**
     * Inserts a new bookmark into the database.
     * The operation is performed on a background thread.
     *
     * @param bookmark The {@link BookmarkEntity} to insert.
     */
    public void insert(BookmarkEntity bookmark) {
        executorService.execute(() -> bookmarkDao.insert(bookmark));
    }

    /**
     * Deletes a specific bookmark from the database.
     * The operation is performed on a background thread.
     *
     * @param bookmark The {@link BookmarkEntity} to delete.
     */
    public void delete(BookmarkEntity bookmark) {
        executorService.execute(() -> bookmarkDao.delete(bookmark));
    }
    
    /**
     * Deletes a bookmark from the database by its URL.
     * The operation is performed on a background thread.
     *
     * @param url The URL of the bookmark to delete.
     */
    public void deleteByUrl(String url) {
        executorService.execute(() -> bookmarkDao.deleteByUrl(url));
    }

    /**
     * Retrieves a specific bookmark by its URL.
     *
     * @param url The URL of the bookmark to retrieve.
     * @return LiveData<BookmarkEntity> containing the bookmark, or null if not found.
     */
    public LiveData<BookmarkEntity> getBookmarkByUrl(String url) {
        return bookmarkDao.getBookmarkByUrl(url);
    }
    
    /**
     * Checks if a URL is already bookmarked.
     * Returns a LiveData<Integer> which will be 1 if bookmarked, 0 otherwise.
     * This is useful for observing the bookmark status reactively.
     *
     * @param url The URL to check.
     * @return LiveData<Integer> indicating if the URL is bookmarked (1 for true, 0 for false).
     */
    public LiveData<Integer> isBookmarked(String url) {
        return bookmarkDao.isBookmarked(url);
    }
}
