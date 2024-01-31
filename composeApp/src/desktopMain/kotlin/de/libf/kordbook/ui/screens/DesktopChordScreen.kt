package de.libf.kordbook.ui.screens

import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material3.Divider
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.libf.kordbook.data.model.Song
import de.libf.kordbook.data.model.SearchResult
import de.libf.kordbook.data.model.toSearchResults
import de.libf.kordbook.data.tools.KeyEventDispatcher
import de.libf.kordbook.res.MR
import de.libf.kordbook.ui.components.AutoScrollControl
import de.libf.kordbook.ui.components.ChordList
import de.libf.kordbook.ui.components.ChordsFontFamily
import de.libf.kordbook.ui.components.FavoriteControl
import de.libf.kordbook.ui.components.FontSizeControl
import de.libf.kordbook.ui.components.RelatedItem
import de.libf.kordbook.ui.components.TransposeControls
import de.libf.kordbook.ui.components.VersionItem
import de.libf.kordbook.ui.viewmodel.DesktopScreenViewModel
import dev.icerock.moko.resources.compose.fontFamilyResource
import dev.icerock.moko.resources.compose.painterResource
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.datetime.Clock
import moe.tlaster.precompose.flow.collectAsStateWithLifecycle
import org.koin.compose.koinInject


@Composable
fun DesktopChordScreen(
    keyEventDispatcher: KeyEventDispatcher,
    modifier: Modifier = Modifier,
) {
    val viewModel = koinInject<DesktopScreenViewModel>()

    val sidebarWidth = 300.dp

    val chordFontFamily = ChordsFontFamily(
        metaName = fontFamilyResource(MR.fonts.MartianMono.bold),
        metaValue = fontFamilyResource(MR.fonts.MartianMono.medium),
        comment = fontFamilyResource(MR.fonts.MartianMono.light),
        section = fontFamilyResource(MR.fonts.MartianMono.medium),
        chord = fontFamilyResource(MR.fonts.MartianMono.bold),
        text = fontFamilyResource(MR.fonts.MartianMono.regular),
        title = fontFamilyResource(MR.fonts.MartianMono.bold),
        subtitle = fontFamilyResource(MR.fonts.MartianMono.medium),
    )

    //val chordList by viewModel.chordList.collectAsStateWithLifecycle(emptyMap())
    val chordList by viewModel.chordList.collectAsStateWithLifecycle()
    val searchSuggestions by viewModel.searchSuggestions.collectAsStateWithLifecycle()
    val listLoading by viewModel.listLoading.collectAsStateWithLifecycle()

    val song by viewModel.chordsToDisplay.collectAsStateWithLifecycle()
    val songSaved by viewModel.displayedChordsSaved.collectAsStateWithLifecycle(false)

    val autoScrollEnabled = remember { mutableStateOf(true) }
    val autoScrollSpeed = remember { mutableStateOf(0f) }
    var fastScrollState by remember { mutableStateOf(0) }
    var lastScrollSpeed by remember { mutableStateOf(1f) }

    val lazyListState = rememberLazyListState()
    val transposing = remember { mutableStateOf(0) }
    val fontSizeSp = remember { mutableStateOf(16) }

    val focusRequester = remember { FocusRequester() }
    var hasFocus by remember { mutableStateOf(false) }

    fun handleKeypress(it: KeyEvent): Boolean {
        if(it.type == KeyEventType.KeyDown &&
            it.key != Key.S && it.key != Key.DirectionDown &&
            it.key != Key.W && it.key != Key.DirectionUp) return true

        when(it.key) {
            Key.Spacebar -> {
                if(autoScrollSpeed.value != 0f) {
                    lastScrollSpeed = autoScrollSpeed.value
                    autoScrollSpeed.value = 0f
                } else {
                    autoScrollSpeed.value = lastScrollSpeed
                }
            }

            Key.Plus -> {
                autoScrollSpeed.value += 0.1f
            }

            Key.Minus -> {
                autoScrollSpeed.value -= 0.1f
            }

            Key.U -> {
                transposing.value += 1
            }

            Key.I -> {
                transposing.value -= 1
            }

            Key.DirectionDown,
            Key.S -> {
                fastScrollState = (it.type == KeyEventType.KeyDown).toInt()
            }

            Key.DirectionUp,
            Key.W -> {
                fastScrollState = -(it.type == KeyEventType.KeyDown).toInt()
            }

            else -> { }
        }
        return true
    }

    LaunchedEffect(songSaved) {
        println("Chords saved changed to $songSaved")
    }

    LaunchedEffect(song) {
        autoScrollSpeed.value = 0f
        transposing.value = 0
        lazyListState.scrollToItem(0)
    }

    LaunchedEffect(fastScrollState) {
        println("fastScrollState changed to $fastScrollState")
        if(fastScrollState != 0) {
            coroutineScope {
                while(isActive) {
                    lazyListState.scrollBy(30f * fastScrollState)
                    delay(15)
                }
            }
        }
    }

    LaunchedEffect(key1 = autoScrollSpeed.value, key2 = autoScrollEnabled.value) {
        if (autoScrollEnabled.value) {
            coroutineScope {
                while (isActive) {
                    delay(50)
                    lazyListState.scrollBy(autoScrollSpeed.value)
                }
                //autoScrollSpeed.value = 0f
            }
        }
    }

    // Two column layout
    Row(
        modifier = modifier
            .fillMaxSize()
            .focusRequester(focusRequester)
            .onFocusChanged {
                hasFocus = it.hasFocus
            }
            .focusable(true)
            .onKeyEvent(::handleKeypress),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        ChordList(
            modifier = Modifier.width(sidebarWidth).fillMaxHeight(),
            chordList = chordList,
            fontFamily = chordFontFamily,
            onChordSelected = viewModel::onSearchResultSelected, /* If we searched for chords, find the best version */
            onQueryChanged = viewModel::updateSearchSuggestions,
            suggestions = searchSuggestions,
            onSearch = { viewModel.setSearchQuery(it); true },
            isLoading = listLoading
        )

        Divider(modifier.width(1.dp).fillMaxHeight())

        Box {
            ChordView(
                song = song,
                lazyListState = lazyListState,
                transposeBy = transposing.value,
                fontSize = fontSizeSp.value,
                fontFamily = chordFontFamily,
                modifier = Modifier
                    .padding(horizontal = 24.dp)
                    .fillMaxSize(),
            )

            ChordViewSidebar(
                song = song,
                fontSize = fontSizeSp,
                scrollEnabled = autoScrollEnabled,
                scrollSpeed = autoScrollSpeed,
                transposing = transposing,
                modifier = Modifier.width(sidebarWidth).fillMaxHeight().align(Alignment.TopEnd),
                onChordSelected = viewModel::onSearchResultSelected,
                onSaveChordsClicked = {
                    viewModel.toggleSongFavorite(song)
                },
                onDeleteChordsClicked = {
                    viewModel.toggleSongFavorite(song)
                },
                fontFamily = chordFontFamily,
                currentChordsSaved = songSaved
            )
        }
    }

    if (!hasFocus) {
        LaunchedEffect(Unit) {
            focusRequester.requestFocus()
        }
    }
}

