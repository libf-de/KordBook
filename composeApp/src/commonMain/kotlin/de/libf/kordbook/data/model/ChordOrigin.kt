package de.libf.kordbook.data.model

interface ChordOrigin {
    val NAME: String
    val REMOTE_SOURCE: Boolean

    suspend fun fetchSongByUrl(url: String): Chords
    suspend fun searchSongs(query: String): List<SearchResult>
}