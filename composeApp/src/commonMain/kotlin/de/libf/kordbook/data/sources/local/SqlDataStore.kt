package de.libf.kordbook.data.sources.local

import app.cash.sqldelight.ColumnAdapter
import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOneOrDefault
import de.libf.kordbook.data.ChordsDatabase
import de.libf.kordbook.data.DbChords
import de.libf.kordbook.data.model.ChordOrigin
import de.libf.kordbook.data.model.Chords
import de.libf.kordbook.data.model.LocalChordOrigin
import de.libf.kordbook.data.model.SearchResult
import io.realm.kotlin.ext.realmListOf
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class SqlDataStore : LocalChordOrigin(), KoinComponent {
    val database: ChordsDatabase by inject()

    override fun getAllChordsFlow(): Flow<Pair<ChordOrigin, List<SearchResult>>>
    = database.chordsQueries
        .selectAll()
        .asFlow()
        .mapToList(Dispatchers.IO)
        .map {
        this to it
            .filter { dbC -> dbC.chords != null }
            .map { dbChords ->
            dbChords.toSearchResult()
        }
    }

    override suspend fun storeChords(chords: Chords) {
        database.chordsQueries.transaction {
            database.chordsQueries.insertChords(
                id = chords.id,
                songName = chords.songName,
                songId = chords.songId,
                artist = chords.artist,
                artistId = chords.artistId,
                versions = chords.versions.map { it.url },
                related = chords.related.map { it.url },
                url = chords.url,
                origin = chords.origin,
                rating = chords.rating,
                votes = chords.votes,
                version = chords.version,
                tonality = chords.tonality,
                capo = chords.capo,
                chords = chords.chords
            )

            chords.versions.forEach {
                database.chordsQueries.insertChords(
                    id = it.id,
                    songName = it.songName,
                    songId = it.songId,
                    artist = it.artist,
                    artistId = it.artistId,
                    versions = emptyList(),
                    related = emptyList(),
                    url = it.url,
                    origin = it.origin,
                    rating = it.rating,
                    votes = it.votes,
                    version = it.version,
                    tonality = it.tonality,
                    capo = it.capo,
                    chords = it.chords
                )
            }

            chords.related.forEach {
                database.chordsQueries.insertChords(
                    id = it.id,
                    songName = it.songName,
                    songId = it.songId,
                    artist = it.artist,
                    artistId = it.artistId,
                    versions = emptyList(),
                    related = emptyList(),
                    url = it.url,
                    origin = it.origin,
                    rating = it.rating,
                    votes = it.votes,
                    version = it.version,
                    tonality = it.tonality,
                    capo = it.capo,
                    chords = it.chords
                )
            }
        }
    }

    override suspend fun deleteChords(chords: Chords) {
        database.chordsQueries.transaction {
            database.chordsQueries.deleteChords(chords.url)
        }
    }

    override fun haveChords(chords: Chords): Flow<Boolean>
        = database.chordsQueries
            .haveUrl(chords.url)
            .asFlow()
            .mapToOneOrDefault(false, Dispatchers.IO)


    companion object {
        const val NAME = "Local"
        const val REMOTE_SOURCE = false

        val ListOfStringsAdapter = object : ColumnAdapter<List<String>, String> {
            override fun decode(databaseValue: String) =
                if (databaseValue.isEmpty()) {
                    listOf()
                } else {
                    databaseValue.split(",")
                }
            override fun encode(value: List<String>) = value.joinToString(separator = ",")
        }
    }

    override val NAME: String
        get() = Companion.NAME

    override val REMOTE_SOURCE: Boolean
        get() = Companion.REMOTE_SOURCE

    override suspend fun fetchSongByUrl(url: String): Chords? {
        if(database.chordsQueries.haveUrl(url).executeAsOneOrNull() != true) return null
        return database.chordsQueries
            .selectByUrl(url)
            .executeAsOneOrNull()
            .toChords()
    }

    override suspend fun fetchSongBySearchResult(searchResult: SearchResult): Chords? {
        if(database.chordsQueries.haveUrl(searchResult.url).executeAsOneOrNull() != true)
            return null
        return database.chordsQueries
            .selectByUrl(searchResult.url)
            .executeAsOneOrNull()
            .toChords()
    }

    override suspend fun searchSongs(query: String, page: Int): List<SearchResult> {
        return database.chordsQueries
            .search(query)
            .executeAsList()
            .filter { it.chords != null }
            .map { it.toSearchResult() }

    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun searchSongsFlow(query: String): Flow<Pair<ChordOrigin, List<SearchResult>>>
    = database.chordsQueries
        .search(query)
        .asFlow()
        .mapToList(Dispatchers.IO)
        .distinctUntilChanged()
        .mapLatest {
            this to it
                .filter { dbC -> dbC.chords != null }
                .map { dbChords -> dbChords.toSearchResult() }
        }

    override suspend fun getSearchSuggestions(query: String): List<SearchResult> {
        return database.chordsQueries
            .search(query)
            .executeAsList()
            .filter { it.chords != null }
            .map { it.toSearchResult() }
    }

    private fun DbChords?.toChords(): Chords? {
        if(this == null) return null

        val versions = this.versions.map {
            val relatedChord = database.chordsQueries.selectVersionsById(it).executeAsOneOrNull()
            if(relatedChord != null) {
                Chords(
                    id = relatedChord.id,
                    songName = relatedChord.songName,
                    songId = relatedChord.songId,
                    artist = relatedChord.artist,
                    artistId = relatedChord.artistId,
                    versions = realmListOf(),
                    related = realmListOf(),
                    rating = relatedChord.rating,
                    votes = relatedChord.votes,
                    version = relatedChord.version,
                    url = relatedChord.url,
                    origin = this.origin,
                )
            } else {
                null
            }
        }.filterNotNull()

        val related = this.related.map {
            val relatedChord = database.chordsQueries.selectVersionsById(it).executeAsOneOrNull()
            if(relatedChord != null) {
                Chords(
                    id = relatedChord.id,
                    songName = relatedChord.songName,
                    songId = relatedChord.songId,
                    artist = relatedChord.artist,
                    artistId = relatedChord.artistId,
                    versions = realmListOf(),
                    related = realmListOf(),
                    rating = relatedChord.rating,
                    votes = relatedChord.votes,
                    version = relatedChord.version,
                    url = relatedChord.url,
                    origin = this.origin,
                )
            } else {
                null
            }
        }.filterNotNull()

        return Chords(
            id = this.id,
            songName = this.songName,
            songId = this.songId,
            artist = this.artist,
            artistId = this.artistId,
            versions = versions,
            related = related,
            url = this.url,
            origin = this.origin,
            rating = this.rating,
            votes = this.votes,
            version = this.version,
            tonality = this.tonality,
            capo = this.capo,
            chords = this.chords
        )
    }


}

private fun DbChords.toSearchResult(): SearchResult {
    return SearchResult(
        songName = this.songName,
        songId = this.songId,
        artist = this.artist,
        artistId = this.artistId,
        version = this.version,
        rating = this.rating,
        votes = this.votes,
        id = this.id,
        url = this.url,
        origin = this.origin,
    )
}

