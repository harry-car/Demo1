package com.example.pwahelperbrowser;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.content.Intent;
import android.webkit.PermissionRequest;
import android.webkit.WebChromeClient;
import android.webkit.GeolocationPermissions;
import androidx.core.content.ContextCompat;
import android.content.pm.PackageManager;
import java.util.ArrayList;
import java.util.Arrays;
// For System.out.println, no specific import needed beyond java.lang which is default
import android.content.pm.ShortcutInfo;
import android.content.pm.ShortcutManager;
import android.graphics.drawable.Icon;
import android.net.Uri;
import android.os.Build;
// No need for android.util.Log if using System.out.println

public class MainActivity extends AppCompatActivity {

    private WebView webView;
    private EditText urlEditText;
    private Button goButton;
    private Button backButton;
    private Button forwardButton;
    private Button refreshButton;
    private Button addToHomeScreenButton;

    private boolean manifestExists = false;
    private boolean serviceWorkerExists = false;
    private String currentManifestUrl = null; // Renamed from manifestUrl to avoid confusion
    private String appName = "PWA App"; // Default/fallback
    // private String appIconUrl = null; // Fallback for icon, not used in this simplified version

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize WebView
        webView = findViewById(R.id.webView);

        // Initialize other controls
        urlEditText = findViewById(R.id.urlEditText);
        goButton = findViewById(R.id.goButton);
        backButton = findViewById(R.id.backButton);
        forwardButton = findViewById(R.id.forwardButton);
        refreshButton = findViewById(R.id.refreshButton);
        addToHomeScreenButton = findViewById(R.id.addToHomeScreenButton);

