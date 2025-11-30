package com.example.sudoku;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import java.util.List;

@Dao
public interface UserRecordDao {
    @Insert
    void insert(UserRecord userRecord);

    @Query("SELECT * FROM userrecord ORDER BY encryptedTimestamp DESC")
    List<UserRecord> getAllRecords();

    @Query("SELECT * FROM userrecord WHERE encryptedUserId = :encryptedUserId ORDER BY encryptedTimestamp DESC")
    List<UserRecord> getRecordsByUser(String encryptedUserId);
}
