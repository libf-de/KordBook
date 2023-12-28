package de.libf.kordbook.ui.components

import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.libf.kordbook.data.model.Chords
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

object UltimateGuitarViewer : ChordsViewerInterface {
    //val DIRECTIVES = listOf("Chrous", "Verse", "Intro", "Bridge", "Outro", "Solo", "Interlude", "Instrumental", "Pre-Chorus", "Pre-Verse", "Post-Chorus", "Post-Verse", "Pre-Intro", "Post-Intro", "Pre-Bridge", "Post-Bridge", "Pre-Outro", "Post-Outro", "Pre-Solo", "Post-Solo", "Pre-Interlude", "Post-Interlude", "Pre-Instrumental", "Post-Instrumental", "Pre-Pre-Chorus", "Post-Post-Chorus", "Pre-Pre-Verse", "Post-Post-Verse", "Pre-Pre-Intro", "Post-Post-Intro", "Pre-Pre-Bridge", "Post-Post-Bridge", "Pre-Pre-Outro", "Post-Post-Outro", "Pre-Pre-Solo", "Post-Post-Solo", "Pre-Pre-Interlude", "Post-Post-Interlude", "Pre-Pre-Instrumental", "Post-Post-Instrumental", "Pre-Pre-Pre-Chorus", "Post-Post-Post-Chorus", "Pre-Pre-Pre-Verse", "Post-Post-Post-Verse", "Pre-Pre-Pre-Intro", "Post-Post-Post-Intro", "Pre-Pre-Pre-Bridge", "Post-Post-Post-Bridge", "Pre-Pre-Pre-Outro", "Post-Post-Post-Outro", "Pre-Pre-Pre-Solo", "Post-Post-Post-Solo", "Pre-Pre-Pre-Interlude", "Post-Post-Post-Interlude", "Pre-Pre-Pre-Instrumental", "Post-Post-Post-Instrumental")

    @Composable
    override fun ChordsViewer(
        chords: Chords,
        transposeBy: Int,
        lazyListState: LazyListState,
        fontSize: Int,
        fontFamily: ChordsFontFamily,
        modifier: Modifier
    ) {
        val lines = (chords.chords ?: "")
            .replace("\r", "")
            .replace("[tab]", "")
            .replace("[/tab]", "")
            .split("\n")

        LazyColumn(
            state = lazyListState,
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

            val directiveRegex = Regex("^\\[(?<lbl>[A-Z][a-zA-Z0-9_\\- ]+)\\]")
            val segmentRegex = Regex("(\\[ch].*?\\[/ch])|([^\\[]+|\\[(?!ch]).*?\\])")

            items(lines) { line ->
                //Check if line starts with directive
                val directiveCandidate = directiveRegex.find(line)
                if(directiveCandidate != null) {
                    Row(modifier = Modifier.padding(top = 12.dp, bottom = 4.dp)) {
                        MakeMetaBox(
                            directiveCandidate.groups["lbl"]?.value ?: line,
                            fontSize = fontSize,
                            fontFamily = fontFamily,
                            modifier = Modifier
                        )
                    }
                } else {
                    Row(
                        verticalAlignment = Alignment.Bottom,
                        modifier = Modifier
                    ) {
                        // Have an offset to compensate for ChordBox's padding around text
                        // Increase by 4 for each chord box, apply to all text segments
                        var offset = 0

                        segmentRegex.findAll(line).forEach { match ->
                            // Check if match is a chord box
                            if (match.value.startsWith("[ch]")) {
                                offset -= 2
                                ChordBox(
                                    chord = match.value.removePrefix("[ch]").removeSuffix("[/ch]"),
                                    transposeBy = transposeBy,
                                    fontSize = fontSize,
                                    fontFamily = fontFamily.chord,
                                    modifier = Modifier
                                        .offset(x = offset.dp)
                                        .padding(top = 8.dp)
                                )
                                offset -= 2
                            } else {
                                Text(
                                    text = match.value,
                                    fontSize = fontSize.sp,
                                    fontFamily = fontFamily.text,
                                    softWrap = false,
                                    maxLines = 1,
                                    modifier = Modifier.offset(x = offset.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}