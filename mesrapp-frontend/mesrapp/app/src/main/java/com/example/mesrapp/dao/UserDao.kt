package com.example.mesrapp.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.mesrapp.models.User

@Dao
interface UserDao {

    @Query("SELECT * FROM users")
    fun getAll(): List<User>

    @Insert
    fun insert(user: User)

    @Delete
    fun delete(user: User)

    @Update
    fun update(user: User)

    @Query("SELECT * FROM users WHERE spotiId = :spotiId")
    fun getUserById(spotiId: String): User

    @Query("delete from users WHERE spotiId = :spotiId")
    fun deleteUserById(spotiId: String)
}