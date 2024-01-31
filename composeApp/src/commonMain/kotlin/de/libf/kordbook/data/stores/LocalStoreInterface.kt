package de.libf.kordbook.data.stores

import de.libf.kordbook.data.model.SearchResult
import de.libf.kordbook.data.model.Song
import kotlinx.coroutines.flow.Flow

interface LocalStoreInterface {
    suspend fun getSongByUrl(url: String): Flow<Song>

    fun searchSongsFlow(query: String): Flow<List<SearchResult>>

    fun getAllSongsFlow(): Flow<List<SearchResult>>

    suspend fun storeSong(song: Song)

    suspend fun deleteSong(song: Song)

    fun isSongFavorite(song: Song): Flow<Boolean>

    suspend fun toggleSongFavorite(song: Song)
}