        // Configure WebView settings
        android.webkit.WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true); // Enable JavaScript
        webSettings.setDomStorageEnabled(true); // Enable DOM Storage API (already enabled)
        webSettings.setDatabaseEnabled(true); // Enable Database Storage API (already enabled)

        // PWA Cache settings
        webSettings.setCacheMode(android.webkit.WebSettings.LOAD_DEFAULT);
        webSettings.setAppCacheEnabled(true);
        webSettings.setAppCachePath(getCacheDir().getAbsolutePath() + "/appcache");

        webSettings.setAllowFileAccessFromFileURLs(true); // Enable file access from file URLs
        webSettings.setAllowUniversalAccessFromFileURLs(true); // Enable universal access from file URLs

        // Set a basic WebViewClient (will be overridden later with more specific one)
        webView.setWebViewClient(new WebViewClient());

        // Handle incoming intent for PWA URL loading (moved from later in the original code)
        Intent intent = getIntent();
        if (Intent.ACTION_VIEW.equals(intent.getAction()) && intent.getData() != null) {
            String url = intent.getDataString();
            System.out.println("PWA_LAUNCH: Launched with URL from shortcut: " + url);
            webView.loadUrl(url);
        } else {
            // Load a default URL if not launched from a shortcut
            System.out.println("PWA_LAUNCH: Launched normally, loading default URL.");
            webView.loadUrl("https://www.google.com");
        }
        // urlEditText.setText(webView.getUrl()); // Show current URL - This will be updated by onPageFinished

        // Set OnClickListener for goButton
        goButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadUrlFromEditText();
            }
        });

        // Set OnEditorActionListener for urlEditText
        urlEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_GO || (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN)) {
                    loadUrlFromEditText();
                    return true;
                }
                return false;
            }
        });

        // Set OnClickListener for backButton
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (webView.canGoBack()) {
                    webView.goBack();
                }
            }
        });

        // Set OnClickListener for forwardButton
        forwardButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (webView.canGoForward()) {
                    webView.goForward();
                }
            }
        });

        // Set OnClickListener for refreshButton
        refreshButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                webView.reload();
            }
        });

        // Update URL in EditText when page finishes loading and check PWA features
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                if (urlEditText != null) { // Ensure urlEditText is initialized
                    urlEditText.setText(url);
                }
                // Reset flags for fresh detection
                manifestExists = false;
                serviceWorkerExists = false;
                currentManifestUrl = null;
                // Reset appName to default or use title
                appName = webView.getTitle() != null && !webView.getTitle().isEmpty() ? webView.getTitle() : "PWA App";

                checkPwaFeatures(url); // Call PWA feature detection
            }

            // Keep navigation within WebView
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;
            }
        });

        // Set OnClickListener for addToHomeScreenButton
        addToHomeScreenButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createHomeScreenShortcut();
            }
        });

        // Set the custom WebChromeClient
        webView.setWebChromeClient(new PwaWebChromeClient());
    }

    private class PwaWebChromeClient extends WebChromeClient {
        @Override
        public void onPermissionRequest(final PermissionRequest request) {
            // This method handles requests for protected resources like camera, microphone.
            // For simplicity in this step, we'll try to grant common permissions if they are declared in the manifest.
            // A more robust app would show a custom UI or check global app settings.

            System.out.println("PWA_PERMISSIONS: onPermissionRequest for " + Arrays.toString(request.getResources()));

            ArrayList<String> grantedPermissions = new ArrayList<>();
            for (String permission : request.getResources()) {
                if (permission.equals(PermissionRequest.RESOURCE_VIDEO_CAPTURE)) {
                    if (ContextCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                        grantedPermissions.add(permission);
                    }
                } else if (permission.equals(PermissionRequest.RESOURCE_AUDIO_CAPTURE)) {
                    if (ContextCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
                        grantedPermissions.add(permission);
                    }
                }
                // Add more specific resource checks if needed (e.g., MIDI, protected media)
            }

            if (!grantedPermissions.isEmpty()) {
                System.out.println("PWA_PERMISSIONS: Granting permissions: " + grantedPermissions.toString());
                request.grant(grantedPermissions.toArray(new String[0]));
            } else {
                // If no specific permissions handled or manifest declarations missing, deny.
                System.out.println("PWA_PERMISSIONS: Denying permission request: " + Arrays.toString(request.getResources()));
                request.deny();
            }
        }

        @Override
        public void onGeolocationPermissionsShowPrompt(String origin, GeolocationPermissions.Callback callback) {
            // This method handles requests for geolocation.
            System.out.println("PWA_PERMISSIONS: onGeolocationPermissionsShowPrompt for " + origin);
            if (ContextCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                callback.invoke(origin, true, false);
                 System.out.println("PWA_PERMISSIONS: Geolocation permission granted for " + origin);
            } else {
                // Consider requesting android.Manifest.permission.ACCESS_FINE_LOCATION here
                // For now, just deny if not already granted at app level.
                callback.invoke(origin, false, false);
                System.out.println("PWA_PERMISSIONS: Geolocation permission denied for " + origin + " (app-level permission missing)");
            }
        }

        // Optional: Override onJsAlert, onJsConfirm, onJsPrompt for better dialogs if needed.
        // Optional: Override onShowFileChooser for file input handling.
    }

    private void updateA2HSButtonVisibility() {
        if (manifestExists && serviceWorkerExists) {
            addToHomeScreenButton.setVisibility(View.VISIBLE);
        } else {
            addToHomeScreenButton.setVisibility(View.GONE);
        }
    }

    private void checkPwaFeatures(String currentUrl) {
        System.out.println("PWA_DETECT: Checking PWA features for URL: " + currentUrl);

        // Manifest Detection
        webView.evaluateJavascript(
            "(function() { " +
            "  var manifestTag = document.querySelector('link[rel=\"manifest\"]'); " +
            "  if (manifestTag) { return manifestTag.href; } " +
            "  return null; " +
            "})();",
            value -> {
                if (value != null && !value.equals("null") && !value.isEmpty()) {
                    currentManifestUrl = value.replaceAll("^\"|\"$", ""); // Clean quotes
                    manifestExists = true;
                    System.out.println("PWA_DETECT: Manifest found at: " + currentManifestUrl);
                    // TODO: Optionally parse manifest for appName and appIconUrl here
                    // For now, appName uses webView.getTitle() as a fallback (set in onPageFinished)
                } else {
                    manifestExists = false;
                    System.out.println("PWA_DETECT: No manifest tag found via JS.");
                }
                updateA2HSButtonVisibility(); // Call visibility update
            }
        );

        // Service Worker Detection
        webView.evaluateJavascript(
            "(function() { return !!navigator.serviceWorker && !!navigator.serviceWorker.controller; })();",
            value -> {
                serviceWorkerExists = Boolean.parseBoolean(value);
                if (serviceWorkerExists) {
                    System.out.println("PWA_DETECT: Active Service Worker found.");
                } else {
                    System.out.println("PWA_DETECT: No active Service Worker.");
                }
                updateA2HSButtonVisibility(); // Call visibility update
            }
        );
    }

    private void createHomeScreenShortcut() {
        if (webView.getUrl() == null || webView.getUrl().isEmpty()) {
            System.out.println("PWA_A2HS: Cannot create shortcut, WebView URL is empty.");
            return;
        }

        // Use webView.getTitle() if appName wasn't updated from manifest
        if (webView.getTitle() != null && !webView.getTitle().isEmpty()) {
            appName = webView.getTitle();
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            ShortcutManager shortcutManager = getSystemService(ShortcutManager.class);
            if (shortcutManager != null && shortcutManager.isRequestPinShortcutSupported()) {
                Intent shortcutIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(webView.getUrl()));
                // Optional: Add more specifics if MainActivity handles different intents
                // shortcutIntent.setClass(this, MainActivity.class); // Make it explicit
                // shortcutIntent.putExtra("url", webView.getUrl()); // Example of passing URL

                ShortcutInfo shortcut = new ShortcutInfo.Builder(this, "pwaAppShortcut_" + System.currentTimeMillis())
                        .setShortLabel(appName)
                        .setLongLabel(appName)
                        .setIcon(Icon.createWithResource(this, R.mipmap.ic_launcher)) // Fallback icon
                        .setIntent(shortcutIntent)
                        .build();
                shortcutManager.requestPinShortcut(shortcut, null);
                System.out.println("PWA_A2HS: Requested pin shortcut (API 26+). App Name: " + appName + " URL: " + webView.getUrl());
            } else {
                System.out.println("PWA_A2HS: Pin shortcut not supported (API 26+).");
            }
        } else {
            // Legacy method for older Android versions
            Intent installShortcutIntent = new Intent("com.android.launcher.action.INSTALL_SHORTCUT");
            installShortcutIntent.putExtra("duplicate", false);
            installShortcutIntent.putExtra(Intent.EXTRA_SHORTCUT_NAME, appName);

            // Icon
            Intent.ShortcutIconResource iconResource = Intent.ShortcutIconResource.fromContext(this, R.mipmap.ic_launcher);
            installShortcutIntent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, iconResource);

            // Launch Intent
            Intent launchIntent = new Intent(this, MainActivity.class);
            launchIntent.setAction(Intent.ACTION_VIEW);
            launchIntent.setData(Uri.parse(webView.getUrl()));
            // Ensure the activity doesn't unnecessarily recreate if already running with same URL
            launchIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);


            installShortcutIntent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, launchIntent);
            sendBroadcast(installShortcutIntent);
            System.out.println("PWA_A2HS: Sent install shortcut broadcast (pre-API 26). App Name: " + appName + " URL: " + webView.getUrl());
        }
    }

    private void loadUrlFromEditText() {
        String url = urlEditText.getText().toString().trim();
        if (!url.isEmpty()) {
            // Add http:// if scheme is missing
            if (!url.startsWith("http://") && !url.startsWith("https://")) {
                url = "http://" + url;
            }
            webView.loadUrl(url);
        }
    }

    // Handle back press for WebView navigation
    @Override
    public void onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }
}
