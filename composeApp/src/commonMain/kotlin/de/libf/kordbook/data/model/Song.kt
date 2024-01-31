package de.libf.kordbook.data.model

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import de.libf.kordbook.ui.components.ChordsFontFamily

data class Song(
    var url: String,
    var songName: String,
    var songId: String,
    var artist: String,
    var artistId: String,
    var versions: List<Song> = emptyList(),
    var related: List<Song> = emptyList(),
    var rating: Double? = null,
    var votes: Double? = null,
    var version: String? = null,
    var tonality: String? = null,
    var capo: String? = null,
    var content: String? = null,
    var format: SongFormat,
    var instrument: InstrumentType
) {
    companion object {
        val EMPTY = Song(
            url = "",
            songName = "",
            songId = "",
            artist = "",
            artistId = "",
            versions = emptyList(),
            related = emptyList(),
            rating = null,
            votes = null,
            version = null,
            tonality = null,
            capo = null,
            content = null,
            format = SongFormat.NULL,
            instrument = InstrumentType.UNDEFINED
        )
    }

    fun ratingVotesRatio(): Double {
        return (this.votes?.let { it1 ->
            this.rating?.times(
                it1
            )
        }) ?: 0.0
    }

    @Composable
    fun Viewer(
        transposeBy: Int = 0,
        lazyListState: LazyListState = rememberLazyListState(),
        /*scrollSpeed: Float = 1f,
        isAutoScrollEnabled: Boolean = true,
        isFastScrollEnabled: Boolean = false,*/
        fontSize: Int = 16,
        fontFamily: ChordsFontFamily = ChordsFontFamily.default,
        modifier: Modifier = Modifier
    ) = this.format.viewer.SongViewer(
        chords = this,
        transposeBy = transposeBy,
        lazyListState = lazyListState,
        /*scrollSpeed = scrollSpeed,
        isAutoScrollEnabled = isAutoScrollEnabled,
        isFastScrollEnabled = isFastScrollEnabled,*/
        fontSize = fontSize,
        fontFamily = fontFamily,
        modifier = modifier
    )
}


fun List<Song>.toSearchResults(): List<SearchResult> {
    return this.map {
        SearchResult(
            songName = it.songName,
            songId = it.songId,
            artist = it.artist,
            artistId = it.artistId,
            version = it.version,
            rating = it.rating,
            votes = it.votes,
            url = it.url,
        )
    }
}