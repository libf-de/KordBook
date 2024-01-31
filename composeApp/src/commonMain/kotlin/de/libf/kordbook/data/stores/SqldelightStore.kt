package de.libf.kordbook.data.stores

import de.libf.kordbook.data.extensions.toSong
import de.libf.kordbook.data.model.Song

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOneOrDefault
import app.cash.sqldelight.coroutines.mapToOneOrNull
import de.libf.kordbook.data.ChordsDatabase
import de.libf.kordbook.data.extensions.toSearchResult
import de.libf.kordbook.data.model.SearchResult
import de.libf.kordbook.data.sources.AbstractSource
import io.github.aakira.napier.Napier
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class SqldelightStore(
    nextSource: AbstractSource
) : AbstractSource(nextSource), LocalStoreInterface, KoinComponent {

    private val database: ChordsDatabase by inject()

    override suspend fun fetchSongByUrlImpl(url: String): Song? { return null }

    private val currentSongUrl = MutableStateFlow("")

    /*@OptIn(ExperimentalCoroutinesApi::class)
    override suspend fun getSongByUrl(url: String): Flow<Song> = database.songsQueries
        .selectSongByUrl(url)
        .asFlow()
        .mapToOneOrNull(Dispatchers.IO)
        .mapLatest {
            if(it?.content == null) {
                CoroutineScope(Dispatchers.IO).launch {
                    this@SqldelightStore.fetchSongByUrl(url)?.let { fetchedSong ->
                        this@SqldelightStore.storeSong(fetchedSong)
                    }
                }
            }
            it
        }
        .filterNotNull()
        .mapLatest {
            it.toSong(database)
        }*/

    override suspend fun getAndCacheSong(url: String): Song? {
        val dbSong = database.songsQueries
            .selectSongByUrl(url)
            .executeAsOneOrNull()



        //return dbSong?.toSong(database) ?: fetchSongByUrl(url)?.also { storeSong(it) }

        if(dbSong == null) {
            Napier.d { "Song not in database, fetching from network" }
            val fetchedSong = fetchSongByUrl(url)
            if(fetchedSong != null) {
                Napier.d { "Song fetched from network, storing in database" }
                storeSong(fetchedSong)
            }

            Napier.d { "Returning fetched song ${fetchedSong?.songName}"}

            return fetchedSong
        } else {
            Napier.d { "Song in database, returning ${dbSong.songName}" }
            return dbSong.toSong(database)
        }
    }

    override val NAME: String = "Favoriten"

    override suspend fun searchImpl(query: String, page: Int): List<SearchResult> {
        return database.songsQueries
            .search(query)
            .executeAsList()
            .filter { it.content != null }
            .map { it.toSearchResult() }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun searchSongsFlow(query: String): Flow<List<SearchResult>>
            = database.songsQueries
        .search(query)
        .asFlow()
        .mapToList(Dispatchers.IO)
        .distinctUntilChanged()
        .mapLatest {
            it
                .filter { dbSong -> dbSong.content != null }
                .map { dbSong -> dbSong.toSearchResult() }
        }

    override suspend fun getSearchSuggestionsImpl(query: String): List<SearchResult> {
        return database.songsQueries
            .search(query)
            .executeAsList()
            .filter { it.content != null }
            .map { it.toSearchResult() }
    }

    override fun getAllSongsFlow(): Flow<List<SearchResult>> = database.songsQueries
        .selectAllSongs()
        .asFlow()
        .mapToList(Dispatchers.IO)
        .map {
            it
                .filter { dbSong -> dbSong.content != null }
                .map { dbSong ->
                    dbSong.toSearchResult()
                }
        }

    override suspend fun storeSong(song: Song) {
        with(database.songsQueries) {
            transaction {
                song.versions.forEach {
                    insertSong(
                        songName = it.songName,
                        songId = it.songId,
                        artist = it.artist,
                        artistId = it.artistId,
                        versions = emptyList(),
                        related = emptyList(),
                        url = it.url,
                        rating = it.rating,
                        votes = it.votes,
                        version = it.version,
                        format = it.format,
                        content = it.content,
                        capo = it.capo,
                        tonality = it.tonality,
                        instrument = it.instrument
                    )
                }

                song.related.forEach {
                    insertSong(
                        songName = it.songName,
                        songId = it.songId,
                        artist = it.artist,
                        artistId = it.artistId,
                        versions = emptyList(),
                        related = emptyList(),
                        url = it.url,
                        rating = it.rating,
                        votes = it.votes,
                        version = it.version,
                        format = it.format,
                        content = it.content,
                        capo = it.capo,
                        tonality = it.tonality,
                        instrument = it.instrument
                    )
                }

                replaceSong(
                    songName = song.songName,
                    songId = song.songId,
                    artist = song.artist,
                    artistId = song.artistId,
                    versions = song.versions.map { it.url },
                    related = song.related.map { it.url },
                    url = song.url,
                    rating = song.rating,
                    votes = song.votes,
                    version = song.version,
                    format = song.format,
                    content = song.content,
                    capo = song.capo,
                    tonality = song.tonality,
                    instrument = song.instrument
                )
            }
        }
    }

    override suspend fun deleteSong(song: Song) {
        database.songsQueries.deleteSongByUrl(song.url)
    }

    override fun isSongFavorite(song: Song): Flow<Boolean> = database.songsQueries
        .getFavorite(song.url)
        .asFlow()
        .mapToOneOrDefault(false, Dispatchers.IO)

    override suspend fun toggleSongFavorite(song: Song) {
        database.songsQueries.toggleFavorite(song.url)
    }
}




