package com.geely.browser.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView; // Added for potential favicon
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;
import com.geely.browser.R;
import com.geely.browser.model.BookmarkEntity;

/**
 * RecyclerView Adapter for displaying a list of {@link BookmarkEntity} items.
 * Uses {@link ListAdapter} with {@link DiffUtil} for efficient list updates.
 * Handles item clicks for opening a bookmark and clicks on a delete button for removing a bookmark.
 */
public class BookmarkAdapter extends ListAdapter<BookmarkEntity, BookmarkAdapter.BookmarkViewHolder> {

    private final OnBookmarkClickListener listener; // Listener for item click events

    /**
     * Constructor for BookmarkAdapter.
     *
     * @param listener The callback that will handle item click events.
     */
    public BookmarkAdapter(OnBookmarkClickListener listener) {
        super(DIFF_CALLBACK);
        this.listener = listener;
    }

    /**
     * DiffUtil.ItemCallback for calculating the difference between two non-null items in a list.
     * Used by ListAdapter to efficiently update the RecyclerView.
     */
    private static final DiffUtil.ItemCallback<BookmarkEntity> DIFF_CALLBACK = new DiffUtil.ItemCallback<BookmarkEntity>() {
        /**
         * Called to check whether two objects represent the same item.
         * For example, if your items have unique IDs, this method should check their id equality.
         */
        @Override
        public boolean areItemsTheSame(@NonNull BookmarkEntity oldItem, @NonNull BookmarkEntity newItem) {
            return oldItem.id == newItem.id; // Compare by unique ID
        }

        /**
         * Called to check whether two items have the same data.
         * This information is used to detect if the contents of an item have changed.
         */
        @Override
        public boolean areContentsTheSame(@NonNull BookmarkEntity oldItem, @NonNull BookmarkEntity newItem) {
            // Compare relevant content fields
            return oldItem.url.equals(newItem.url) &&
                   oldItem.title.equals(newItem.title) &&
                   oldItem.timestamp == newItem.timestamp;
        }
    };

    /**
     * Called when RecyclerView needs a new {@link BookmarkViewHolder} of the given type to represent an item.
     *
     * @param parent The ViewGroup into which the new View will be added after it is bound to an adapter position.
     * @param viewType The view type of the new View.
     * @return A new BookmarkViewHolder that holds a View of the given view type.
     */
    @NonNull
    @Override
    public BookmarkViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.bookmark_list_item, parent, false);
        return new BookmarkViewHolder(itemView);
    }

    /**
     * Called by RecyclerView to display the data at the specified position.
     * This method should update the contents of the {@link BookmarkViewHolder#itemView} to reflect the item at the given position.
     *
     * @param holder The BookmarkViewHolder which should be updated to represent the contents of the item at the given position in the data set.
     * @param position The position of the item within the adapter's data set.
     */
    @Override
    public void onBindViewHolder(@NonNull BookmarkViewHolder holder, int position) {
        BookmarkEntity currentBookmark = getItem(position);
        holder.tvTitle.setText(currentBookmark.title);
        holder.tvUrl.setText(currentBookmark.url);
        // TODO: Implement favicon loading for ivFavicon if favicons are stored or fetched.
        // Example: holder.ivFavicon.setImageResource(R.drawable.ic_default_favicon); // Placeholder
    }

    /**
     * ViewHolder class for bookmark items.
     * Holds references to the UI views for a single bookmark item and sets up click listeners.
     */
    class BookmarkViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvTitle;
        private final TextView tvUrl;
        // private final ImageView ivFavicon; // Uncomment if favicon ImageView is used
        private final ImageButton btnDeleteBookmark;

        /**
         * Constructor for the BookmarkViewHolder.
         *
         * @param itemView The View for a single bookmark item.
         */
        public BookmarkViewHolder(View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tv_bookmark_title);
            tvUrl = itemView.findViewById(R.id.tv_bookmark_url);
            // ivFavicon = itemView.findViewById(R.id.iv_bookmark_favicon); // Uncomment if used
            btnDeleteBookmark = itemView.findViewById(R.id.btn_delete_bookmark);

            // Set listener for the entire item view click
            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                // Ensure position is valid and listener is set
                if (listener != null && position != RecyclerView.NO_POSITION) {
                    listener.onBookmarkClick(getItem(position));
                }
            });
            
            // Set listener for the delete button click
            btnDeleteBookmark.setOnClickListener(v -> {
                 int position = getAdapterPosition();
                // Ensure position is valid and listener is set
                if (listener != null && position != RecyclerView.NO_POSITION) {
                    listener.onDeleteClick(getItem(position));
                }
            });
        }
    }

    /**
     * Interface for handling click events on bookmark items.
     */
    public interface OnBookmarkClickListener {
        /**
         * Called when a bookmark item is clicked.
         *
         * @param bookmark The {@link BookmarkEntity} that was clicked.
         */
        void onBookmarkClick(BookmarkEntity bookmark);

        /**
         * Called when the delete button for a bookmark item is clicked.
         *
         * @param bookmark The {@link BookmarkEntity} whose delete button was clicked.
         */
        void onDeleteClick(BookmarkEntity bookmark);
    }
}
