package com.geely.browser.fragments;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.geely.browser.R;
import com.geely.browser.activities.MainActivity;
import com.geely.browser.utils.WebViewSetup;
import com.geely.browser.viewmodels.BrowserViewModel;
import com.geely.browser.viewmodels.MainViewModel;

/**
 * Fragment responsible for displaying web content within a WebView.
 * Each instance of BrowserFragment represents a single "tab" in the browser.
 * It handles WebView configuration, loading URLs, and communicating page state
 * (URL, title, loading progress, navigation capabilities) back to the MainViewModel.
 */
public class BrowserFragment extends Fragment {

    private static final String ARG_URL = "url"; // Argument key for the initial URL
    private String initialUrl; // URL to load when the fragment is first created

    private WebView webView;
    private BrowserViewModel browserViewModel; // ViewModel for this specific fragment instance (tab-specific state)
    private MainViewModel mainActivityViewModel; // Activity-scoped ViewModel for shared state and inter-component communication

    /**
     * Creates a new instance of BrowserFragment.
     *
     * @param url The initial URL to load in this fragment's WebView.
     * @return A new instance of BrowserFragment.
     */
    public static BrowserFragment newInstance(String url) {
        BrowserFragment fragment = new BrowserFragment();
        Bundle args = new Bundle();
        args.putString(ARG_URL, url);
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Called when the fragment is first created.
     * Initializes ViewModels and retrieves the initial URL from arguments.
     *
     * @param savedInstanceState If the fragment is being re-created from a previous saved state, this is the state.
     */
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            initialUrl = getArguments().getString(ARG_URL);
        }
        // Initialize ViewModel specific to this fragment instance (e.g., for scroll position, tab-specific settings)
        browserViewModel = new ViewModelProvider(this).get(BrowserViewModel.class);
        // Obtain the Activity-scoped MainViewModel for communication with MainActivity and shared state
        if (getActivity() instanceof MainActivity) {
            mainActivityViewModel = new ViewModelProvider(requireActivity()).get(MainViewModel.class);
        }
    }

    /**
     * Called to have the fragment instantiate its user interface view.
     * Inflates the layout, initializes the WebView, configures its settings,
     * and loads the initial URL. It also observes user agent settings from MainViewModel.
     *
     * @param inflater The LayoutInflater object that can be used to inflate any views in the fragment.
     * @param container If non-null, this is the parent view that the fragment's UI should be attached to.
     * @param savedInstanceState If non-null, this fragment is being re-constructed from a previous saved state.
     * @return Return the View for the fragment's UI, or null.
     */
    @SuppressLint("ClickableViewAccessibility") // Suppresses lint warning for setOnTouchListener if not performing accessibility actions
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_browser, container, false);
        webView = view.findViewById(R.id.webview);

        // Configure WebView settings, client, and chrome client
        // Pass MainViewModel to MyWebChromeClient for access to settings like pop-up blocker status
        WebViewSetup.configureWebView(webView, new MyWebViewClient(), new MyWebChromeClient(mainActivityViewModel));

        // Observe user agent mode changes from MainViewModel
        if (mainActivityViewModel != null) {
            mainActivityViewModel.getUserAgentMode().observe(getViewLifecycleOwner(), mode -> {
                if (webView != null) { // Ensure webView is initialized
                    boolean isDesktop = "desktop".equals(mode);
                    WebViewSetup.setUserAgent(webView, isDesktop);
                    // TODO: Consider reloading the page if the user agent changes while a page is loaded.
                    // This might be disruptive, so it needs careful consideration.
                    // if (webView.getUrl() != null && !webView.getUrl().isEmpty()) webView.reload();
                }
            });
        }

        // Basic touch listener, can be expanded for custom gestures.
        // Standard pinch zoom is handled by WebView if settings are enabled.
        webView.setOnTouchListener((v, event) -> {
            // v.performClick(); // Call performClick when a click is detected
            return false; // Return false to allow WebView to handle the event
        });

        // Load initial URL or the last known URL if fragment is recreated
        if (initialUrl != null) {
            webView.loadUrl(initialUrl);
        } else if (mainActivityViewModel != null && mainActivityViewModel.getCurrentUrl().getValue() != null &&
                   !mainActivityViewModel.getCurrentUrl().getValue().isEmpty()) {
            // If fragment is recreated (e.g., due to configuration change), load the last known URL from MainViewModel
            webView.loadUrl(mainActivityViewModel.getCurrentUrl().getValue());
        }
        return view;
    }
    
    /**
     * Called immediately after onCreateView() has returned, but before any saved state has been restored in to the view.
     * @param view The View returned by {@link #onCreateView(LayoutInflater, ViewGroup, Bundle)}.
     * @param savedInstanceState If non-null, this fragment is being re-constructed
     * from a previous saved state as given here.
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // Further view setup can be done here if needed
    }

    // --- WebView Control Methods ---

    /**
     * Loads the specified URL in the WebView.
     * @param url The URL to load.
     */
    public void loadUrl(String url) {
        if (webView != null && url != null && !url.isEmpty()) {
            webView.loadUrl(url);
        }
    }

    /**
     * Checks if the WebView can navigate back in its history.
     * @return True if the WebView can go back, false otherwise.
     */
    public boolean canGoBack() {
        return webView != null && webView.canGoBack();
    }

    /**
     * Navigates the WebView back in its history, if possible.
     */
    public void goBack() {
        if (canGoBack()) { // Check before calling to avoid issues
            webView.goBack();
        }
    }

    /**
     * Checks if the WebView can navigate forward in its history.
     * @return True if the WebView can go forward, false otherwise.
     */
    public boolean canGoForward() {
        return webView != null && webView.canGoForward();
    }

    /**
     * Navigates the WebView forward in its history, if possible.
     */
    public void goForward() {
        if (canGoForward()) { // Check before calling
            webView.goForward();
        }
    }

    /**
     * Reloads the current page in the WebView.
     */
    public void reload() {
        if (webView != null) {
            webView.reload();
        }
    }

    /**
     * Stops the current page loading in the WebView.
     */
    public void stopLoading() {
        if (webView != null) {
            webView.stopLoading();
        }
    }
    
    /**
     * Provides direct access to the WebView instance.
     * Use with caution, prefer methods that abstract WebView interactions.
     * @return The WebView instance managed by this fragment.
     */
    public WebView getWebView(){
        return webView;
    }

    // --- Inner Classes for WebView Callbacks ---

    /**
     * Custom WebViewClient to handle page loading events and update MainViewModel.
     */
    private class MyWebViewClient extends WebViewClient {
        /**
         * Notifies the host application that a page has started loading.
         * Updates MainViewModel with the new URL and loading state.
         */
        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
            if (mainActivityViewModel != null) {
                mainActivityViewModel.setCurrentUrl(url); // This will also trigger checkIfBookmarked in MainViewModel
                mainActivityViewModel.setLoading(true);
            }
        }

        /**
         * Notifies the host application that a page has finished loading.
         * Updates MainViewModel with loading state, navigation capabilities, and page title.
         */
        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            if (mainActivityViewModel != null) {
                mainActivityViewModel.setLoading(false);
                mainActivityViewModel.setCanGoBack(view.canGoBack());
                mainActivityViewModel.setCanGoForward(view.canGoForward());
                mainActivityViewModel.setCurrentTitle(view.getTitle());
            }
        }
    }

    /**
     * Custom WebChromeClient to handle UI-related events like progress changes,
     * receiving page titles, and managing pop-up windows based on settings.
     */
    private static class MyWebChromeClient extends WebChromeClient {
        private final MainViewModel mainViewModel; // Reference to MainViewModel for settings access

        /**
         * Constructor for MyWebChromeClient.
         * @param mainViewModel The MainViewModel instance to access browser settings.
         */
        public MyWebChromeClient(MainViewModel mainViewModel) {
            this.mainViewModel = mainViewModel;
        }

        /**
         * Notifies the host application of the current page loading progress.
         * (Currently not used to update any UI element).
         */
        @Override
        public void onProgressChanged(WebView view, int newProgress) {
            super.onProgressChanged(view, newProgress);
            // TODO: Implement a progress bar in MainActivity and update it here if desired.
        }

        /**
         * Notifies the host application of a new title for the current page.
         * Updates MainViewModel with the new title.
         */
        @Override
        public void onReceivedTitle(WebView view, String title) {
            super.onReceivedTitle(view, title);
            if (mainViewModel != null) {
                mainViewModel.setCurrentTitle(title);
            }
        }

        /**
         * Handles requests to create a new window (e.g., for pop-ups).
         * If pop-up blocker is enabled in settings, it blocks the window and shows a Toast.
         * Otherwise, it allows the window to be created (simplified: opens in a new conceptual WebView).
         *
         * @return True if the host application will create a new window, false otherwise.
         *         Returning true means the WebView has handled the request.
         */
        @Override
        public boolean onCreateWindow(WebView view, boolean isDialog, boolean isUserGesture, Message resultMsg) {
            if (mainViewModel != null) {
                Boolean popupBlockerEnabled = mainViewModel.isPopupBlockerEnabled().getValue();
                if (popupBlockerEnabled != null && popupBlockerEnabled) {
                    // Show a toast message indicating that the pop-up was blocked
                    Toast.makeText(view.getContext(), R.string.pref_summary_popup_blocker_on, Toast.LENGTH_SHORT).show();
                    return true; // Pop-up blocked
                }
            }
            
            // If pop-up blocker is off or ViewModel not available, allow the pop-up.
            // This is a simplified way to handle pop-ups. A more robust implementation
            // would involve creating a new BrowserFragment via TabManager.
            WebView newWebView = new WebView(view.getContext());
            // Basic configuration for the new WebView. It should ideally use the same settings
            // as the main WebView, possibly through a shared configuration utility.
            WebViewSetup.configureWebView(newWebView, new WebViewClient(), new WebChromeClient()); 

            // The transport mechanism to send the new WebView back to the system
            WebView.WebViewTransport transport = (WebView.WebViewTransport) resultMsg.obj;
            transport.setWebView(newWebView);
            resultMsg.sendToTarget();
            
            // TODO: Integrate this new WebView with TabManager to open it as a new tab.
            // Currently, this new WebView is created but not added to the main UI structure,
            // which means it might not be visible or manageable as a separate tab.
            // This is a significant simplification for pop-up handling.
            return true; // We've handled the request to create a new window.
        }
    }
}
