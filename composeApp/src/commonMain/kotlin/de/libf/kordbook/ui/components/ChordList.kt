package de.libf.kordbook.ui.components

import androidx.compose.animation.core.animateIntAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Clear
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.SearchBar
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import de.libf.kordbook.data.model.ChordOrigin
import de.libf.kordbook.data.model.ResultType
import de.libf.kordbook.data.model.SearchResult
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChordList(
    modifier: Modifier,
    chordList: Map<ChordOrigin, List<SearchResult>>,
    onChordSelected: (selected: SearchResult, fromSearch: Boolean) -> Unit,
    onSearch: (String) -> Boolean,
    onQueryChanged: (String) -> Unit,
    isLoading: Boolean,
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
            onQueryChange = { query = it;  },
            active = searchActive,
            onActiveChange = { searchActive = it },
            onSearch = { if (onSearch(it)) searchActive = false },
            modifier = Modifier.padding(horizontal = searchPadding.dp).fillMaxWidth(),
            leadingIcon = {
                if(!isLoading) {
                    Icon(
                        Icons.Rounded.Search,
                        contentDescription = "Search",
                    )
                } else {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp)
                    )
                }
            },
            trailingIcon = {
                if(query.isNotBlank()) {
                    IconButton(
                        onClick = { query = "" },
                        modifier = Modifier
                    ) {
                        Icon(
                            Icons.Rounded.Clear,
                            contentDescription = "Search",
                        )
                    }
                }
            }
        ) {
            LazyColumn {
                items(suggestions) { suggestion ->
                    if(suggestion.type == ResultType.SUGGESTION) {
                        SuggestionItem(
                            searchResult = suggestion,
                            modifier = Modifier.clickable {
                                query = suggestion.songName
                                if(onSearch(query)) searchActive = false
                            }
                        )
                    } else {
                        ChordItem(
                            searchResult = suggestion,
                            modifier = Modifier.clickable {
                                query = suggestion.songName
                                if(onSearch(query)) searchActive = false

                                if(suggestion.type == ResultType.RESULT) {
                                    onChordSelected(suggestion, true)
                                }
                            }
                        )
                    }
                }
            }
        }
        LazyColumn(modifier = Modifier) {
            chordList.mapValues {
                it.value.groupBy { itm -> itm.songId }
            }.forEach {
                item {
                    Column {
                        Text("Provider: ${it.key.NAME}")
                        Divider()
                    }


                }
                items(it.value.values.toList()) { versionList ->
                    val ratingSortedList = versionList
                        .sortedBy { itm -> itm.ratingVotesRatio() }
                        .reversed()

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