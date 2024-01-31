package de.libf.kordbook.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.libf.kordbook.data.model.Song

fun interface SongViewerInterface {
    @Composable
    fun SongViewer(
        chords: Song,
        transposeBy: Int,
        lazyListState: LazyListState,
        fontSize: Int,
        fontFamily: ChordsFontFamily,
        modifier: Modifier
    )
}

object NullViewer : SongViewerInterface {
    @Composable
    override fun SongViewer(
        chords: Song,
        transposeBy: Int,
        lazyListState: LazyListState,
        fontSize: Int,
        fontFamily: ChordsFontFamily,
        modifier: Modifier
    ) { }
}

val CHORD_REGEX = Regex("^([A-H]|N\\.?C\\.?)([#b])?([^/\\s-]*)(/([A-H]|N\\.?C\\.?)([#b])?)?\$")
@Composable
fun ChordBox(
    chord: String,
    transposeBy: Int,
    fontSize: Int,
    fontFamily: FontFamily? = null,
    modifier: Modifier = Modifier) {

    if(CHORD_REGEX.matches(chord)) {
        Box(
            modifier = modifier
                .background(MaterialTheme.colorScheme.tertiaryContainer, RoundedCornerShape(8.dp))
                .padding(horizontal = 2.dp, vertical = 1.dp)
        ) {
            Text(
                text = transposeChord(chord, transposeBy),
                fontSize = fontSize.sp,
                fontFamily = fontFamily,
                color = MaterialTheme.colorScheme.onTertiaryContainer,
            )
        }
    } else {
        Text(
            text = chord,
            fontSize = fontSize.sp,
            fontFamily = fontFamily,
        )
    }
}

data class ChordsFontFamily(
    val metaName: FontFamily?,
    val metaValue: FontFamily?,
    val comment: FontFamily?,
    val section: FontFamily?,
    val chord: FontFamily?,
    val text: FontFamily?,
    val title: FontFamily?,
    val subtitle: FontFamily?,
) {
    companion object {
        val default = ChordsFontFamily(
            metaName = null,
            metaValue = null,
            comment = null,
            section = null,
            chord = null,
            text = null,
            title = null,
            subtitle = null,
        )
    }
}

data class MetaBoxEnv(
    val fontSize: Int,
    val fontFamily: ChordsFontFamily,
    val modifier: Modifier = Modifier
) {
    @Composable
    fun NameValueMetaBox(
        name: String,
        value: String,
        isItalic: Boolean = false,
        modifier: Modifier = this.modifier
    ) {
        MetaBox(
            textValue = buildAnnotatedString {
                withStyle(SpanStyle(fontFamily = fontFamily.metaName)) {
                    append("$name: ")
                }
                withStyle(SpanStyle(fontFamily = fontFamily.metaValue)) {
                    append(value)
                }
            },
            isItalic = isItalic,
            modifier = modifier
        )
    }

    @Composable
    fun MetaBox(
        textValue: String,
        bgColor: Color = MaterialTheme.colorScheme.primaryContainer,
        fgColor: Color = MaterialTheme.colorScheme.onPrimaryContainer,
        fontFamily: FontFamily? = this.fontFamily.metaValue,
        isItalic: Boolean = false,
        modifier: Modifier = this.modifier) {
        Box(
            modifier = modifier
                .background(bgColor, RoundedCornerShape(8.dp))
                .padding(start = 8.dp, end = 8.dp, top = 4.dp)
        ) {
            Text(
                text = textValue,
                fontSize = fontSize.sp,
                fontStyle = if(isItalic) FontStyle.Italic else null,
                fontFamily = fontFamily,
                color = fgColor,
            )
        }
    }

    @Composable
    fun MetaBox(
        textValue: AnnotatedString,
        bgColor: Color = MaterialTheme.colorScheme.primaryContainer,
        fgColor: Color = MaterialTheme.colorScheme.onPrimaryContainer,
        fontFamily: FontFamily? = this.fontFamily.metaValue,
        isItalic: Boolean = false,
        modifier: Modifier = this.modifier) {
        Box(
            modifier = modifier
                /*.padding(start = 8.dp, end = 8.dp, top = 4.dp)*/
                .background(bgColor, RoundedCornerShape(8.dp))
        ) {
            Text(
                text = textValue,
                fontSize = fontSize.sp,
                fontStyle = if(isItalic) FontStyle.Italic else null,
                fontFamily = fontFamily,
                color = fgColor,
            )
        }
    }
}

fun transposeChord(chord: String, transposeBy: Int): String {
    if(chord.contains("/")) {
        return chord.split("/").joinToString("/") { transposeChord(it, transposeBy) }
    }
    if(transposeBy != 0) {
        print("Transposing $chord by $transposeBy to...")
    }
    val noteRegex = Regex("([A-H][b#]?)")
    val baseNote = noteRegex.find(chord)?.value ?: return chord
    val notesUS = listOf("C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B")
    val notesDE = listOf("C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "H")
    val notes = if (baseNote.startsWith("H")) notesDE else notesUS
    val index = notes.indexOf(baseNote)
    if (index == -1) {
        // Unbekannter Akkord, gebe ihn unverändert zurück
        println("unknown chord $ :(")
        return chord
    }
    val transposedIndex = (index + transposeBy + 12) % 12
    val targetChord = chord.replace(noteRegex, notes[transposedIndex])
    if(transposeBy != 0) {
        println("-> ${targetChord}")
    }
    return targetChord
}