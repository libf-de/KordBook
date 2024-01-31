package de.libf.kordbook.data.model

import kotlinx.coroutines.flow.Flow

interface ChordOrigin {
    val NAME: String
    val REMOTE_SOURCE: Boolean

    suspend fun fetchSongByUrl(url: String): Song?

    suspend fun fetchSongBySearchResult(searchResult: SearchResult): Song?

    suspend fun searchSongs(query: String, page: Int = 1): List<SearchResult>

    fun searchSongsFlow(query: String): Flow<Pair<ChordOrigin, List<SearchResult>>>

    suspend fun getSearchSuggestions(query: String): List<SearchResult>
}