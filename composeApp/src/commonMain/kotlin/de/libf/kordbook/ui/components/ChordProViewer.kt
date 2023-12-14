package de.libf.kordbook.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.libf.kordbook.data.model.Chords
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

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

val CHORD_REGEX = Regex("^([A-G]|N\\.?C\\.?)([#b])?([^/\\s-]*)(/([A-G]|N\\.?C\\.?)([#b])?)?\$")

@Composable
fun ChordProViewer(
    chords: Chords,
    transposeBy: Int = 0,
    scrollSpeed: Float = 1f,
    isAutoScrollEnabled: Boolean = true,
    fontSize: Int = 16,
    fontFamily: ChordsFontFamily = ChordsFontFamily.default,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    val lcstate = rememberLazyListState()
    val lines = (chords.chords ?: "").split("\n")

    LaunchedEffect(key1 = scrollSpeed, key2 = isAutoScrollEnabled) {
        if (isAutoScrollEnabled) {
            coroutineScope {
                while (isActive) {
                    delay(50) // Kleine Verzögerung für das kontinuierliche Scrollen
                    //scrollState.scrollTo(scrollState.value + (scrollSpeed).toInt())
                    lcstate.scrollBy(scrollSpeed)
                }
            }
        }
    }

    LaunchedEffect(transposeBy) {
        println("Transposing (Composable) changed to $transposeBy")
    }

    // Get first visible item index from lazycolumn


    LazyColumn(
        state = lcstate,
        modifier = modifier
    ) {
        item {
            Text(
                text = chords.artist,
                style = MaterialTheme.typography.headlineSmall,
                fontFamily = fontFamily.subtitle,
                modifier = Modifier.padding(top = 32.dp)
            )
        }
        item {
            Text(
                text = chords.songName,
                style = MaterialTheme.typography.headlineLarge,
                fontFamily = fontFamily.title,
                modifier = Modifier.padding(top = 0.dp)
            )
        }

        items(lines) { line ->
            if(line.isDirective()) {
                Row(modifier = Modifier.padding(bottom = 8.dp)) {
                    MakeMetaBox(
                        line,
                        fontSize = fontSize,
                        fontFamily = fontFamily,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            } else {
                Row(
                    verticalAlignment = Alignment.Bottom,
                    modifier = Modifier
                ) {
                    parseChordProLineMapping(line, transposeBy).forEach { (chord, text) ->
                        Column(
                            (if(chord != null)
                                Modifier.padding(top = 8.dp)
                            else
                                Modifier)
                        ) {
                            if(chord != null) {
                                ChordBox(
                                    chord = chord,
                                    transposeBy = transposeBy,
                                    fontSize = fontSize,
                                    fontFamily = fontFamily.chord,
                                    modifier = Modifier.padding(end = 4.dp)
                                )
                            }

                            Text(
                                text = text,
                                fontSize = fontSize.sp,
                                fontFamily = fontFamily.text,
                                softWrap = false,
                                maxLines = 1,
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun String.isDirective(): Boolean {
    return this.matches("""\{.*\}""".toRegex())
}

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

@Composable
private fun MakeMetaBox(
    line: String,
    fontSize: Int,
    fontFamily: ChordsFontFamily,
    modifier: Modifier = Modifier) {
    with(MetaBoxEnv(fontSize, fontFamily, modifier)) {
        //parse with regex
        var directive = Regex("\\{(?<name>[A-Za-z0-9\\-_]+)(:\\s*(?<value>.*))?\\}").find(line)

        //if(directive == null)
        //    directive = Regex("\\{(?<name>[A-Za-z0-9]+)\\}").find(line)

        if(directive == null) {
            println("unknown directive: \"$line\"")
            // return red text
            return MetaBox(
                line,
                fgColor = MaterialTheme.colorScheme.onErrorContainer,
                bgColor = MaterialTheme.colorScheme.errorContainer,
            )
        }

        val value = directive.groups["value"]?.value

        return with(fontFamily) {
            when(directive.groups["name"]?.value) {
                "capo" -> NameValueMetaBox(
                    name = "Capo",
                    value = value ?: "???"
                )

                "c",
                "comment",
                "comment_italic",
                "ci",
                "comment_box",
                "cb" -> {
                    if(value != null)
                        MetaBox(
                            value,
                            isItalic = true,
                            fontFamily = fontFamily.comment,
                            bgColor = MaterialTheme.colorScheme.primaryContainer,
                            fgColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                }

                "image" -> Text(
                    "(Images not yet supported: $value)",
                )

                "highlight",
                "start_of_chorus",
                "soc",
                "chorus",
                "start_of_verse",
                "sov",
                "start_of_bridge",
                "sob",
                "start_of_tab",
                "sot",
                "start_of_grid",
                "sog" -> {
                    val text = value ?: when(directive.groups["name"]?.value) {
                        "start_of_chorus",
                        "soc",
                        "chorus" -> "Chorus"
                        "start_of_verse",
                        "sov" -> "Verse"
                        "start_of_bridge",
                        "sob" -> "Bridge"
                        "start_of_tab",
                        "sot" -> "Tab"
                        "start_of_grid",
                        "sog" -> "Grid"
                        else -> "???"
                    }
                    MetaBox(
                        text,
                        fontFamily = fontFamily.section
                    )
                }

                "end_of_chorus",
                "eoc",
                "end_of_verse",
                "eov",
                "end_of_bridge",
                "eob",
                "end_of_tab",
                "eot",
                "end_of_grid",
                "eog" -> { }

                else -> NameValueMetaBox(
                    name = directive.groups["name"]?.value ?: "",
                    value = directive.groups["value"]?.value ?: ""
                )
            }
        }
    }
}

private data class MetaBoxEnv(
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




private fun parseChordProLineMapping(line: String, transposeBy: Int): List<Pair<String?, String>> {
    val musicSegments = mutableListOf<Pair<String?, String>>()
    var lastChord: String? = null
    var lastIndex = 0

    val regex = """\[(.*?)\]""".toRegex()
    regex.findAll(line).forEach { match ->
        val nextChord = match.groupValues[1]
        val text = line.substring(lastIndex, match.range.first)
        musicSegments.add(lastChord to text)
        lastChord = nextChord
        lastIndex = match.range.last + 1
    }

    musicSegments.add(lastChord to line.substring(lastIndex))

    return musicSegments
}

private fun parseChordProLine(line: String, transposeBy: Int): Pair<List<String>, List<String>> {
    val chords = mutableListOf<String>()
    val lyricsSegments = mutableListOf<String>()
    var lastIndex = 0

    val regex = """\[(.*?)\]""".toRegex()
    regex.findAll(line).forEach { match ->
        val chord = transposeChord(match.groupValues[1], transposeBy)
        chords.add(chord)
        val startIndex = match.range.first
        lyricsSegments.add(line.substring(lastIndex, startIndex))
        lastIndex = match.range.last + 1
    }
    lyricsSegments.add(line.substring(lastIndex))

    return chords to lyricsSegments
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


