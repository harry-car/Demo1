package com.geely.browser.fragments;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;
import androidx.lifecycle.ViewModelProvider;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import com.geely.browser.R;
import com.geely.browser.viewmodels.SettingsViewModel;

/**
 * Fragment for displaying and managing user preferences.
 * This fragment uses the AndroidX Preference library to create a settings screen
 * based on an XML resource file (`R.xml.preferences`).
 * It listens for changes in SharedPreferences and updates the {@link SettingsViewModel} accordingly,
 * which in turn updates the {@link MainViewModel} for application-wide settings changes.
 */
public class SettingsFragment extends PreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener {

    private SettingsViewModel settingsViewModel;

    /**
     * Called during {@link #onCreate(Bundle)} to supply the preferences for this fragment.
     * Subclasses are expected to call {@link #setPreferenceScreen(PreferenceScreen)} either
     * directly or via helper methods such as {@link #addPreferencesFromResource(int)}.
     *
     * @param savedInstanceState If the fragment is being re-created from a previous saved state, this is the state.
     * @param rootKey If non-null, this preference fragment should be rooted at the {@link PreferenceScreen} with this key.
     */
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.preferences, rootKey);

        // Obtain the Activity-scoped SettingsViewModel.
        // This ViewModel facilitates communication of settings changes to other parts of the app (e.g., MainViewModel).
        settingsViewModel = new ViewModelProvider(requireActivity()).get(SettingsViewModel.class);

        // Initialize summaries for ListPreferences to show current selection
        updateListPreferenceSummary(findPreference(SettingsViewModel.KEY_PREF_BROWSING_MODE));
        updateListPreferenceSummary(findPreference(SettingsViewModel.KEY_PREF_SEARCH_ENGINE));

        // Setup click listener for "Download Manager" preference
        Preference downloadsPref = findPreference("pref_manage_downloads");
        if (downloadsPref != null) {
            downloadsPref.setOnPreferenceClickListener(preference -> {
                // TODO: Implement actual Download Manager activity/screen and launch it here.
                Toast.makeText(getContext(), R.string.toast_downloads_manager_clicked, Toast.LENGTH_SHORT).show();
                return true;
            });
        }

        // Setup click listener for "Add to Home Screen" preference
        Preference addToHomeScreenPref = findPreference("pref_add_to_homescreen");
        if (addToHomeScreenPref != null) {
            addToHomeScreenPref.setOnPreferenceClickListener(preference -> {
                // TODO: Implement "Add to Home Screen" functionality.
                // This might involve interacting with MainActivity/MainViewModel to get current page details
                // and then creating a shortcut.
                Toast.makeText(getContext(), R.string.toast_add_to_homescreen_clicked, Toast.LENGTH_SHORT).show();
                return true;
            });
        }
    }

    /**
     * Called when the fragment is visible to the user and actively running.
     * Registers the SharedPreferences change listener and updates preference summaries.
     */
    @Override
    public void onResume() {
        super.onResume();
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
        // Update summaries again, in case a value was changed programmatically or is out of sync.
        updateListPreferenceSummary(findPreference(SettingsViewModel.KEY_PREF_BROWSING_MODE));
        updateListPreferenceSummary(findPreference(SettingsViewModel.KEY_PREF_SEARCH_ENGINE));
    }

    /**
     * Called when the Fragment is no longer resumed.
     * Unregisters the SharedPreferences change listener to prevent memory leaks.
     */
    @Override
    public void onPause() {
        super.onPause();
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }

    /**
     * Called when a shared preference is changed, added, or removed.
     * This may be called even if a preference is set to its existing value.
     * This callback will be invoked on the main thread.
     *
     * @param sharedPreferences The {@link SharedPreferences} that received the change.
     * @param key The key of the preference that was changed.
     */
    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        Preference preference = findPreference(key);
        // Update the corresponding LiveData in SettingsViewModel based on the changed preference.
        // MainViewModel observes these changes indirectly via its own SharedPreferences listener.
        // TODO: This direct update to SettingsViewModel is redundant if MainViewModel is already listening to SharedPreferences directly.
        // The original design had SettingsViewModel update MainViewModel's LiveData directly, but MainViewModel now listens to SP.
        // For consistency, ensure SettingsViewModel's LiveData (if used by other UI components directly tied to settings)
        // are also updated, or simplify if MainViewModel is the sole consumer of these settings states.
        // Current implementation: SettingsViewModel LiveData are updated here. MainViewModel updates its own LiveData from SP.

        if (SettingsViewModel.KEY_PREF_BROWSING_MODE.equals(key)) {
            String value = sharedPreferences.getString(key, "mobile");
            settingsViewModel.updateUserAgentMode(value); // Update SettingsViewModel's LiveData
            updateListPreferenceSummary(preference); // Update the summary displayed in settings
        } else if (SettingsViewModel.KEY_PREF_POPUP_BLOCKER.equals(key)) {
            boolean value = sharedPreferences.getBoolean(key, true);
            settingsViewModel.updatePopupBlockerEnabled(value); // Update SettingsViewModel's LiveData
            // SwitchPreferenceCompat updates its own summary on/off.
        } else if (SettingsViewModel.KEY_PREF_SEARCH_ENGINE.equals(key)) {
            String value = sharedPreferences.getString(key, "https://www.google.com/search?q=");
            settingsViewModel.updateSearchEngineUrl(value); // Update SettingsViewModel's LiveData
            updateListPreferenceSummary(preference); // Update the summary displayed in settings
        }
    }

    /**
     * Updates the summary of a {@link ListPreference} to display the human-readable entry
     * corresponding to the currently selected value.
     *
     * @param preference The {@link Preference} to update. If it's not a ListPreference, this method does nothing.
     */
    private void updateListPreferenceSummary(Preference preference) {
        if (preference instanceof ListPreference) {
            ListPreference listPreference = (ListPreference) preference;
            // Sets the summary to the currently selected entry's display name.
            listPreference.setSummary(listPreference.getEntry());
        }
    }
}
