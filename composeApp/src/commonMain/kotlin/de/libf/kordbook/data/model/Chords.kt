package de.libf.kordbook.data.model

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import de.libf.kordbook.ui.components.ChordProViewer
import de.libf.kordbook.ui.components.ChordsFontFamily
import de.libf.kordbook.ui.components.ChordsViewerInterface
import de.libf.kordbook.ui.components.NullViewer
import de.libf.kordbook.ui.components.UltimateGuitarViewer

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
    var format: ChordFormat/* = ChordFormat.CHORDPRO*/,
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
            format = ChordFormat.NULL,
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
        scrollSpeed: Float = 1f,
        isAutoScrollEnabled: Boolean = true,
        fontSize: Int = 16,
        fontFamily: ChordsFontFamily = ChordsFontFamily.default,
        modifier: Modifier = Modifier
    ) = this.format.viewer.ChordsViewer(
        chords = this,
        transposeBy = transposeBy,
        scrollSpeed = scrollSpeed,
        isAutoScrollEnabled = isAutoScrollEnabled,
        fontSize = fontSize,
        fontFamily = fontFamily,
        modifier = modifier)
}

enum class ChordFormat(val viewer: ChordsViewerInterface) {
    CHORDPRO(ChordProViewer),
    UG(UltimateGuitarViewer),
    NULL(NullViewer)
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