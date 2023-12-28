package de.libf.kordbook.ui.screens

import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SheetState
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.libf.kordbook.data.model.Chords
import de.libf.kordbook.data.model.SearchResult
import de.libf.kordbook.data.model.toSearchResults
import de.libf.kordbook.res.MR
import de.libf.kordbook.ui.components.AutoScrollControl
import de.libf.kordbook.ui.components.ChordProViewer
import de.libf.kordbook.ui.components.ChordsFontFamily
import de.libf.kordbook.ui.components.FavoriteControl
import de.libf.kordbook.ui.components.FontSizeControl
import de.libf.kordbook.ui.components.RelatedItem
import de.libf.kordbook.ui.components.TransposeControls
import de.libf.kordbook.ui.components.VersionItem
import de.libf.kordbook.ui.viewmodel.ChordDisplayViewModel
import de.libf.kordbook.ui.viewmodel.ChordListViewModel
import dev.icerock.moko.resources.compose.fontFamilyResource
import dev.icerock.moko.resources.compose.painterResource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import moe.tlaster.precompose.flow.collectAsStateWithLifecycle
import moe.tlaster.precompose.navigation.Navigator
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChordsScreen(
    url: String,
    findBest: Boolean,
    chordFontFamily: ChordsFontFamily,
    navigator: Navigator
) {
    val viewModel: ChordDisplayViewModel = koinInject()
    val chords by viewModel.chordsToDisplay.collectAsStateWithLifecycle()
    val chordsSaved by viewModel.displayedChordsSaved.collectAsStateWithLifecycle(false)

    val toolboxExpanded = remember { mutableStateOf(false) }

    val autoScrollEnabled = remember { mutableStateOf(true) }
    val autoScrollSpeed = remember { mutableStateOf(0f) }
    val transposing = remember { mutableStateOf(0) }
    val fontSizeSp = remember { mutableStateOf(16) }
    var maxCharsPerLine by remember { mutableStateOf(9999) }
    var wrappedChords by remember { mutableStateOf(Chords.EMPTY) }

    var isLandscape by remember { mutableStateOf(false) }

    val sheetState = rememberModalBottomSheetState()
    val lazyListState = rememberLazyListState()

    LaunchedEffect(key1 = autoScrollSpeed.value, key2 = autoScrollEnabled.value) {
        if (autoScrollEnabled.value) {
            coroutineScope {
                while (isActive) {
                    delay(50)
                    lazyListState.scrollBy(autoScrollSpeed.value)
                }
            }
        }
    }


    LaunchedEffect(null) {
        viewModel.fetchChords(url, findBest)
    }

    LaunchedEffect(maxCharsPerLine, chords) {
        wrappedChords = chords.copy(
            chords = chords.chords?.ensureLineLength(maxCharsPerLine)
        )
    }

    Box(
        modifier = Modifier.fillMaxSize(),
    ) {
        MaxTextMeasurer(
            fontFamily = chordFontFamily,
            fontSize = fontSizeSp.value,
            onMeasured = { charsPerLine, landscape ->
                maxCharsPerLine = charsPerLine
                isLandscape = landscape
            }
        )

        chords.Viewer(
            lazyListState = lazyListState,
            transposeBy = transposing.value,
            fontSize = fontSizeSp.value,
            fontFamily = chordFontFamily,
            modifier = Modifier.fillMaxSize(),
        )

        /*ModalBottomSheet(
            sheetState = sheetState,
            onDismissRequest = {
                CoroutineScope(Dispatchers.IO).launch {
                    sheetState.partialExpand()
                }
            }
        ) {
            Row(
                modifier = Modifier,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { navigator.goBack() }) {
                    Icon(Icons.Rounded.ArrowBack, contentDescription = null)
                }

                Spacer(Modifier.weight(1f))
                AutoScrollControl(
                    scrollSpeed = autoScrollSpeed,
                    scrollEnabled = autoScrollEnabled,
                )

                Spacer(Modifier.weight(1f))

                FavoriteControl(
                    currentChordsSaved = chordsSaved,
                    onSaveChordsClicked = {
                        if (chordsSaved) {
                            viewModel.deleteChords(chords)
                        } else {
                            viewModel.saveChords(chords)
                        }
                    },
                )

                Spacer(Modifier.width(16.dp))

                Crossfade(targetState = toolboxExpanded.value) { expanded ->
                    IconButton(
                        onClick = {
                            toolboxExpanded.value = !toolboxExpanded.value
                        },
                    ) {
                        Icon(
                            painter = painterResource(
                                if (expanded) MR.images.expand
                                else MR.images.collapse
                            ),
                            contentDescription = "List",
                        )
                    }

                }
            }

            if(sheetState.targetValue == SheetValue.Expanded) {
                Row {
                    Spacer(Modifier.weight(1f))

                    FontSizeControl(
                        fontSize = fontSizeSp
                    )

                    Spacer(Modifier.weight(1f))

                    TransposeControls(
                        transposing = transposing
                    )

                    Spacer(Modifier.weight(1f))
                }

                LazyColumn(
                    modifier = Modifier.fillMaxWidth().padding(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val versionsList = (chords.versions + chords)
                        .toSearchResults()
                        .sortedBy { it.version?.toDoubleOrNull() }

                    val maxVotesPad = versionsList.maxOfOrNull {
                        it.votes?.toInt() ?: 0
                    }?.toString()?.length ?: 0

                    items(versionsList) { alternativeChord ->

                        VersionItem(
                            searchResult = alternativeChord,
                            selected = alternativeChord.version == chords.version,
                            fontFamily = chordFontFamily,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    if (alternativeChord.version == chords.version) return@clickable
                                    viewModel.onSearchResultSelected(alternativeChord, false)
                                },
                            maxVotesPad = maxVotesPad
                        )
                    }
                }
            }
        }*/


        if(isLandscape) {
            SideControlPane(
                onBackClicked = {
                    navigator.goBack()
                },
                onSaveChordsClicked = {

                },
                chordsSaved = chordsSaved,
                fontSizeSp = fontSizeSp,
                transposing = transposing,
                autoScrollSpeed = autoScrollSpeed,
                autoScrollEnabled = autoScrollEnabled,
                modifier = Modifier.align(Alignment.CenterEnd),
            )
        } else {
            BottomControlPane(
                onBackClicked = {
                    navigator.goBack()
                },
                onSaveChordsClicked = {
                    if (chordsSaved) {
                        viewModel.deleteChords(chords)
                    } else {
                        viewModel.saveChords(chords)
                    }
                },
                chordsSaved = chordsSaved,
                toolboxExpanded = toolboxExpanded,
                fontSizeSp = fontSizeSp,
                transposing = transposing,
                autoScrollSpeed = autoScrollSpeed,
                autoScrollEnabled = autoScrollEnabled,
                chords = chords,
                fontFamily = chordFontFamily,
                onChordSelected = { searchResult, findBest ->
                    toolboxExpanded.value = false
                    viewModel.onSearchResultSelected(searchResult, findBest)
                },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .heightIn(max = 300.dp),
            )
        }
    }
}


