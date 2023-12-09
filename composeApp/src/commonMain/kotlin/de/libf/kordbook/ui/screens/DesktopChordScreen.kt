package de.libf.kordbook.ui.screens

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.compose.material3.Divider
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SearchBar
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.libf.kordbook.data.model.ChordOrigin
import de.libf.kordbook.data.model.Chords
import de.libf.kordbook.data.model.SearchResult
import de.libf.kordbook.res.MR
import de.libf.kordbook.ui.components.ChordProViewer
import de.libf.kordbook.ui.components.ChordsFontFamily
import de.libf.kordbook.ui.components.SongItem
import de.libf.kordbook.ui.components.VersionItem
import de.libf.kordbook.ui.viewmodel.DesktopScreenViewModel
import dev.icerock.moko.resources.compose.fontFamilyResource
import dev.icerock.moko.resources.compose.painterResource
import kotlinx.coroutines.delay
import kotlinx.datetime.Clock
import moe.tlaster.precompose.flow.collectAsStateWithLifecycle
import moe.tlaster.precompose.navigation.Navigator
import org.koin.compose.koinInject


@Composable
fun DesktopChordScreen(
    navigator: Navigator,
    modifier: Modifier = Modifier,
) {
    val viewModel = koinInject<DesktopScreenViewModel>()

    val sidebarWidth = 400.dp

    val chordFontFamily = ChordsFontFamily(
        metaName = fontFamilyResource(MR.fonts.MartianMono.bold),
        metaValue = fontFamilyResource(MR.fonts.MartianMono.medium),
        comment = fontFamilyResource(MR.fonts.MartianMono.light),
        section = fontFamilyResource(MR.fonts.MartianMono.medium),
        chord = fontFamilyResource(MR.fonts.MartianMono.bold),
        text = fontFamilyResource(MR.fonts.MartianMono.regular),
        ui = fontFamilyResource(MR.fonts.MartianMono.light)
    )

    //val chordList by viewModel.chordList.collectAsStateWithLifecycle(emptyMap())
    val chordList by viewModel.chordList.collectAsStateWithLifecycle()
    val chords by viewModel.chordsToDisplay.collectAsStateWithLifecycle()
    val chordsLoaded by viewModel.chordsLoaded.collectAsStateWithLifecycle()
    val chordsSaved by viewModel.displayedChordsSaved.collectAsStateWithLifecycle(false)

    val autoScrollEnabled = remember { mutableStateOf(false) }
    val autoScrollSpeed = remember { mutableStateOf(1f) }
    val transposing = remember { mutableStateOf(0) }
    val fontSizeSp = remember { mutableStateOf(16) }

    val alpha: Float by animateFloatAsState(if (true) 1f else 0f)

    LaunchedEffect(chordsSaved) {
        println("Chords saved changed to $chordsSaved")
    }


    // Two column layout
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        ChordList(
            modifier = Modifier.width(sidebarWidth).fillMaxHeight(),
            chordList = chordList,
            fontFamily = chordFontFamily,
            onChordSelected = viewModel::onSearchResultSelected, /* If we searched for chords, find the best version */
            onQueryChanged = { },
            onSearch = { viewModel.setSearchQuery(it); true },
        )

        Divider(modifier.width(1.dp).fillMaxHeight())

        ChordView(
            chords = chords,
            isAutoScrollEnabled = autoScrollEnabled.value,
            scrollSpeed = autoScrollSpeed.value,
            transposeBy = transposing.value,
            fontSize = fontSizeSp.value,
            fontFamily = chordFontFamily,
            modifier = Modifier
                .alpha(alpha)
                .padding(horizontal = 24.dp)
                .fillMaxHeight()
                .weight(1f),
        )

        ChordViewSidebar(
            chords = chords,
            fontSize = fontSizeSp,
            scrollEnabled = autoScrollEnabled,
            scrollSpeed = autoScrollSpeed,
            transposing = transposing,
            modifier = Modifier.alpha(alpha).width(sidebarWidth).fillMaxHeight(),
            onChordSelected = { viewModel.onSearchResultSelected(it, false) },
            onSaveChordsClicked = {
                if (chordsSaved) {
                    viewModel.deleteChords(chords)
                } else {
                    viewModel.saveChords(chords)
                }
            },
            fontFamily = chordFontFamily,
            currentChordsSaved = chordsSaved
        )


    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChordList(
    modifier: Modifier,
    chordList: Map<ChordOrigin, List<SearchResult>>,
    onChordSelected: (selected: SearchResult, fromSearch: Boolean) -> Unit,
    onSearch: (String) -> Boolean,
    onQueryChanged: (String) -> Unit,
    suggestions: List<SearchResult> = emptyList(),
    fontFamily: ChordsFontFamily = ChordsFontFamily.default,
) {
    var query by remember { mutableStateOf("") }
    var searchActive by remember { mutableStateOf(false) }
    val searchPadding: Int by animateIntAsState(if (searchActive) 0 else 12)


    LaunchedEffect(key1 = query) {
        delay(200)
        onQueryChanged(query)
    }

    Column(modifier = modifier) {
        SearchBar(
            query = query,
            onQueryChange = { query = it },
            active = searchActive,
            onActiveChange = { searchActive = it },
            onSearch = { if (onSearch(it)) searchActive = false },
            modifier = Modifier.padding(horizontal = searchPadding.dp).fillMaxWidth(),
        ) {
            Text("Suggestions here!")
        }
        LazyColumn(modifier = Modifier) {
            chordList.mapValues {
                it.value.groupBy { it.songId }
            }.forEach {
                item {
                    Column {
                        Text("Provider: ${it.key.NAME}")
                        Divider()
                    }


                }
                items(it.value.values.toList()) { versionList ->
                    val ratingSortedList = versionList.sortedBy { it.ratingVotesRatio() }.reversed()
                    SongItem(
                        ratingSortedList,
                        onChordsSelected = { chords ->
                            onChordSelected(
                                chords,
                                query.isNotEmpty()
                            )
                        },
                        fontFamily = fontFamily
                    )
                }
            }
        }
    }

}

@Composable
fun ChordView(
    chords: Chords,
    fontFamily: ChordsFontFamily = ChordsFontFamily.default,
    transposeBy: Int = 0,
    isAutoScrollEnabled: Boolean = false,
    scrollSpeed: Float = 1f,
    fontSize: Int = 16,
    modifier: Modifier = Modifier,
) {
    Row(modifier = modifier) {
        ChordProViewer(
            chordProText = chords.chords ?: "",
            transposeBy = transposeBy,
            isAutoScrollEnabled = isAutoScrollEnabled,
            scrollSpeed = scrollSpeed,
            fontSize = fontSize,
            fontFamily = fontFamily,
            modifier = Modifier.fillMaxWidth().fillMaxHeight(),
        )
    }
}

@Composable
fun ChordViewSidebar(
    chords: Chords,
    fontSize: MutableState<Int>,
    scrollSpeed: MutableState<Float>,
    scrollEnabled: MutableState<Boolean>,
    transposing: MutableState<Int>,
    modifier: Modifier,
    currentChordsSaved: Boolean?,
    onChordSelected: (SearchResult) -> Unit = {},
    onSaveChordsClicked: () -> Unit = {},
    fontFamily: ChordsFontFamily = ChordsFontFamily.default,
) {
    Column(modifier = modifier.fillMaxWidth().fillMaxHeight()) {
        ElevatedCard(modifier = Modifier.padding(8.dp).fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(16.dp),
            ) {
                Row {
                    Text(
                        chords.songName,
                        style = MaterialTheme.typography.headlineSmall,
                        softWrap = false,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )

                    if (currentChordsSaved != null) {
                        Crossfade(targetState = currentChordsSaved) { saved ->
                            Icon(
                                imageVector = if (saved) Icons.Rounded.Favorite
                                else Icons.Rounded.FavoriteBorder,
                                contentDescription = "List",
                                modifier = Modifier.size(16.dp).clickable {
                                    onSaveChordsClicked()
                                }
                            )
                        }
                    }

                }
                Text(
                    "von ${chords.artist}",
                    softWrap = false,
                    overflow = TextOverflow.Ellipsis,
                )

                Text("Version ${chords.version}")

                SidebarControls(
                    fontSize = fontSize,
                    scrollSpeed = scrollSpeed,
                    scrollEnabled = scrollEnabled,
                    transposing = transposing
                )


                Divider()

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
                        val mod: Modifier
                        val fontWeight: FontWeight?

                        VersionItem(
                            searchResult = alternativeChord,
                            selected = alternativeChord.version == chords.version,
                            fontFamily = fontFamily,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    if (alternativeChord.version == chords.version) return@clickable
                                    onChordSelected(alternativeChord)
                                },
                            maxVotesPad = maxVotesPad
                        )
                    }
                }

            }
        }
    }
}

