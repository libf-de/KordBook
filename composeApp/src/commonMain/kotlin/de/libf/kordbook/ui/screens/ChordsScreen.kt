package de.libf.kordbook.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.libf.kordbook.res.MR
import de.libf.kordbook.ui.components.ChordProViewer
import de.libf.kordbook.ui.components.ChordsFontFamily
import de.libf.kordbook.ui.viewmodel.ChordDisplayViewModel
import dev.icerock.moko.resources.compose.fontFamilyResource
import dev.icerock.moko.resources.compose.painterResource
import kotlinx.datetime.Clock
import moe.tlaster.precompose.flow.collectAsStateWithLifecycle
import moe.tlaster.precompose.navigation.Navigator
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChordsScreen(
    url: String,
    navigator: Navigator
) {
    var scrolling by remember { mutableStateOf(true) }
    var scrollSpeed by remember { mutableStateOf(1f) }
    var transposing by remember { mutableStateOf(1) }
    var fontSizeSp by remember { mutableStateOf(16) }
    val fontSizePx = with(LocalDensity.current) { fontSizeSp.sp.toPx() }
    var firstLineLength by remember { mutableStateOf(1) }

    var lastAutoscrollClick by remember { mutableStateOf(0L) }

    val viewModel: ChordDisplayViewModel = koinInject()
    val chords by viewModel.chords.collectAsStateWithLifecycle()

    LaunchedEffect(key1 = null) {
        viewModel.getChordsFromUrl(url)
    }

    LaunchedEffect(transposing) {
        println("Transposing changed to $transposing")
    }


    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("${chords.songName} von ${chords.artist}")
                },
                navigationIcon = {
                    IconButton(onClick = { navigator.goBack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { scrollSpeed += 0.1f }) {
                        Icon(
                            Icons.Rounded.Add,
                            contentDescription = "List"
                        )
                    }
                    Text(
                        text = scrollSpeed.toSingleDecimalString(),
                        modifier = Modifier.clickable {
                            if(lastAutoscrollClick == 0L) {
                                lastAutoscrollClick = Clock.System.now().toEpochMilliseconds()
                            } else {
                                val timeBetweenScrolls = Clock.System.now().toEpochMilliseconds() - lastAutoscrollClick
                                scrollSpeed = calculateSpeedFromDelay(
                                    fontSizePx,
                                    timeBetweenScrolls.toInt()
                                )
                                lastAutoscrollClick = 0
                            }
                        }
                    )
                    IconButton(onClick = { scrollSpeed -= 0.1f }) {
                        Icon(
                            Icons.Rounded.Delete,
                            contentDescription = "List"
                        )
                    }

                    Spacer(Modifier.width(8.dp))

                    IconButton(onClick = { transposing++ }) {
                        Icon(
                            painterResource(MR.images.transpose_up),
                            contentDescription = "List"
                        )
                    }
                    Text(
                        text = transposing.toTransposedString(),
                    )
                    IconButton(onClick = { transposing-- }) {
                        Icon(
                            painterResource(MR.images.transpose_down),
                            contentDescription = "List"
                        )
                    }


                    IconButton(onClick = { scrolling = !scrolling }) {
                        Icon(Icons.Default.PlayArrow, contentDescription = "List")
                    }
                },

            )
        }
    ) {
        ChordProViewer(
            chordProText = chords.chords ?: "",
            transposeBy = transposing,
            isAutoScrollEnabled = scrolling,
            scrollSpeed = scrollSpeed,
            fontSize = fontSizeSp,
            fontFamily = ChordsFontFamily(
                metaName = fontFamilyResource(MR.fonts.MartianMono.bold),
                metaValue = fontFamilyResource(MR.fonts.MartianMono.medium),
                comment = fontFamilyResource(MR.fonts.MartianMono.light),
                section = fontFamilyResource(MR.fonts.MartianMono.medium),
                chord = fontFamilyResource(MR.fonts.MartianMono.bold),
                text = fontFamilyResource(MR.fonts.MartianMono.regular),
                ui = fontFamilyResource(MR.fonts.MartianMono.thin),
            ),
            modifier = Modifier.padding(it),
            onNewTopmostLine = { lineLength ->
                firstLineLength = lineLength
            }
        )
    }
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
