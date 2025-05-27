package com.geely.browser.utils;

import android.annotation.SuppressLint;
import android.content.pm.ApplicationInfo; // For checking debuggable status
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

/**
 * Utility class for configuring {@link WebView} instances with common settings.
 * This helps in centralizing WebView setup logic.
 */
public class WebViewSetup {

    /**
     * Configures a {@link WebView} with a standard set of features and clients.
     * Enables JavaScript, DOM storage, database, zoom controls, and sets up
     * the provided WebViewClient and WebChromeClient.
     *
     * @param webView The WebView instance to configure.
     * @param webViewClient The {@link WebViewClient} to handle page navigation and events.
     * @param webChromeClient The {@link WebChromeClient} to handle browser UI events (progress, titles, pop-ups).
     */
    @SuppressLint("SetJavaScriptEnabled") // JavaScript is essential for modern web browsing.
    public static void configureWebView(WebView webView, WebViewClient webViewClient, WebChromeClient webChromeClient) {
        WebSettings settings = webView.getSettings();

        // Enable essential web features
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true); // Required for websites that use localStorage or sessionStorage.
        settings.setDatabaseEnabled(true);   // For websites that use HTML5 Web SQL Database (though less common now).
        
        // Configure viewport settings for proper page scaling
        settings.setLoadWithOverviewMode(true); // Zooms out the page to fit the WebView's width.
        settings.setUseWideViewPort(true);      // Allows the WebView to use a viewport wider than its actual width, enabling zoom-out.

        // Enable built-in zoom controls but hide the on-screen buttons for a cleaner UI.
        // Pinch-to-zoom will still be functional.
        settings.setBuiltInZoomControls(true);
        settings.setDisplayZoomControls(false);

        // Performance settings
        settings.setRenderPriority(WebSettings.RenderPriority.HIGH); // Deprecated in API 18, but harmless. Modern WebViews manage this.
        // settings.setCacheMode(WebSettings.LOAD_DEFAULT); // Use default caching behavior. Can be adjusted for specific needs.

        // Enable file access and content access.
        // Be cautious with file access, especially from file:/// URIs if loading local untrusted content.
        settings.setAllowFileAccess(true); 
        settings.setAllowContentAccess(true);

        // Enable remote debugging of WebView content in debug builds.
        // This allows using Chrome DevTools to inspect the WebView.
        if (0 != (webView.getContext().getApplicationInfo().flags & ApplicationInfo.FLAG_DEBUGGABLE)) {
           WebView.setWebContentsDebuggingEnabled(true);
        }

        // Set the clients to handle various WebView events
        webView.setWebViewClient(webViewClient);
        webView.setWebChromeClient(webChromeClient);

        // Note: User Agent and pop-up blocking are typically handled dynamically:
        // - User Agent is set in `setUserAgent()` based on user preference.
        // - Pop-up blocking is handled within the `WebChromeClient`'s `onCreateWindow` method,
        //   based on settings from `MainViewModel`.
    }

    /**
     * Sets the User-Agent string for the WebView.
     * Allows switching between mobile and desktop versions of websites.
     * Also adjusts viewport settings (`useWideViewPort`, `loadWithOverviewMode`)
     * which are often necessary for correct rendering of desktop sites on mobile.
     *
     * @param webView The WebView instance whose User-Agent is to be set.
     * @param isDesktopMode If true, sets a common desktop User-Agent string.
     *                      If false, resets to the WebView's default (mobile) User-Agent.
     */
    public static void setUserAgent(WebView webView, boolean isDesktopMode) {
        WebSettings settings = webView.getSettings();
        String currentUserAgent = settings.getUserAgentString(); // Get current for comparison if needed

        if (isDesktopMode) {
            // Example Desktop User Agent (Chrome on Windows).
            // This string might need to be updated periodically to reflect common desktop browsers.
            String desktopUserAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/90.0.4430.212 Safari/537.36";
            // Only set if different to avoid unnecessary reloads or changes if already in desktop mode with this UA
            if (!desktopUserAgent.equals(currentUserAgent)) {
                 settings.setUserAgentString(desktopUserAgent);
            }
        } else {
            // `null` resets the User-Agent to the WebView's default.
            // Only set if different to avoid unnecessary reloads or changes if already in mobile mode with default UA
            if (currentUserAgent != null && !currentUserAgent.equals(WebSettings.getDefaultUserAgent(webView.getContext()))) { // Compare with actual default
                 settings.setUserAgentString(null);
            } else if (currentUserAgent == null && isDesktopMode) { // If it was desktop previously and now switching to mobile
                 settings.setUserAgentString(null);
            }
        }
        
        // These viewport settings are crucial for desktop mode to behave as expected.
        // For mobile mode, they ensure the site is scaled to fit the screen.
        settings.setUseWideViewPort(true); // Always true for flexibility, site will control via viewport meta tag
        settings.setLoadWithOverviewMode(true); // Zoom out to show full page width

        // TODO: Consider if a page reload (`webView.reload()`) is necessary after changing the User-Agent
        // for the change to take full effect on the currently loaded page. This can be disruptive to the user.
        // Often, the User-Agent is best applied before loading a new page or when a tab is first created.
    }
}
