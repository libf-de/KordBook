package de.libf.kordbook.ui.screens

import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.libf.kordbook.data.model.Chords
import de.libf.kordbook.data.model.SearchResult
import de.libf.kordbook.data.model.toSearchResults
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
import kotlinx.datetime.Clock
import moe.tlaster.precompose.flow.collectAsStateWithLifecycle
import org.koin.compose.koinInject


@Composable
fun DesktopChordScreen(
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

    val chords by viewModel.chordsToDisplay.collectAsStateWithLifecycle()
    val chordsSaved by viewModel.displayedChordsSaved.collectAsStateWithLifecycle(false)

    val autoScrollEnabled = remember { mutableStateOf(true) }
    val autoScrollSpeed = remember { mutableStateOf(0f) }
    val transposing = remember { mutableStateOf(0) }
    val fontSizeSp = remember { mutableStateOf(16) }

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
            onQueryChanged = viewModel::updateSearchSuggestions,
            suggestions = searchSuggestions,
            onSearch = { viewModel.setSearchQuery(it); true },
            isLoading = listLoading
        )

        Divider(modifier.width(1.dp).fillMaxHeight())

        Box {
            ChordView(
                chords = chords,
                isAutoScrollEnabled = autoScrollEnabled.value,
                scrollSpeed = autoScrollSpeed.value,
                transposeBy = transposing.value,
                fontSize = fontSizeSp.value,
                fontFamily = chordFontFamily,
                modifier = Modifier
                    .padding(horizontal = 24.dp)
                    .fillMaxSize(),
            )

            ChordViewSidebar(
                chords = chords,
                fontSize = fontSizeSp,
                scrollEnabled = autoScrollEnabled,
                scrollSpeed = autoScrollSpeed,
                transposing = transposing,
                modifier = Modifier.width(sidebarWidth).fillMaxHeight().align(Alignment.TopEnd),
                onChordSelected = viewModel::onSearchResultSelected,
                onSaveChordsClicked = {
                    /*if (chordsSaved) {
                        viewModel.deleteChords(chords)
                    } else {*/
                        viewModel.saveChords(chords)
                    //}
                },
                onDeleteChordsClicked = {
                    viewModel.deleteChords(chords)
                },
                fontFamily = chordFontFamily,
                currentChordsSaved = chordsSaved
            )
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
        chords.Viewer(
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
                            fontFamily = fontFamily,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    if (alternativeChord.version == chords.version) return@clickable
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
                        items(chords.related.toSearchResults()) {
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
