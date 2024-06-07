package com.example.mesrapp.models

import androidx.room.ColumnInfo
import androidx.room.Entity

@Entity
data class IdMatch (
    @ColumnInfo(name = "id", index = true) val id: String,
    @ColumnInfo(name = "spotiId") val spotiId: String,
    @ColumnInfo(name = "matchedSpotiId") val matchedSpotiId: String = "",
    @ColumnInfo(name = "matchedMusicId") val matchedMusicId: String = "",
    @ColumnInfo(name = "matchedArtistId") val matchedArtistId: String = "",
) {
    constructor(spotiId: String) : this(spotiId, "", "", "", "")
}
