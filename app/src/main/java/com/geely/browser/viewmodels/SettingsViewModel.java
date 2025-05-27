package com.geely.browser.viewmodels;

import android.app.Application;
import android.content.SharedPreferences;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.preference.PreferenceManager;

public class SettingsViewModel extends AndroidViewModel {

    private SharedPreferences sharedPreferences;

    // LiveData to be observed by MainViewModel or other components
    private MutableLiveData<String> userAgentMode = new MutableLiveData<>();
    private MutableLiveData<Boolean> popupBlockerEnabled = new MutableLiveData<>();
    private MutableLiveData<String> searchEngineUrl = new MutableLiveData<>();

    public static final String KEY_PREF_BROWSING_MODE = "pref_browsing_mode";
    public static final String KEY_PREF_POPUP_BLOCKER = "pref_popup_blocker";
    public static final String KEY_PREF_SEARCH_ENGINE = "pref_search_engine";

    public SettingsViewModel(Application application) {
        super(application);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(application);
        loadSettings();
    }

    private void loadSettings() {
        userAgentMode.setValue(sharedPreferences.getString(KEY_PREF_BROWSING_MODE, "mobile"));
        popupBlockerEnabled.setValue(sharedPreferences.getBoolean(KEY_PREF_POPUP_BLOCKER, true));
        searchEngineUrl.setValue(sharedPreferences.getString(KEY_PREF_SEARCH_ENGINE, "https://www.google.com/search?q="));
    }

    // Getters for LiveData
    public LiveData<String> getUserAgentMode() { return userAgentMode; }
    public LiveData<Boolean> getPopupBlockerEnabled() { return popupBlockerEnabled; }
    public LiveData<String> getSearchEngineUrl() { return searchEngineUrl; }

    // Methods to update LiveData when preferences change (called from SettingsFragment)
    public void updateUserAgentMode(String mode) {
        userAgentMode.setValue(mode);
    }

    public void updatePopupBlockerEnabled(boolean enabled) {
        popupBlockerEnabled.setValue(enabled);
    }

    public void updateSearchEngineUrl(String url) {
        searchEngineUrl.setValue(url);
    }
}
