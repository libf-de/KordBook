package de.libf.kordbook.data.sources.local

import de.libf.kordbook.data.model.ChordFormat
import de.libf.kordbook.data.model.ChordOrigin
import de.libf.kordbook.data.model.Chords
import de.libf.kordbook.data.model.LocalChordOrigin
import de.libf.kordbook.data.model.RealmChords
import de.libf.kordbook.data.model.SearchResult
import io.realm.kotlin.Realm
import io.realm.kotlin.RealmConfiguration
import io.realm.kotlin.ext.query
import io.realm.kotlin.ext.toRealmList
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

abstract class RealmDataStore : LocalChordOrigin() {

    val realm: Realm

    init {
        val config = RealmConfiguration.create(schema = setOf(RealmChords::class))
        realm = Realm.open(config)
    }

    fun getAllChords(): List<Chords> {
        return realm.query<RealmChords>().find().toList().map {
            it.toChords()
        }
    }

    fun getAllChordsAsChordsFlow(): Flow<List<Chords>> {
        return realm.query<RealmChords>().find().asFlow().map {
            it.list.toList().map {
                it.toChords()
            }
        }
    }

    override fun getAllChordsFlow(): Flow<Pair<ChordOrigin, List<SearchResult>>> {
        return realm.query<RealmChords>().find().asFlow().map { result ->
            this to result.list.toList().map {
                SearchResult(
                    songName = it.title,
                    songId = it.title,
                    artist = it.artist,
                    artistId = it.artist,
                    version = it.version,
                    rating = it.rating,
                    votes = it.votes,
                    id = it.id,
                    url = it.url,
                    origin = this.NAME
                )
            }
        }
    }

    fun searchChords(query: String): List<Chords> {
        return realm.query<RealmChords>("TEXT '${query}'").find().toList().map {
            it.toChords()
        }
    }

    override suspend fun storeChords(chords: Chords) {
        realm.write {
            try {
                copyToRealm(chords.toRealmChords())
            } catch(ex: Exception) {
                ex.printStackTrace()
                this.cancelWrite()
            }

        }
    }

    fun storeChordsBlocking(chords: Chords) {
        realm.writeBlocking {
            copyToRealm(chords.toRealmChords())
        }
    }

    companion object {
        const val NAME = "Local"
        const val REMOTE_SOURCE = false
    }

    override val NAME: String
        get() = Companion.NAME

    override val REMOTE_SOURCE: Boolean
        get() = Companion.REMOTE_SOURCE

    override suspend fun fetchSongByUrl(url: String): Chords? {
        return try {
            realm.query<RealmChords>("url = '${url}'").find().first().toChords()
        } catch(ex: NoSuchElementException) {
            null
        }
    }

    override suspend fun fetchSongBySearchResult(searchResult: SearchResult): Chords? {
        return try {
            realm.query<RealmChords>("url = '${searchResult.url}'").find().first().toChords()
        } catch(ex: NoSuchElementException) {
            null
        }
    }

    override suspend fun searchSongs(query: String, page: Int): List<SearchResult> = searchChords(query).map {
        SearchResult(
            songName = it.songName,
            songId = it.songId,
            artist = it.artist,
            artistId = it.artistId,
            version = it.version,
            rating = it.rating,
            votes = it.votes,
            id = it.id,
            url = it.url,
            origin = this.NAME
        )
    }

    override fun searchSongsFlow(query: String): Flow<Pair<ChordOrigin, List<SearchResult>>> {
        val dbQuery = if(query.isBlank())
            realm.query<RealmChords>()
        else
            realm.query<RealmChords>("(title TEXT '${query}') OR (artist TEXT '${query}')")

        return dbQuery.find().asFlow().map { result ->
            this to result.list.toList().map {
                SearchResult(
                    songName = it.title,
                    songId = it.title,
                    artist = it.artist,
                    artistId = it.artist,
                    version = it.version,
                    rating = it.rating,
                    votes = it.votes,
                    id = it.id,
                    url = it.url,
                    origin = this.NAME
                )
            }
        }
    }
}

private fun RealmChords.toChords(): Chords {
    return Chords(
        url = this.url,
        id = this.id,
        songName = this.title,
        songId = this.title,
        artist = this.artist,
        artistId = this.artist,
        versions = this.versions.map { it.toChords() },
        related = this.related.map { it.toChords() },
        origin = this.origin,
        rating = this.rating,
        votes = this.votes,
        version = this.version,
        tonality = this.tonality,
        capo = this.capo,
        chords = this.chords,
        format = ChordFormat.NULL
    )

}

private fun Chords.toRealmChords(): RealmChords {
    return RealmChords(
        url = this.url,
        id = this.id,
        title = this.songName,
        artist = this.artist,
        versions = this.versions.map { it.toRealmChords() }.toRealmList(),
        related = this.related.map { it.toRealmChords() }.toRealmList(),
        origin = this.origin,
        rating = this.rating,
        votes = this.votes,
        version = this.version,
        tonality = this.tonality,
        capo = this.capo,
        chords = this.chords,
    )

}