@Composable
private fun SidebarControls(
    fontSize: MutableState<Int>,
    scrollSpeed: MutableState<Float>,
    scrollEnabled: MutableState<Boolean>,
    transposing: MutableState<Int>,
    modifier: Modifier = Modifier
) {
    var lastAutoscrollClick by remember { mutableStateOf(0L) }
    val fontSizePx = with(LocalDensity.current) { fontSize.value.sp.toPx() }

    Column(modifier = modifier) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { fontSize.value += 2 }) {
                Icon(
                    Icons.Rounded.KeyboardArrowUp,
                    contentDescription = "List"
                )
            }
            Text(
                text = fontSize.value.toString(),
            )
            IconButton(onClick = { fontSize.value -= 2 }) {
                Icon(
                    Icons.Rounded.KeyboardArrowDown,
                    contentDescription = "List"
                )
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { scrollSpeed.value += 0.1f }) {
                Icon(
                    Icons.Rounded.KeyboardArrowUp,
                    contentDescription = "List"
                )
            }
            Text(
                text = scrollSpeed.value.toSingleDecimalString(),
                modifier = Modifier.clickable {
                    if (lastAutoscrollClick == 0L) {
                        lastAutoscrollClick = Clock.System.now().toEpochMilliseconds()
                        scrollEnabled.value = false
                    } else {
                        val timeBetweenScrolls =
                            Clock.System.now().toEpochMilliseconds() - lastAutoscrollClick
                        scrollSpeed.value =
                            calculateSpeedFromDelay(fontSizePx, timeBetweenScrolls.toInt())
                        lastAutoscrollClick = 0
                        scrollEnabled.value = true
                    }
                }
            )
            IconButton(onClick = { scrollSpeed.value -= 0.1f }) {
                Icon(
                    Icons.Rounded.KeyboardArrowDown,
                    contentDescription = "List"
                )
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { transposing.value++ }) {
                Icon(
                    painterResource(MR.images.transpose_up),
                    contentDescription = "List"
                )
            }
            Text(
                text = transposing.value.toTransposedString(),
            )
            IconButton(onClick = { transposing.value-- }) {
                Icon(
                    painterResource(MR.images.transpose_down),
                    contentDescription = "List"
                )
            }
        }
    }
}

private fun List<Chords>.toSearchResults(): List<SearchResult> {
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
