package com.geely.browser.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import com.geely.browser.R;
import com.geely.browser.fragments.BrowserFragment;
import com.geely.browser.utils.TabManager;
import com.geely.browser.viewmodels.MainViewModel;

/**
 * The main activity of the browser application.
 * This activity hosts the primary UI, including the address bar, navigation controls,
 * and the container for web content (BrowserFragment instances).
 * It coordinates user interactions, manages tabs through TabManager,
 * and observes MainViewModel for state changes.
 */
public class MainActivity extends AppCompatActivity {

    private MainViewModel mainViewModel;
    private TabManager tabManager;

    // UI Elements
    private EditText etAddressBar;
    private ImageButton btnBack, btnForward, btnRefreshStop, btnAddBookmark, btnTabsMenu, btnSettings; // Renamed btnTabs to btnTabsMenu for clarity

    private static final int BOOKMARKS_REQUEST_CODE = 1; // Request code for starting BookmarksActivity

    /**
     * Initializes the activity, sets up the layout, ViewModels, TabManager,
     * UI elements, observers, and listeners.
     *
     * @param savedInstanceState If the activity is being re-initialized after
     *                           previously being shut down then this Bundle contains the data it most
     *                           recently supplied in onSaveInstanceState(Bundle). Otherwise it is null.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize ViewModel and TabManager
        mainViewModel = new ViewModelProvider(this).get(MainViewModel.class);
        tabManager = new TabManager(this, R.id.fragment_container);

        // Setup Toolbar and its components
        androidx.appcompat.widget.Toolbar includedToolbar = findViewById(R.id.toolbar_main);
        etAddressBar = includedToolbar.findViewById(R.id.et_address_bar);
        btnBack = includedToolbar.findViewById(R.id.btn_back);
        btnForward = includedToolbar.findViewById(R.id.btn_forward);
        btnRefreshStop = includedToolbar.findViewById(R.id.btn_refresh_stop);
        btnAddBookmark = includedToolbar.findViewById(R.id.btn_add_bookmark);
        btnTabsMenu = includedToolbar.findViewById(R.id.btn_tabs); // Corresponds to R.id.btn_tabs in toolbar_layout.xml
        btnSettings = includedToolbar.findViewById(R.id.btn_settings);
        
        androidx.appcompat.widget.Toolbar actualToolbar = includedToolbar.findViewById(R.id.toolbar);
        if (actualToolbar != null) {
            setSupportActionBar(actualToolbar);
        }

        // Hide default title to use custom address bar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        // Load initial tab if activity is newly created
        if (savedInstanceState == null) {
            String homePageUrl = "https://www.google.com"; // Default homepage
            // Attempt to get default search engine URL from ViewModel, otherwise use Google
            if (mainViewModel.getDefaultSearchEngineUrl().getValue() != null && 
                !mainViewModel.getDefaultSearchEngineUrl().getValue().isEmpty()) {
                // Ensure we get the base URL if it's a search query template
                homePageUrl = mainViewModel.getDefaultSearchEngineUrl().getValue().replace("?q=","").replace("%s", "");
            }
            tabManager.createNewTab(homePageUrl); 
        }

        setupObservers();
        setupUIListeners();
    }

    /**
     * Sets up observers for LiveData objects from MainViewModel.
     * These observers update the UI in response to data changes (e.g., current URL, loading state).
     */
    private void setupObservers() {
        // Observes the current URL and updates the address bar
        mainViewModel.getCurrentUrl().observe(this, url -> {
            if (etAddressBar != null && url != null && !etAddressBar.getText().toString().equals(url)) {
                etAddressBar.setText(url);
            }
        });

        // Observes whether the current page can go back and enables/disables the back button
        mainViewModel.canGoBack().observe(this, canGoBack -> {
            if (btnBack != null) btnBack.setEnabled(canGoBack);
        });

        // Observes whether the current page can go forward and enables/disables the forward button
        mainViewModel.canGoForward().observe(this, canGoForward -> {
            if (btnForward != null) btnForward.setEnabled(canGoForward);
        });

        // Observes the loading state and updates the refresh/stop button icon and description
        mainViewModel.isLoading().observe(this, isLoading -> {
            if (btnRefreshStop != null) {
                btnRefreshStop.setImageResource(isLoading ? R.drawable.ic_close : R.drawable.ic_refresh); // ic_close is placeholder for stop
                btnRefreshStop.setContentDescription(isLoading ? getString(R.string.desc_stop_button) : getString(R.string.desc_refresh_button));
            }
        });
        
        // Observes the bookmark status of the current page and updates the bookmark button icon and description
        mainViewModel.isBookmarked().observe(this, isBookmarked -> {
            if (btnAddBookmark != null) {
                btnAddBookmark.setImageResource(isBookmarked ? R.drawable.ic_bookmark_item : R.drawable.ic_bookmark_border); // ic_bookmark_item is placeholder for filled star
                 btnAddBookmark.setContentDescription(isBookmarked ? getString(R.string.desc_bookmarked_button) : getString(R.string.desc_add_bookmark_button));
            }
        });

        // Observes the target URL (e.g., from address bar input or bookmark click) and loads it in the current fragment
        mainViewModel.getTargetUrl().observe(this, targetUrl -> {
            if (targetUrl != null && !targetUrl.isEmpty()) {
                BrowserFragment currentFragment = tabManager.getCurrentFragment();
                if (currentFragment != null) {
                    currentFragment.loadUrl(targetUrl);
                } else {
                    // If no fragment exists, create a new tab with this URL
                    tabManager.createNewTab(targetUrl);
                }
            }
        });
    }