private fun Boolean.toInt(): Int {
    return if(this) 1 else 0
}


@Composable
fun ChordView(
    song: Song,
    lazyListState: LazyListState,
    fontFamily: ChordsFontFamily = ChordsFontFamily.default,
    transposeBy: Int = 0,
    fontSize: Int = 16,
    modifier: Modifier = Modifier,
) {
    Row(modifier = modifier) {
        song.Viewer(
            lazyListState = lazyListState,
            transposeBy = transposeBy,
            fontSize = fontSize,
            fontFamily = fontFamily,
            modifier = Modifier.fillMaxWidth().fillMaxHeight(),
        )
    }
}

@Composable
fun ChordViewSidebar(
    song: Song,
    fontSize: MutableState<Int>,
    scrollSpeed: MutableState<Float>,
    scrollEnabled: MutableState<Boolean>,
    transposing: MutableState<Int>,
    modifier: Modifier,
    currentChordsSaved: Boolean?,
    onChordSelected: (SearchResult, Boolean) -> Unit = { _, _ -> },
    onSaveChordsClicked: () -> Unit = {},
    onDeleteChordsClicked: () -> Unit = {},
    fontFamily: ChordsFontFamily = ChordsFontFamily.default,
) {
    var recommendedExpanded by remember { mutableStateOf(false) }
    //var versionsExpanded by remember { mutableStateOf(false) }

    Column(modifier = modifier.fillMaxWidth().fillMaxHeight()) {
        ElevatedCard(
            modifier = Modifier
                .animateContentSize()
                .padding(8.dp)
                .fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(top = 8.dp, bottom = 8.dp, start = 16.dp, end = 16.dp),
            ) {
                SidebarControls(
                    fontSize = fontSize,
                    scrollSpeed = scrollSpeed,
                    scrollEnabled = scrollEnabled,
                    transposing = transposing,
                    currentChordsSaved = currentChordsSaved,
                    onSaveChordsClicked = onSaveChordsClicked,
                    onDeleteChordsClicked = onDeleteChordsClicked,
                )


                Divider()

                LazyColumn(
                    modifier = Modifier.fillMaxWidth().padding(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val versionsList = (song.versions + song)
                        .toSearchResults()
                        .sortedBy { it.version?.toDoubleOrNull() }

                    val maxVotesPad = versionsList.maxOfOrNull {
                        it.votes?.toInt() ?: 0
                    }?.toString()?.length ?: 0

                    items(versionsList) { alternativeChord ->

                        VersionItem(
                            searchResult = alternativeChord,
                            selected = alternativeChord.version == song.version,
                            fontFamily = fontFamily,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    if (alternativeChord.version == song.version) return@clickable
                                    onChordSelected(alternativeChord, false)
                                },
                            maxVotesPad = maxVotesPad
                        )
                    }

                    item {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Divider(modifier = Modifier.weight(1f).padding(end = 8.dp))

                            Crossfade(recommendedExpanded) {
                                Icon(
                                    if(it) painterResource(MR.images.collapse) else painterResource(MR.images.expand),
                                    contentDescription = "Expand/collapse recommendations",
                                    modifier = Modifier
                                        .clickable {
                                            recommendedExpanded = !recommendedExpanded
                                        }.size(16.dp)
                                )
                            }
                        }
                    }

                    if(recommendedExpanded) {
                        items(song.related.toSearchResults()) {
                            RelatedItem(
                                result = it,
                                modifier = Modifier.clickable {
                                    onChordSelected(it, true)
                                }
                            )
                        }
                    }

                }

            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun SidebarControls(
    fontSize: MutableState<Int>,
    scrollSpeed: MutableState<Float>,
    scrollEnabled: MutableState<Boolean>,
    transposing: MutableState<Int>,
    currentChordsSaved: Boolean? = null,
    onSaveChordsClicked: () -> Unit = {},
    onDeleteChordsClicked: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var lastAutoscrollClick by remember { mutableStateOf(0L) }
    val fontSizePx = with(LocalDensity.current) { fontSize.value.sp.toPx() }

    Column(modifier = modifier) {
        if (currentChordsSaved != null) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Noten abspeichern:",
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.weight(1f),
                )

                FavoriteControl(
                    currentChordsSaved = currentChordsSaved,
                    onSaveChordsClicked = onSaveChordsClicked
                )

                IconButton(
                    onClick = onDeleteChordsClicked,
                    modifier = Modifier
                ) {
                    Icon(
                        Icons.Rounded.Delete,
                        contentDescription = "Search",
                    )
                }
            }
        }


        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Textgröße:",
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.weight(1f)
            )

            FontSizeControl(fontSize = fontSize)
        }

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {

            Row(
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
                }.height(40.dp).weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Auto-Scroll:",
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier
                )
            }


            AutoScrollControl(
                scrollSpeed = scrollSpeed,
                scrollEnabled = scrollEnabled,
                modifier = Modifier
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Transpose:",
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.weight(1f)
            )

            TransposeControls(transposing = transposing)
        }
    }
}
