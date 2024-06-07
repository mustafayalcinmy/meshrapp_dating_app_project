package com.example.mesrapp.models

import androidx.room.ColumnInfo
import androidx.room.Entity

@Entity
data class Match (
    @ColumnInfo(name = "spotiId", index = true) val spotiId: String,
    @ColumnInfo(name = "musicId") val musicId: String,
    @ColumnInfo(name = "artistId") val artistId: String,
) {
    constructor(spotiId: String, musicId: String)
            : this(spotiId = spotiId, musicId = musicId, artistId = "")
}