package com.geely.browser.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
// import androidx.annotation.Nullable; // @Nullable is not strictly needed for onCreate's Bundle
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.geely.browser.R;
import com.geely.browser.adapters.BookmarkAdapter;
import com.geely.browser.model.BookmarkEntity;
import com.geely.browser.viewmodels.BookmarksViewModel;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

/**
 * Activity for displaying and managing bookmarks.
 * Users can view their saved bookmarks, select one to open in the main browser view,
 * or delete bookmarks. This activity uses a RecyclerView to display the list of bookmarks.
 */
public class BookmarksActivity extends AppCompatActivity {

    private BookmarksViewModel bookmarksViewModel; // ViewModel for managing bookmark data
    private BookmarkAdapter adapter; // Adapter for the RecyclerView
    private RecyclerView recyclerView; // View to display the list of bookmarks
    private TextView tvNoBookmarks; // TextView displayed when no bookmarks are available

    /**
     * Key for the extra data in the Intent result when a bookmark is selected.
     * The value is the URL of the selected bookmark.
     */
    public static final String EXTRA_SELECTED_URL = "com.geely.browser.EXTRA_SELECTED_URL";

    /**
     * Initializes the activity, its ViewModel, RecyclerView, and observers.
     * Sets up the toolbar and handles the display of bookmarks or a "no bookmarks" message.
     *
     * @param savedInstanceState If the activity is being re-initialized after
     *                           previously being shut down then this Bundle contains the data it most
     *                           recently supplied in onSaveInstanceState(Bundle). Otherwise it is null.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bookmarks);

        // Setup Toolbar with a back button
        Toolbar toolbar = findViewById(R.id.toolbar_bookmarks);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true); // Show back arrow
            getSupportActionBar().setDisplayShowHomeEnabled(true); // Ensure back arrow is interactive
            // Title is set from layout (app:title) or can be set here:
            // getSupportActionBar().setTitle(R.string.title_activity_bookmarks);
        }

        // Initialize UI elements
        recyclerView = findViewById(R.id.rv_bookmarks);
        tvNoBookmarks = findViewById(R.id.tv_no_bookmarks);
        
        // Initialize the adapter with click listeners
        adapter = new BookmarkAdapter(new BookmarkAdapter.OnBookmarkClickListener() {
            /**
             * Handles a click on a bookmark item.
             * Sets the selected bookmark's URL as the result of this activity and finishes.
             * @param bookmark The clicked {@link BookmarkEntity}.
             */
            @Override
            public void onBookmarkClick(BookmarkEntity bookmark) {
                Intent resultIntent = new Intent();
                resultIntent.putExtra(EXTRA_SELECTED_URL, bookmark.url);
                setResult(Activity.RESULT_OK, resultIntent);
                finish(); // Close BookmarksActivity and return to MainActivity
            }

            /**
             * Handles a click on the delete button of a bookmark item.
             * Shows a confirmation dialog before deleting the bookmark.
             * @param bookmark The {@link BookmarkEntity} to be deleted.
             */
            @Override
            public void onDeleteClick(BookmarkEntity bookmark) {
                // Show confirmation dialog before deleting
                new MaterialAlertDialogBuilder(BookmarksActivity.this)
                    .setTitle(getString(R.string.dialog_title_delete_bookmark)) // Using string resource
                    .setMessage(getString(R.string.dialog_message_delete_bookmark, bookmark.title)) // Using formatted string resource
                    .setPositiveButton(getString(R.string.action_delete), (dialog, which) -> {
                        bookmarksViewModel.deleteBookmark(bookmark);
                        Toast.makeText(BookmarksActivity.this, R.string.feedback_bookmark_deleted, Toast.LENGTH_SHORT).show();
                    })
                    .setNegativeButton(getString(R.string.action_cancel), null) // Null listener simply dismisses the dialog
                    .show();
            }
        });
        
        // Configure RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        // Initialize ViewModel and observe bookmark data
        bookmarksViewModel = new ViewModelProvider(this).get(BookmarksViewModel.class);
        bookmarksViewModel.getAllBookmarks().observe(this, bookmarks -> {
            adapter.submitList(bookmarks); // Submit new list to ListAdapter for diffing
            // Show/hide "no bookmarks" message based on whether the list is empty
            if (bookmarks == null || bookmarks.isEmpty()) {
                tvNoBookmarks.setVisibility(View.VISIBLE);
                recyclerView.setVisibility(View.GONE);
            } else {
                tvNoBookmarks.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);
            }
        });
    }

    /**
     * Handles action bar item clicks.
     * Specifically, handles the click on the back arrow (home button) to navigate up.
     *
     * @param item The menu item that was selected.
     * @return boolean Return false to allow normal menu processing to proceed, true to consume it here.
     */
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        // Handle presses on the action bar items
        if (item.getItemId() == android.R.id.home) {
            finish(); // Finish this activity and return to the previous one (MainActivity)
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
