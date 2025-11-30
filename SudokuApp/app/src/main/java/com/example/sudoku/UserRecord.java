package com.example.sudoku;

import android.widget.Toast;
import android.content.Context;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "userrecord")
public class UserRecord {
    @PrimaryKey(autoGenerate = true)
    public int id;
    public String encryptedUserId;
    public String encryptedScore;
    public String encryptedLocation;
    public String encryptedTimestamp;

    public UserRecord() {}

    @Ignore
    public UserRecord(String encryptedUserId, String encryptedScore, String encryptedLocation, String encryptedTimestamp) {
        this.encryptedUserId = encryptedUserId;
        this.encryptedScore = encryptedScore;
        this.encryptedLocation = encryptedLocation;
        this.encryptedTimestamp = encryptedTimestamp;
    }
}