@Composable
fun BottomControlPane(
    toolboxExpanded: MutableState<Boolean>,
    fontSizeSp: MutableState<Int>,
    transposing: MutableState<Int>,
    autoScrollSpeed: MutableState<Float>,
    autoScrollEnabled: MutableState<Boolean>,
    chordsSaved: Boolean,
    chords: Chords,
    fontFamily: ChordsFontFamily,
    onChordSelected: (SearchResult, Boolean) -> Unit = { _, _ -> },
    onSaveChordsClicked: () -> Unit = {},
    onBackClicked: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(6.dp))
            .animateContentSize(),
    ) {
        Row {
            IconButton(onClick = onBackClicked) {
                Icon(Icons.Rounded.ArrowBack, contentDescription = null)
            }

            Spacer(Modifier.weight(1f))
            AutoScrollControl(
                scrollSpeed = autoScrollSpeed,
                scrollEnabled = autoScrollEnabled,
            )

            Spacer(Modifier.weight(1f))

            FavoriteControl(
                currentChordsSaved = chordsSaved,
                onSaveChordsClicked = onSaveChordsClicked,
            )

            Spacer(Modifier.width(16.dp))

            Crossfade(targetState = toolboxExpanded.value) { expanded ->
                IconButton(
                    onClick = {
                        toolboxExpanded.value = !toolboxExpanded.value
                    },
                ) {
                    Icon(
                        painter = painterResource(
                            if(expanded) MR.images.expand
                            else MR.images.collapse
                        ),
                        contentDescription = "List",
                    )
                }
            }
        }

        if(toolboxExpanded.value) {
            Row {
                Spacer(Modifier.weight(1f))

                FontSizeControl(
                    fontSize = fontSizeSp
                )

                Spacer(Modifier.weight(1f))

                TransposeControls(
                    transposing = transposing
                )

                Spacer(Modifier.weight(1f))
            }

            LazyColumn(
                modifier = Modifier.fillMaxWidth().padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val versionsList = (chords.versions + chords)
                    .toSearchResults()
                    .sortedBy { it.version?.toDoubleOrNull() }

                val maxVotesPad = versionsList.maxOfOrNull {
                    it.votes?.toInt() ?: 0
                }?.toString()?.length ?: 0

                items(versionsList) { alternativeChord ->
                    Row(
                        modifier = Modifier.clickable {
                            if (alternativeChord.version == chords.version) return@clickable
                            onChordSelected(alternativeChord, false)
                        }
                    ) {
                        Spacer(Modifier.weight(2f))
                        VersionItem(
                            searchResult = alternativeChord,
                            selected = alternativeChord.version == chords.version,
                            fontFamily = fontFamily,
                            modifier = Modifier,
                            maxVotesPad = maxVotesPad,
                            isLarge = true,
                            spaceOut = false
                        )
                        Spacer(Modifier.weight(2f))
                    }

                }
            }
        }
    }
}

