package com.example.sudoku;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import java.util.List;

@Dao
public interface UserRecordDao {
    @Insert
    void insert(UserRecord userRecord);

    @Query("SELECT * FROM userrecord ORDER BY timestamp DESC")
    List<UserRecord> getAllRecords();

    @Query("SELECT * FROM userrecord WHERE userId = :userId ORDER BY timestamp DESC")
    List<UserRecord> getRecordsByUser(String userId);
}
