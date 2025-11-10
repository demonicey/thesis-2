package com.example.sudoku;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import android.content.Context;

@Database(entities = {UserRecord.class}, version = 1, exportSchema = false)
public abstract class DatabaseHelper extends RoomDatabase {
    public abstract UserRecordDao userRecordDao();

    private static volatile DatabaseHelper INSTANCE;

    public static DatabaseHelper getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (DatabaseHelper.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            DatabaseHelper.class, "sudoku_database")
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