@Composable
fun SideControlPane(
    fontSizeSp: MutableState<Int>,
    transposing: MutableState<Int>,
    autoScrollSpeed: MutableState<Float>,
    autoScrollEnabled: MutableState<Boolean>,
    chordsSaved: Boolean,
    onSaveChordsClicked: () -> Unit = {},
    onBackClicked: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(6.dp))
            .animateContentSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        IconButton(onClick = onBackClicked) {
            Icon(Icons.Rounded.ArrowBack, contentDescription = null)
        }

        FontSizeControl(
            fontSize = fontSizeSp
        )

        AutoScrollControl(
            scrollSpeed = autoScrollSpeed,
            scrollEnabled = autoScrollEnabled,
        )

        TransposeControls(
            transposing = transposing
        )

        FavoriteControl(
            currentChordsSaved = chordsSaved,
            onSaveChordsClicked = onSaveChordsClicked,
        )
    }
}

private fun String?.ensureLineLength(maxCharsPerLine: Int): String? {
    fun procLine(line: String, maxCharsPerLine: Int): String {
        if(line.length <= maxCharsPerLine) return line

        val chordsExtraChars = Regex("""\[[^\]]+\]""").findAll(line).sumOf { it.value.length }
        // Berechne den Startindex für die Suche
        val startIndex = maxCharsPerLine + chordsExtraChars

        // Suche das letzte Leerzeichen vor dem startIndex
        for (i in startIndex.coerceAtMost(line.length-1) downTo 0) {
            if (line[i] == ' ') {
                return line.substring(0, i) + "\n" + procLine(line.substring(i + 1), maxCharsPerLine)
            }
        }

        return line.substring(0, maxCharsPerLine) + "\n" + procLine(line.substring(maxCharsPerLine), maxCharsPerLine)
    }



    if (this == null) { println("[ELL] input is null"); return null }
    if (this.length <= maxCharsPerLine) { println("[ELL] ${this} is not too long ($maxCharsPerLine)"); return this }

    return this.split("\n").map { procLine(it, maxCharsPerLine) }.joinToString("\n")
}

@Composable
fun MaxTextMeasurer(
    fontFamily: ChordsFontFamily,
    fontSize: Int,
    onMeasured: (Int, Boolean) -> Unit
) {
    val textMeasurer = rememberTextMeasurer()
    val charWidthInPixels = textMeasurer.measure(
        text = " ",
        style = TextStyle(
            fontFamily = fontFamily.text,
            fontSize = fontSize.sp
        )
    ).size.width

    Box(
        Modifier.fillMaxSize().onGloballyPositioned {
            val maxChars = (it.size.width / charWidthInPixels)
            val landscape = it.size.width > it.size.height
            onMeasured(maxChars, landscape)
        }
    )
}


@Composable
fun measureTextWidth(text: String, style: TextStyle): Dp {
    val textMeasurer = rememberTextMeasurer()
    val widthInPixels = textMeasurer.measure(text, style).size.width
    return with(LocalDensity.current) { widthInPixels.toDp() }
}


fun calculateSpeedFromDelay(fontSizePx: Float, timeBetweenNewlines: Int): Float {
    // Calculate scroll speed in px/s
    val scrollSpeed = fontSizePx / timeBetweenNewlines * 1000

    // Convert to scroll speed in px/25ms
    return scrollSpeed / 10f
}

fun Float.toSingleDecimalString(): String {
    val gerundetAlsInt = (this * 10).toInt()
    val gerundetAlsFloat = gerundetAlsInt.toFloat() / 10
    return gerundetAlsFloat.toString()

}

fun Int.toTransposedString(): String {
    return when {
        this > 0 -> "+$this"
        this < 0 -> "$this"
        else -> "$this"
    }

}
