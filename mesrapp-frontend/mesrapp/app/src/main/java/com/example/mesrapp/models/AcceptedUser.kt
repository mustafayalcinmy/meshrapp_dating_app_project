package com.example.mesrapp.models

import androidx.room.ColumnInfo
import androidx.room.Entity

@Entity
data class AcceptedUser (
    @ColumnInfo(name = "id", index = true) val id: String,
    @ColumnInfo(name = "spotiId") val spotiId: String,
    @ColumnInfo(name = "acceptedSpotiIds") val acceptedSpotiIds: String = "",
    @ColumnInfo(name = "acceptedMusicId") val acceptedMusicId: String = "",
    @ColumnInfo(name = "acceptedArtistId") val acceptedArtistId: String = "",
) {
    constructor(spotiId: String) : this(spotiId, "", "", "", "")
}
