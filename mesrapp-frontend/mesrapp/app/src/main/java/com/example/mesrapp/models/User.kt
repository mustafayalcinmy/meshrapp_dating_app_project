package com.example.mesrapp.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @ColumnInfo(name = "username", index = true) var username: String,
    @ColumnInfo(name = "image") var image: String? = null,
    @PrimaryKey @ColumnInfo(name = "spotiId", index = true) val spotiId: String,
    @ColumnInfo(name = "spotiEmail", index = true) val spotiEmail: String? = null,
    @ColumnInfo(name = "bio") var bio: String? = null,
    @ColumnInfo(name = "gender") var gender : String? = null,
    @ColumnInfo(name = "instagram") var instagram : String? = null,
    @ColumnInfo(name = "dateTime") var dateTime : String? = null,
    @ColumnInfo(name = "location") var location : String? = null,
)  {
    constructor(username: String, spotiId: String, spotiEmail: String, gender : String, dateTime: String?)
            : this(username = username, spotiId = spotiId, spotiEmail = spotiEmail, gender = gender, dateTime=dateTime,image = null, bio = null, instagram = null)

}