    /**
     * Sets up listeners for UI elements like buttons and the address bar.
     * These listeners handle user interactions and trigger actions in the MainViewModel or TabManager.
     */
    private void setupUIListeners() {
        // Listener for address bar: handles 'Go' action to load URL or search
        etAddressBar.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_GO) {
                String input = etAddressBar.getText().toString();
                mainViewModel.loadUrlOrSearch(input);
                etAddressBar.clearFocus(); // Hide keyboard
                return true;
            }
            return false;
        });
        
        // Listener for address bar focus: selects all text when focused for easy replacement
        etAddressBar.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                etAddressBar.selectAll();
            }
        });

        // Listener for back button: navigates back in the current fragment's WebView
        btnBack.setOnClickListener(v -> {
            BrowserFragment currentFragment = tabManager.getCurrentFragment();
            if (currentFragment != null) currentFragment.goBack();
        });

        // Listener for forward button: navigates forward in the current fragment's WebView
        btnForward.setOnClickListener(v -> {
            BrowserFragment currentFragment = tabManager.getCurrentFragment();
            if (currentFragment != null) currentFragment.goForward();
        });

        // Listener for refresh/stop button: reloads or stops loading the current page
        btnRefreshStop.setOnClickListener(v -> {
            BrowserFragment currentFragment = tabManager.getCurrentFragment();
            if (currentFragment != null) {
                if (mainViewModel.isLoading().getValue() != null && mainViewModel.isLoading().getValue()) {
                    currentFragment.stopLoading();
                } else {
                    currentFragment.reload();
                }
            }
        });

        // Listener for add bookmark button: toggles the bookmark status for the current page
        btnAddBookmark.setOnClickListener(v -> {
            BrowserFragment currentFragment = tabManager.getCurrentFragment();
            if (currentFragment != null && currentFragment.getWebView() != null) {
                String url = currentFragment.getWebView().getUrl();
                String title = currentFragment.getWebView().getTitle();
                if (url != null && !url.isEmpty()) {
                    String bookmarkTitle = (title == null || title.isEmpty()) ? url : title; // Use URL as title if page title is missing
                    mainViewModel.toggleBookmark(url, bookmarkTitle);
                    // Show feedback Toast
                    Toast.makeText(this, 
                        (mainViewModel.isBookmarked().getValue() != null && mainViewModel.isBookmarked().getValue()) ? getString(R.string.feedback_bookmarked) : getString(R.string.feedback_bookmark_removed), 
                        Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Listener for tabs/menu button: opens BookmarksActivity
        // TODO: This button (R.id.btn_tabs) was originally for a tab switcher. It's now repurposed for bookmarks.
        // Consider renaming ID in toolbar_layout.xml to btn_bookmarks for clarity if this is permanent.
        btnTabsMenu.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, BookmarksActivity.class);
            startActivityForResult(intent, BOOKMARKS_REQUEST_CODE);
        });

        // Listener for settings button: opens SettingsActivity
        btnSettings.setOnClickListener(v -> {
             Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
             startActivity(intent);
        });
    }

    /**
     * Handles the back button press.
     * If the current BrowserFragment can go back in its WebView history, it does so.
     * Otherwise, performs the default back action (e.g., exiting the app).
     */
    @Override
    public void onBackPressed() {
        BrowserFragment currentFragment = tabManager.getCurrentFragment();
        if (currentFragment != null && currentFragment.canGoBack()) {
            currentFragment.goBack();
        } else {
            super.onBackPressed();
        }
    }
    
    /**
     * Provides access to the MainViewModel.
     * Used by fragments (like BrowserFragment) to communicate with or observe the ViewModel.
     * @return The instance of MainViewModel.
     */
    public MainViewModel getMainViewModel() {
        return mainViewModel;
    }

    /**
     * Called when the activity will start interacting with the user.
     * Ensures the UI (address bar, navigation buttons, bookmark status) is consistent
     * with the state of the current WebView/tab.
     */
    @Override
    protected void onResume() {
        super.onResume();
        BrowserFragment currentFragment = tabManager.getCurrentFragment();
        if (currentFragment != null && currentFragment.getWebView() != null) {
            String currentWebViewUrl = currentFragment.getWebView().getUrl();
            if (currentWebViewUrl != null && !currentWebViewUrl.isEmpty()) { 
                mainViewModel.setCurrentUrl(currentWebViewUrl); 
                // checkIfBookmarked is now called by setCurrentUrl observer in MainViewModel
                mainViewModel.setCanGoBack(currentFragment.canGoBack());
                mainViewModel.setCanGoForward(currentFragment.canGoForward());
                mainViewModel.setCurrentTitle(currentFragment.getWebView().getTitle());
                mainViewModel.setLoading(false); // Assume not loading when activity resumes
            }
        } else if (tabManager.getAllTabs().isEmpty()) {
            // If there are no tabs (e.g., after closing the last one and returning to app), create a new one.
             String homePageUrl = "https://www.google.com";
            if (mainViewModel.getDefaultSearchEngineUrl().getValue() != null && 
                !mainViewModel.getDefaultSearchEngineUrl().getValue().isEmpty()) {
                homePageUrl = mainViewModel.getDefaultSearchEngineUrl().getValue().replace("?q=","").replace("%s", "");
            }
            tabManager.createNewTab(homePageUrl);
        }
    }

    /**
     * Handles the result from an activity started for a result (e.g., BookmarksActivity).
     *
     * @param requestCode The integer request code originally supplied to startActivityForResult().
     * @param resultCode  The integer result code returned by the child activity.
     * @param data        An Intent, which can return result data to the caller.
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == BOOKMARKS_REQUEST_CODE && resultCode == Activity.RESULT_OK && data != null) {
            String selectedUrl = data.getStringExtra(BookmarksActivity.EXTRA_SELECTED_URL);
            if (selectedUrl != null && !selectedUrl.isEmpty()) {
                // Load the URL selected from bookmarks
                mainViewModel.loadUrlOrSearch(selectedUrl); 
            }
        }
    }
}
