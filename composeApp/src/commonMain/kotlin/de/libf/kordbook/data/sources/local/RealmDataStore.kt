package de.libf.kordbook.data.sources.local

import de.libf.kordbook.data.model.ChordOrigin
import de.libf.kordbook.data.model.Chords
import de.libf.kordbook.data.model.LocalChordOrigin
import de.libf.kordbook.data.model.SearchResult
import io.realm.kotlin.Realm
import io.realm.kotlin.RealmConfiguration
import io.realm.kotlin.ext.query
import io.realm.kotlin.query.find
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RealmDataStore : LocalChordOrigin {
    val config = RealmConfiguration.create(schema = setOf(Chords::class))
    val realm: Realm = Realm.open(config)

    fun getAllChords(): List<Chords> {
        return realm.query<Chords>().find().toList()
    }

    fun getAllChordsAsChordsFlow(): Flow<List<Chords>> {
        return realm.query<Chords>().find().asFlow().map {
            it.list.toList()
        }
    }

    override suspend fun getAllChordsFlow(): Flow<List<SearchResult>> {
        return realm.query<Chords>().find().asFlow().map { result ->
            result.list.toList().map {
                SearchResult(
                    title = it.title,
                    artist = it.artist,
                    version = it.version?.toInt(),
                    rating = it.rating,
                    votes = it.votes,
                    id = it.id,
                    url = it.url
                )
            }
        }
    }

    fun searchChords(query: String): List<Chords> {
        return realm.query<Chords>("TEXT '${query}'").find().toList()
    }

    fun getChordsByUrl(url: String): Chords {
        return realm.query<Chords>("url = '${url}'").find().first()
    }

    suspend fun storeChords(chords: Chords) {
        realm.write {
            copyToRealm(chords)
        }
    }

    fun storeChordsBlocking(chords: Chords) {
        realm.writeBlocking {
            copyToRealm(chords)
        }
    }

    override val NAME: String
        get() = "Local"

    override val REMOTE_SOURCE: Boolean
        get() = false

    override suspend fun fetchSongByUrl(url: String): Chords = getChordsByUrl(url)

    override suspend fun searchSongs(query: String): List<SearchResult> = searchChords(query).map {
        SearchResult(
            title = it.title,
            artist = it.artist,
            version = it.version?.toInt(),
            rating = it.rating,
            votes = it.votes,
            id = it.id,
            url = it.url
        )
    }
}