package de.libf.kordbook.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.LocalTextSelectionColors
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TextField
import androidx.compose.material3.Icon
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TextFieldDefaults.indicatorLine
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.takeOrElse
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.libf.kordbook.data.model.SearchResult
import de.libf.kordbook.ui.components.ChordItem
import de.libf.kordbook.ui.components.ChordList
import de.libf.kordbook.ui.components.ChordsFontFamily
import de.libf.kordbook.ui.viewmodel.ChordListViewModel
import io.ktor.util.encodeBase64
import kotlinx.coroutines.delay
import moe.tlaster.precompose.flow.collectAsStateWithLifecycle
import moe.tlaster.precompose.navigation.Navigator
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChordListScreen(
    navigator: Navigator,
    chordFontFamily: ChordsFontFamily,
    modifier: Modifier = Modifier,
) {
    val viewModel: ChordListViewModel = koinInject()
    val chordList by viewModel.chordList.collectAsStateWithLifecycle()
    val searchSuggestions by viewModel.searchSuggestions.collectAsStateWithLifecycle()
    val listLoading by viewModel.listLoading.collectAsStateWithLifecycle()

    ChordList(
        modifier = Modifier.fillMaxSize(),
        chordList = chordList,
        fontFamily = chordFontFamily,
        onChordSelected = { selected: SearchResult, findBest: Boolean ->
            navigator.navigate("/chords/${findBest}/${selected.url.encodeBase64()}")
        },
        onQueryChanged = viewModel::updateSearchSuggestions,
        suggestions = searchSuggestions,
        onSearch = { viewModel.setSearchQuery(it); true },
        isLoading = listLoading
    )



    /*var searchText by remember { mutableStateOf("") }
    var isSearchActive by remember { mutableStateOf(false) }
    //val items = listOf("Item 1", "Item 2", "Item 3") // Ersetzen Sie dies durch Ihre Item-Liste
    //val filteredItems = if (searchText.isEmpty()) items else items.filter { it.contains(searchText, true) }

    val chordList by viewModel.getChords().collectAsStateWithLifecycle(emptyMap())

    LaunchedEffect(key1 = searchText) {
        delay(200)

        viewModel.setSearchQuery(searchText)
    }


    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    if (isSearchActive) {
                        AppBarTextField(
                            value = searchText,
                            onValueChange = { searchText = it },
                            hint = "Suchen..."
                        )
                    } else {
                        Text("Meine App")
                    }
                },
                actions = {
                    if (isSearchActive) {
                        IconButton(onClick = { isSearchActive = false }) {
                            Icon(Icons.Filled.Close, contentDescription = "Schließen")
                        }
                    } else {
                        IconButton(onClick = { isSearchActive = true }) {
                            Icon(Icons.Filled.Search, contentDescription = "Suchen")
                        }
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(modifier = Modifier.padding(padding)) {
            chordList.forEach {
                item {
                    Column {
                        Text("Provider: ${it.key.NAME}")
                        Divider()
                    }


                }
                items(it.value) {
                    ChordItem(
                        searchResult = it,
                        modifier = Modifier.clickable {
                            println("opening ${it.url}")
                            navigator.navigate("/chords/${it.url.encodeBase64()}")
                        })
                }
            }
        }
    }*/
}



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppBarTextField(
    value: String,
    onValueChange: (String) -> Unit,
    hint: String,
    modifier: Modifier = Modifier,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val textStyle = LocalTextStyle.current
    // make sure there is no background color in the decoration box
    val colors = TextFieldDefaults.colors(
        focusedContainerColor = Color.Unspecified,
        unfocusedContainerColor = Color.Unspecified,
        disabledContainerColor = Color.Unspecified,
    )

    // If color is not provided via the text style, use content color as a default
    val textColor = textStyle.color.takeOrElse {
        MaterialTheme.colorScheme.onSurface
    }
    val mergedTextStyle = textStyle.merge(TextStyle(color = textColor, lineHeight = 50.sp))

    // request focus when this composable is first initialized
    val focusRequester = FocusRequester()
    SideEffect {
        focusRequester.requestFocus()
    }

    // set the correct cursor position when this composable is first initialized
    var textFieldValue by remember {
        mutableStateOf(TextFieldValue(value, TextRange(value.length)))
    }
    textFieldValue = textFieldValue.copy(text = value) // make sure to keep the value updated

    CompositionLocalProvider(
        LocalTextSelectionColors provides LocalTextSelectionColors.current
    ) {
        BasicTextField(
            value = textFieldValue,
            onValueChange = {
                textFieldValue = it
                // remove newlines to avoid strange layout issues, and also because singleLine=true
                onValueChange(it.text.replace("\n", ""))
            },
            modifier = modifier
                .fillMaxWidth()
                .heightIn(32.dp)
                .indicatorLine(
                    enabled = true,
                    isError = false,
                    interactionSource = interactionSource,
                    colors = colors
                )
                .focusRequester(focusRequester),
            textStyle = mergedTextStyle,
            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions,
            interactionSource = interactionSource,
            singleLine = true,
            decorationBox = { innerTextField ->
                // places text field with placeholder and appropriate bottom padding
                TextFieldDefaults.DecorationBox(
                    value = value,
                    innerTextField = innerTextField,
                    enabled = true,
                    singleLine = true,
                    visualTransformation = VisualTransformation.None,
                    interactionSource = interactionSource,
                    isError = false,
                    placeholder = { Text(text = hint) },
                    colors = colors,
                    contentPadding = PaddingValues(bottom = 4.dp),
                )
            }
        )
    }
}