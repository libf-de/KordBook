package de.libf.kordbook.data.extensions

import app.cash.sqldelight.ColumnAdapter
import de.libf.kordbook.data.ChordsDatabase
import de.libf.kordbook.data.DbSong
import de.libf.kordbook.data.SelectSongByUrl
import de.libf.kordbook.data.model.InstrumentType
import de.libf.kordbook.data.model.SearchResult
import de.libf.kordbook.data.model.Song
import de.libf.kordbook.data.model.SongFormat
import io.realm.kotlin.ext.realmListOf

val ListOfStringsAdapter = object : ColumnAdapter<List<String>, String> {
    override fun decode(databaseValue: String) =
        if (databaseValue.isEmpty()) {
            listOf()
        } else {
            databaseValue.split(",")
        }
    override fun encode(value: List<String>) = value.joinToString(separator = ",")
}

val ChordFormatAdapter = object : ColumnAdapter<SongFormat, Long> {
    override fun decode(databaseValue: Long): SongFormat {
        return SongFormat.entries[databaseValue.toInt()]
    }

    override fun encode(value: SongFormat): Long {
        return value.ordinal.toLong()
    }
}

val InstrumentTypeAdapter = object : ColumnAdapter<InstrumentType, Long> {
    override fun decode(databaseValue: Long): InstrumentType {
        return InstrumentType.entries[databaseValue.toInt()]
    }

    override fun encode(value: InstrumentType): Long {
        return value.ordinal.toLong()
    }
}

fun DbSong.toSearchResult(): SearchResult {
    return SearchResult(
        songName = this.songName,
        songId = this.songId,
        artist = this.artist,
        artistId = this.artistId,
        version = this.version,
        rating = this.rating,
        votes = this.votes,
        url = this.url,
        favorite = this.favorite
    )
}

fun SelectSongByUrl.toSong(database: ChordsDatabase): Song {
    val versions = this.versions.mapNotNull {
        val relatedChord = database.songsQueries.selectVersionsByUrl(it).executeAsOneOrNull()
        if (relatedChord != null) {
            Song(
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
                format = SongFormat.NULL,
                instrument = relatedChord.instrument
            )
        } else {
            null
        }
    }

    val related = this.related.mapNotNull {
        val relatedChord = database.songsQueries.selectVersionsByUrl(it).executeAsOneOrNull()
        if (relatedChord != null) {
            Song(
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
                format = SongFormat.NULL,
                instrument = relatedChord.instrument
            )
        } else {
            null
        }
    }

    return Song(
        songName = this.songName,
        songId = this.songId,
        artist = this.artist,
        artistId = this.artistId,
        versions = versions,
        related = related,
        url = this.url,
        rating = this.rating,
        votes = this.votes,
        version = this.version,
        tonality = this.tonality,
        capo = this.capo,
        content = this.content,
        format = this.format,
        instrument = this.instrument
    )
}

fun DbSong.toSong(database: ChordsDatabase): Song {
    val versions = this.versions.mapNotNull {
        val relatedChord = database.songsQueries.selectVersionsByUrl(it).executeAsOneOrNull()
        if (relatedChord != null) {
            Song(
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
                format = SongFormat.NULL,
                instrument = relatedChord.instrument
            )
        } else {
            null
        }
    }

    val related = this.related.mapNotNull {
        val relatedChord = database.songsQueries.selectVersionsByUrl(it).executeAsOneOrNull()
        if (relatedChord != null) {
            Song(
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
                format = SongFormat.NULL,
                instrument = relatedChord.instrument
            )
        } else {
            null
        }
    }

    return Song(
        songName = this.songName,
        songId = this.songId,
        artist = this.artist,
        artistId = this.artistId,
        versions = versions,
        related = related,
        url = this.url,
        rating = this.rating,
        votes = this.votes,
        version = this.version,
        tonality = this.tonality,
        capo = this.capo,
        content = this.content,
        format = this.format,
        instrument = this.instrument
    )
}