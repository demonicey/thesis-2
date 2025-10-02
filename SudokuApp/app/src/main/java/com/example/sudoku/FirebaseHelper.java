package com.example.sudoku;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class FirebaseHelper {
    private DatabaseReference database;

    public FirebaseHelper() {
        database = FirebaseDatabase.getInstance().getReference();
    }

    public void saveUserRecord(String userId, int score, double lat, double lon) {
        UserRecord record = new UserRecord(userId, score, lat, lon, System.currentTimeMillis());
        database.child("users").child(userId).push().setValue(record);
    }

    public static class UserRecord {
        public String userId;
        public int score;
        public double latitude;
        public double longitude;
        public long timestamp;

        public UserRecord() {}

        public UserRecord(String userId, int score, double latitude, double longitude, long timestamp) {
            this.userId = userId;
            this.score = score;
            this.latitude = latitude;
            this.longitude = longitude;
            this.timestamp = timestamp;
        }
    }
}
