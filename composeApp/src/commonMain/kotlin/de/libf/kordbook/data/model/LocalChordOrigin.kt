package de.libf.kordbook.data.model

import kotlinx.coroutines.flow.Flow

interface LocalChordOrigin : ChordOrigin {
    suspend fun getAllChordsFlow(): Flow<List<SearchResult>>

}