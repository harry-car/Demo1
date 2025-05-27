package com.geely.browser.viewmodels;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import com.geely.browser.model.BookmarkEntity;
import com.geely.browser.model.BookmarkRepository;
import java.util.List;

public class BookmarksViewModel extends AndroidViewModel {
    private BookmarkRepository repository;
    private LiveData<List<BookmarkEntity>> allBookmarks;

    public BookmarksViewModel(Application application) {
        super(application);
        repository = new BookmarkRepository(application);
        allBookmarks = repository.getAllBookmarks();
    }

    public LiveData<List<BookmarkEntity>> getAllBookmarks() {
        return allBookmarks;
    }

    public void deleteBookmark(BookmarkEntity bookmark) {
        repository.delete(bookmark);
    }
}
