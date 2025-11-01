package com.example.khanghvse184160.data.local;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

@Dao
public interface LocalUserDao {

    @Query("SELECT * FROM local_users WHERE email = :email LIMIT 1")
    LocalUser findByEmail(String email);

    @Insert(onConflict = OnConflictStrategy.ABORT)
    long insert(LocalUser user);
}
