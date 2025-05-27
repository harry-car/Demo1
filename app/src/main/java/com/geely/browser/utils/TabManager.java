package com.geely.browser.utils;

import androidx.fragment.app.Fragment; // Added for clarity on findFragmentByTag return type
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import com.geely.browser.fragments.BrowserFragment;
import java.util.ArrayList;
import java.util.List;

/**
 * Manages browser tabs, each represented by a {@link BrowserFragment}.
 * This class handles the creation, switching, and closing of tabs using the Android FragmentManager.
 *
 * TODO: The current tab management (especially switching and creation) is simplified.
 * A more robust implementation would use `detach` and `attach` or `hide` and `show`
 * for FragmentTransactions to preserve fragment state more effectively when switching tabs,
 * rather than always using `replace` or `add` without proper state handling for existing fragments.
 * The `currentVisibleFragment` logic in `createNewTab` is commented out and needs review.
 */
public class TabManager {
    private final FragmentActivity activity; // Host activity
    private final FragmentManager fragmentManager; // For managing fragments
    private final int containerId; // ID of the container view where fragments are displayed
    private final List<BrowserFragment> tabs; // List to keep track of created BrowserFragment instances
    private int currentTabIndex = -1; // Index of the currently active tab

    /**
     * Constructs a new TabManager.
     *
     * @param activity The hosting FragmentActivity.
     * @param containerId The resource ID of the ViewGroup that will contain the tab fragments.
     */
    public TabManager(FragmentActivity activity, int containerId) {
        this.activity = activity;
        this.fragmentManager = activity.getSupportFragmentManager();
        this.containerId = containerId;
        this.tabs = new ArrayList<>();
    }

    /**
     * Creates a new tab and displays it.
     * The new tab loads the specified URL.
     *
     * @param url The URL to load in the new tab.
     * @return The newly created {@link BrowserFragment} instance representing the tab.
     *
     * TODO: The current implementation uses `replace`. For better state preservation and performance
     * with multiple tabs, this should be changed to `add` and then `show`/`hide` (or `attach`/`detach`)
     * existing fragments. The commented-out logic for `currentVisibleFragment` suggests an
     * initial attempt at this that needs to be completed.
     */
    public BrowserFragment createNewTab(String url) {
        BrowserFragment newFragment = BrowserFragment.newInstance(url);
        tabs.add(newFragment);
        currentTabIndex = tabs.size() - 1; // Set the new tab as current

        FragmentTransaction transaction = fragmentManager.beginTransaction();
        
        // TODO: Review and implement proper fragment hiding/detaching.
        // The commented code below indicates an incomplete attempt.
        // BrowserFragment currentVisibleFragment = getCurrentFragment(); // This would get the *previous* current fragment
        // if (currentVisibleFragment != null && currentVisibleFragment != newFragment && currentVisibleFragment.isAdded()) {
        //     transaction.hide(currentVisibleFragment); // Hide the previously visible fragment
        // }
        
        // If using add/show/hide:
        // transaction.add(containerId, newFragment, "tab_" + currentTabIndex);
        // transaction.show(newFragment); // Show the new fragment
        
        // Current simplified implementation: always replaces the content.
        // This is not ideal for tab management as it destroys and recreates the fragment view.
        transaction.replace(containerId, newFragment, "tab_" + currentTabIndex);
        transaction.commit();
        
        return newFragment;
    }

    /**
     * Retrieves the currently active {@link BrowserFragment}.
     *
     * @return The current BrowserFragment, or null if no tab is active or exists.
     */
    public BrowserFragment getCurrentFragment() {
        if (currentTabIndex >= 0 && currentTabIndex < tabs.size()) {
            // Try to find the fragment by its tag in the FragmentManager first.
            // This is important because the FragmentManager might reattach fragments (e.g., after config change).
            Fragment fragment = fragmentManager.findFragmentByTag("tab_" + currentTabIndex);
            if (fragment instanceof BrowserFragment) {
                return (BrowserFragment) fragment;
            }
            // Fallback to the instance stored in our list if not found in FragmentManager (should ideally not happen if managed correctly).
            return tabs.get(currentTabIndex);
        }
        return null;
    }

    /**
     * Switches to the tab at the specified index.
     * Hides the current tab and shows the tab at the given index.
     *
     * @param index The index of the tab to switch to.
     *
     * TODO: This method needs to be implemented correctly using `hide` and `show` (or `detach`/`attach`)
     * for efficient tab switching and state preservation. The current logic is a placeholder.
     */
    public void switchToTab(int index) {
        if (index >= 0 && index < tabs.size() && index != currentTabIndex) {
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            BrowserFragment currentFragment = getCurrentFragment(); // Get the currently displayed fragment

            if (currentFragment != null && currentFragment.isAdded()) {
                transaction.hide(currentFragment); // Hide the current one
            }
            
            BrowserFragment nextFragment = tabs.get(index);
            if (nextFragment.isAdded()) {
                transaction.show(nextFragment); // Show the existing one
            } else {
                // This case should ideally not be hit if fragments are added correctly and kept in the list.
                // If it means creating it for the first time, 'add' then 'show' would be appropriate.
                transaction.add(containerId, nextFragment, "tab_" + index);
                // No explicit show needed if add is used and it's the top fragment.
            }
            // TODO: Consider using attach/detach for better state management for off-screen tabs.
            transaction.commit();
            currentTabIndex = index; // Update the current tab index
        }
    }

    /**
     * Closes the tab at the specified index.
     * Removes the fragment and updates the tab list.
     * If the closed tab was the current one, it switches to an adjacent tab or creates a new one if no tabs are left.
     *
     * @param index The index of the tab to close.
     *
     * TODO: After closing a tab, if no tabs are left, the behavior is to set currentTabIndex to -1.
     * MainActivity's onResume currently handles creating a new tab if the list is empty.
     * This interaction should be clearly defined (e.g., TabManager signals MainActivity or MainActivity always checks).
     */
    public void closeTab(int index) {
        if (index >= 0 && index < tabs.size()) {
            BrowserFragment fragmentToClose = tabs.get(index);
            
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            transaction.remove(fragmentToClose);
            transaction.commit(); // Commit fragment removal
            
            tabs.remove(index); // Remove from our list

            if (tabs.isEmpty()) {
                currentTabIndex = -1;
                // MainActivity will handle creating a new tab in onResume if it finds no tabs.
            } else if (currentTabIndex == index) {
                // If the closed tab was the current tab, switch to a new one.
                // Prefer the tab to the left, or the new first tab if the first was closed.
                currentTabIndex = Math.max(0, index - 1);
                switchToTab(currentTabIndex); // Switch to the new current tab
            } else if (currentTabIndex > index) {
                // If a tab before the current one was closed, adjust the current index.
                currentTabIndex--;
            }
            // If a tab after the current one was closed, currentTabIndex remains correct.
        }
    }
    
    /**
     * Returns a list of all currently managed {@link BrowserFragment} instances.
     *
     * @return A list of all tabs.
     *         The returned list is the internal list; modifications to it could affect TabManager's state.
     *         Consider returning a copy if external modification is a concern: `new ArrayList<>(tabs)`.
     */
    public List<BrowserFragment> getAllTabs() {
        return tabs;
    }
}
