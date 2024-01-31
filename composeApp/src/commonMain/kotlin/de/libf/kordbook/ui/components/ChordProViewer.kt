package de.libf.kordbook.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.libf.kordbook.data.model.Song

object ChordProViewer : SongViewerInterface {
    @Composable
    override fun SongViewer(
        chords: Song,
        transposeBy: Int,
        lazyListState: LazyListState,
        fontSize: Int,
        fontFamily: ChordsFontFamily,
        modifier: Modifier
    ) {
        val scrollState = rememberScrollState()
        val lcstate = rememberLazyListState()
        val lines = (chords.content ?: "").split("\n")

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
}

@Composable
fun MakeMetaBox(
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

private fun String.isDirective(): Boolean {
    return this.matches("""\{.*\}""".toRegex())
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


