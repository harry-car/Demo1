package com.geely.browser.db;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import com.geely.browser.model.BookmarkEntity;

/**
 * The main Room database class for the application.
 * This class defines the database configuration and serves as the main access point
 * to the persisted data. It follows a singleton pattern to ensure only one instance
 * of the database exists at a time.
 *
 * The `entities` array lists all the data entities (tables) included in this database.
 * `version` is the database version, used for schema migrations.
 * `exportSchema` is set to false to avoid exporting the schema to a JSON file,
 * which is useful for version control but not strictly necessary for this project.
 */
@Database(entities = {BookmarkEntity.class}, version = 1, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    /**
     * Abstract method to get the Data Access Object (DAO) for bookmarks.
     * Room will generate the implementation for this method.
     *
     * @return The {@link BookmarkDao} instance for accessing bookmark data.
     */
    public abstract BookmarkDao bookmarkDao();

    // Volatile INSTANCE to ensure visibility of changes across threads.
    private static volatile AppDatabase INSTANCE;

    /**
     * Gets the singleton instance of the AppDatabase.
     * Uses a synchronized block to ensure thread-safe instantiation.
     * If the INSTANCE is null, it creates a new database instance.
     *
     * @param context The application context, used to get the application's file system path for the database.
     * @return The singleton {@link AppDatabase} instance.
     */
    public static AppDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            // Synchronized block to make sure that the database instance is created only once.
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            AppDatabase.class, "browser_database")
                            // TODO: For production, consider adding migration strategies if schema changes are expected.
                            // .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                            // TODO: `allowMainThreadQueries()` should NOT be used in production.
                            // Database operations should be performed on background threads.
                            // Our BookmarkRepository uses an ExecutorService for this.
                            // .allowMainThreadQueries() 
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
