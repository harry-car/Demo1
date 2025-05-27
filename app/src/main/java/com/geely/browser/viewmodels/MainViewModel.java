package com.geely.browser.viewmodels;

import android.app.Application;
import android.content.SharedPreferences;
import android.util.Patterns;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.preference.PreferenceManager;
import com.geely.browser.model.BookmarkEntity;
import com.geely.browser.model.BookmarkRepository;

/**
 * Main ViewModel for the browser application.
 * This ViewModel is responsible for managing the overall browser state, including:
 * - Current URL, title, and loading status of the web page.
 * - Navigation capabilities (canGoBack, canGoForward).
 * - Bookmark status and operations.
 * - User preferences/settings that affect browser behavior (e.g., pop-up blocker, user agent).
 * It interacts with the BookmarkRepository for data persistence and listens to SharedPreferences for settings changes.
 */
public class MainViewModel extends AndroidViewModel implements SharedPreferences.OnSharedPreferenceChangeListener {

    // LiveData for UI state observation
    private final MutableLiveData<String> currentUrl = new MutableLiveData<>();
    private final MutableLiveData<String> currentTitle = new MutableLiveData<>();
    private final MutableLiveData<String> targetUrl = new MutableLiveData<>(); // URL to be loaded next
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> canGoBack = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> canGoForward = new MutableLiveData<>(false);
    private final MediatorLiveData<Boolean> isBookmarked = new MediatorLiveData<>();

    // LiveData for settings, updated by SharedPreferences listener
    private final MutableLiveData<Boolean> popupBlockerEnabled = new MutableLiveData<>();
    private final MutableLiveData<String> userAgentMode = new MutableLiveData<>();
    private final MutableLiveData<String> defaultSearchEngineUrl = new MutableLiveData<>();

    private final BookmarkRepository bookmarkRepository;
    private LiveData<Integer> bookmarkStatusForCurrentUrl; // Source LiveData for isBookmarked Mediator
    private final SharedPreferences sharedPreferences;

    /**
     * Constructor for MainViewModel.
     * Initializes the BookmarkRepository, sets up SharedPreferences listener,
     * and loads initial settings.
     *
     * @param application The application context.
     */
    public MainViewModel(Application application) {
        super(application);
        bookmarkRepository = new BookmarkRepository(application);
        isBookmarked.setValue(false); // Default initial state

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(application);
        sharedPreferences.registerOnSharedPreferenceChangeListener(this);
        loadInitialSettings();
    }

    /**
     * Loads initial values for settings from SharedPreferences and updates LiveData.
     */
    private void loadInitialSettings() {
        popupBlockerEnabled.setValue(sharedPreferences.getBoolean(SettingsViewModel.KEY_PREF_POPUP_BLOCKER, true));
        userAgentMode.setValue(sharedPreferences.getString(SettingsViewModel.KEY_PREF_BROWSING_MODE, "mobile"));
        defaultSearchEngineUrl.setValue(sharedPreferences.getString(SettingsViewModel.KEY_PREF_SEARCH_ENGINE, "https://www.google.com/search?q="));
    }

