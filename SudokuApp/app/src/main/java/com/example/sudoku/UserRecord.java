package com.example.sudoku;

import android.widget.Toast;
import android.content.Context;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "userrecord")
public class UserRecord {
    @PrimaryKey(autoGenerate = true)
    public int id;
    public String userId;
    public int score;
    public String encryptedLocation;
    public long timestamp;

    public UserRecord() {}

    public UserRecord(String userId, int score, String encryptedLocation, long timestamp) {
        this.userId = userId;
        this.score = score;
        this.encryptedLocation = encryptedLocation;
        this.timestamp = timestamp;
    }
}
