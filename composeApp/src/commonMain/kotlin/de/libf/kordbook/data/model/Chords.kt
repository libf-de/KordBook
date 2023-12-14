package de.libf.kordbook.data.model

data class Chords(
    var url: String,
    var id: String,
    var songName: String,
    var songId: String,
    var artist: String,
    var artistId: String,
    var versions: List<Chords> = emptyList(),
    var related: List<Chords> = emptyList(),
    var origin: String,
    var rating: Double? = null,
    var votes: Double? = null,
    var version: String? = null,
    var tonality: String? = null,
    var capo: String? = null,
    var chords: String? = null,
) {
    companion object {
        val EMPTY = Chords(
            url = "",
            id = "",
            songName = "",
            songId = "",
            artist = "",
            artistId = "",
            versions = emptyList(),
            related = emptyList(),
            origin = "",
            rating = null,
            votes = null,
            version = null,
            tonality = null,
            capo = null,
            chords = null,
        )
    }

    fun ratingVotesRatio(): Double {
        return (this.votes?.let { it1 ->
            this.rating?.times(
                it1
            )
        }) ?: 0.0
    }
}

fun List<Chords>.toSearchResults(): List<SearchResult> {
    return this.map {
        SearchResult(
            id = it.id,
            songName = it.songName,
            songId = it.songId,
            artist = it.artist,
            artistId = it.artistId,
            version = it.version,
            rating = it.rating,
            votes = it.votes,
            url = it.url,
            origin = it.origin,
        )
    }
}