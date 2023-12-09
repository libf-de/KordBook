package de.libf.kordbook.data.model

import kotlinx.coroutines.flow.Flow

abstract class LocalChordOrigin : ChordOrigin {
    companion object {
        const val NAME = "Local"
    }

    override val NAME = Companion.NAME
    abstract fun getAllChordsFlow(): Flow<Pair<ChordOrigin, List<SearchResult>>>

    abstract suspend fun storeChords(chords: Chords)

    abstract suspend fun deleteChords(chords: Chords)

    abstract fun haveChords(chords: Chords): Flow<Boolean>
}