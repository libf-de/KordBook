package de.libf.kordbook.ui.components

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import de.libf.kordbook.res.MR
import de.libf.kordbook.ui.screens.toSingleDecimalString
import de.libf.kordbook.ui.screens.toTransposedString
import dev.icerock.moko.resources.compose.painterResource
import kotlin.math.max

@Composable
fun FontSizeControl(
    fontSize: MutableState<Int>,
    showSize: Boolean = true,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier,
        ) {
        IconButton(onClick = { fontSize.value += 2 }) {
            Icon(
                painterResource(MR.images.font_larger),
                contentDescription = "List"
            )
        }
        if(showSize) {
            Text(
                text = fontSize.value.toString(),
                textAlign = TextAlign.Center,
                modifier = Modifier.width(36.dp)
            )
        }
        IconButton(onClick = { fontSize.value -= 2 }) {
            Icon(
                painterResource(MR.images.font_smaller),
                contentDescription = "List"
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AutoScrollControl(
    scrollSpeed: MutableState<Float>,
    scrollEnabled: MutableState<Boolean>,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier,
    ) {
        IconButton(onClick = { scrollSpeed.value += 0.1f; scrollEnabled.value = true }) {
            Icon(
                painterResource(MR.images.faster),
                contentDescription = "List"
            )
        }

        Row(
            modifier = Modifier.height(40.dp).combinedClickable(
                onClick = {

                },
                onLongClick = {
                    scrollSpeed.value = if (scrollSpeed.value > 0f) 0f else 1f
                }
            ).pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) {
                        val event = awaitPointerEvent()
                        if (event.type == PointerEventType.Scroll) {
                            scrollSpeed.value = max(
                                0f,
                                scrollSpeed.value - (event.changes.first().scrollDelta.y * 0.5f)
                            )
                        }
                        scrollEnabled.value = true
                    }
                }
            },
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = scrollSpeed.value.toSingleDecimalString(),
                textAlign = TextAlign.Center,
                modifier = Modifier.width(36.dp)
            )
        }

        IconButton(onClick = {
            if (scrollSpeed.value > 0) scrollSpeed.value -= 0.1f; scrollEnabled.value = true
        }) {
            Icon(
                painterResource(MR.images.slower),
                contentDescription = "List"
            )
        }
    }
}

@Composable
fun TransposeControls(
    transposing: MutableState<Int>,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier,
    ) {
        IconButton(onClick = { transposing.value++ }) {
            Icon(
                painterResource(MR.images.transpose_up),
                contentDescription = "List"
            )
        }
        Text(
            text = transposing.value.toTransposedString(),
            textAlign = TextAlign.Center,
            modifier = Modifier.width(36.dp)
        )
        IconButton(onClick = { transposing.value-- }) {
            Icon(
                painterResource(MR.images.transpose_down),
                contentDescription = "List"
            )
        }
    }
}

@Composable
fun FavoriteControl(
    currentChordsSaved: Boolean,
    onSaveChordsClicked: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Crossfade(targetState = currentChordsSaved) { saved ->
        IconButton(
            onClick = {
                onSaveChordsClicked()
            },
            modifier = modifier
        ) {
            Icon(
                imageVector = if (saved) Icons.Rounded.Favorite
                else Icons.Rounded.FavoriteBorder,
                contentDescription = "List",
            )
        }

    }
}