    /**
     * Called when a shared preference is changed, enabling reactive updates to settings LiveData.
     *
     * @param sharedPreferences The SharedPreferences that changed.
     * @param key The key of the preference that was changed, added, or removed.
     */
    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(SettingsViewModel.KEY_PREF_POPUP_BLOCKER)) {
            popupBlockerEnabled.setValue(sharedPreferences.getBoolean(key, true));
        } else if (key.equals(SettingsViewModel.KEY_PREF_BROWSING_MODE)) {
            userAgentMode.setValue(sharedPreferences.getString(key, "mobile"));
        } else if (key.equals(SettingsViewModel.KEY_PREF_SEARCH_ENGINE)) {
            defaultSearchEngineUrl.setValue(sharedPreferences.getString(key, "https://www.google.com/search?q="));
        }
    }

    /**
     * Called when the ViewModel is no longer used and will be destroyed.
     * Unregisters the SharedPreferences listener to prevent memory leaks.
     */
    @Override
    protected void onCleared() {
        super.onCleared();
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(this);
    }

    // --- Getters for LiveData ---
    public LiveData<String> getCurrentUrl() { return currentUrl; }
    public LiveData<String> getCurrentTitle() { return currentTitle; }
    public LiveData<String> getTargetUrl() { return targetUrl; }
    public LiveData<Boolean> isLoading() { return isLoading; }
    public LiveData<Boolean> canGoBack() { return canGoBack; }
    public LiveData<Boolean> canGoForward() { return canGoForward; }
    public LiveData<Boolean> isBookmarked() { return isBookmarked; }
    public LiveData<Boolean> isPopupBlockerEnabled() { return popupBlockerEnabled; }
    public LiveData<String> getUserAgentMode() { return userAgentMode; }
    public LiveData<String> getDefaultSearchEngineUrl() { return defaultSearchEngineUrl; }

    // --- Setters and Action Methods ---

    /**
     * Sets the current URL displayed in the browser.
     * Also triggers a check for the bookmark status of the new URL.
     *
     * @param url The URL string to set as current.
     */
    public void setCurrentUrl(String url) {
        currentUrl.setValue(url);
        // Check bookmark status only if URL is valid
        if (url != null && !url.isEmpty()) {
            checkIfBookmarked(url);
        }
    }

    public void setCurrentTitle(String title) { this.currentTitle.setValue(title); }
    public void setLoading(boolean loading) { isLoading.setValue(loading); }
    public void setCanGoBack(boolean back) { canGoBack.setValue(back); }
    public void setCanGoForward(boolean forward) { canGoForward.setValue(forward); }

    /**
     * Processes user input from the address bar.
     * If the input is a valid URL (with or without scheme), it's set as the target URL.
     * Otherwise, it's treated as a search query and formatted with the default search engine URL.
     *
     * @param input The URL or search query string.
     */
    public void loadUrlOrSearch(String input) {
        String trimmedInput = input.trim();
        if (trimmedInput.isEmpty()) {
            return; // Do nothing for empty input
        }

        String currentSearchEngineBaseUrl = defaultSearchEngineUrl.getValue();
        if (currentSearchEngineBaseUrl == null || currentSearchEngineBaseUrl.isEmpty()) {
            currentSearchEngineBaseUrl = "https://www.google.com/search?q="; // Fallback search engine
        }

        // Check if input is a well-formed URL or a special scheme
        if (trimmedInput.startsWith("http://") || trimmedInput.startsWith("https://") ||
            trimmedInput.startsWith("file:///") || trimmedInput.startsWith("javascript:") ||
            Patterns.WEB_URL.matcher(trimmedInput).matches()) {
            
            String urlToLoad = trimmedInput;
            // Prepend "http://" if no scheme is present and it's not a special URI like 'javascript:' or 'file:'
            if (!urlToLoad.startsWith("http://") && !urlToLoad.startsWith("https://") &&
                !urlToLoad.startsWith("file:///") && !urlToLoad.startsWith("javascript:")) {
                 urlToLoad = "http://" + urlToLoad;
            }
            targetUrl.setValue(urlToLoad);
        } else {
            // Treat as search query
            targetUrl.setValue(currentSearchEngineBaseUrl + trimmedInput);
        }
    }

    /**
     * Toggles the bookmark status for the given URL and title.
     * If already bookmarked, it's deleted. Otherwise, it's inserted.
     *
     * @param url The URL of the page to bookmark/unbookmark.
     * @param title The title of the page; used if creating a new bookmark.
     */
    public void toggleBookmark(String url, String title) {
        if (url == null || url.isEmpty()) {
            return; // Cannot bookmark an invalid URL
        }
        String pageTitle = (title == null || title.isEmpty()) ? url : title; // Use URL if title is missing

        Boolean currentlyBookmarked = isBookmarked.getValue();
        if (currentlyBookmarked != null && currentlyBookmarked) {
            bookmarkRepository.deleteByUrl(url);
        } else {
            BookmarkEntity newBookmark = new BookmarkEntity(url, pageTitle, System.currentTimeMillis());
            bookmarkRepository.insert(newBookmark);
        }
        // The isBookmarked LiveData will update automatically via the checkIfBookmarked observation.
    }

    /**
     * Checks if the given URL is bookmarked and updates the isBookmarked LiveData.
     * It observes the LiveData from BookmarkRepository.isBookmarked().
     *
     * @param url The URL to check.
     */
    public void checkIfBookmarked(String url) {
        if (url == null || url.isEmpty()) {
            isBookmarked.setValue(false); // Invalid URL cannot be bookmarked
            return;
        }

        // Remove the previous source to prevent multiple observers on the MediatorLiveData
        if (bookmarkStatusForCurrentUrl != null) {
            isBookmarked.removeSource(bookmarkStatusForCurrentUrl);
        }

        // Get LiveData for bookmark status from repository
        bookmarkStatusForCurrentUrl = bookmarkRepository.isBookmarked(url);
        // Add new source to MediatorLiveData
        isBookmarked.addSource(bookmarkStatusForCurrentUrl, count -> {
            // Update isBookmarked based on whether the count of matching bookmarks is greater than 0
            isBookmarked.setValue(count != null && count > 0);
        });
    }
    // Setters for settings are removed as these are now driven by SharedPreferences changes
    // and updated via onSharedPreferenceChanged. UI should observe the LiveData.
}
