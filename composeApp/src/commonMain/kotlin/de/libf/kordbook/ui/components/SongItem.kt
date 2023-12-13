package de.libf.kordbook.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import de.libf.kordbook.data.model.LocalChordOrigin
import de.libf.kordbook.data.model.SearchResult
import kotlin.math.max
import kotlin.math.min

@Composable
fun SongItem(
    songList: List<SearchResult>,
    onChordsSelected: (SearchResult) -> Unit,
    fontFamily: ChordsFontFamily = ChordsFontFamily.default,
    modifier: Modifier = Modifier,
) {
    if(songList.isEmpty()) return
    val mainVersion = songList.first()
    val otherVersions = songList.drop(1)

    Column(modifier = modifier) {
        Column(
            modifier = Modifier
                .padding(top = 12.dp, start = 12.dp, end = 12.dp, bottom = 4.dp)
                .clickable { onChordsSelected(mainVersion) }
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
            ) {
                Text(
                    text = mainVersion.songName,
                    softWrap = false,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.weight(1f)
                )

                if(mainVersion.origin == LocalChordOrigin.NAME || otherVersions.isNotEmpty()) {
                    VersionBox(
                        text = "v${mainVersion.version}",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = mainVersion.artist,
                    softWrap = false,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f)
                )

                if(mainVersion.origin == LocalChordOrigin.NAME || otherVersions.isNotEmpty()) {
                    RatingBar(
                        value = mainVersion.rating?.toFloat() ?: 0f,
                        stepSize = StepSize.HALF,
                        style = RatingBarStyle.Stroke(),
                        size = 16.dp,
                        spaceBetween = 2.dp,
                        isIndicator = true,
                        onValueChange = {},
                        onRatingChanged = {},
                        modifier = Modifier
                    )

                    Text(
                        text = "(${mainVersion.votes?.toInt()})",
                        style = MaterialTheme.typography.bodySmall,
                        softWrap = false,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        fontFamily = fontFamily.text,
                        modifier = Modifier.padding(start = 8.dp, bottom = 2.dp)
                    )
                }
            }
        }

        otherVersions.forEach {
            VersionItem(
                searchResult = it,
                fontFamily = fontFamily,
                modifier = Modifier
                    .padding(12.dp)
                    .clickable { onChordsSelected(it) }
            )
        }

        Divider()
    }
}

private fun <E> List<E>.exist(): Boolean {
    return this.isNotEmpty()
}

@Composable
fun VersionItem(
    searchResult: SearchResult,
    selected: Boolean = false,
    fontFamily: ChordsFontFamily = ChordsFontFamily.default,
    modifier: Modifier = Modifier,
    maxVotesPad: Int = 5
) {
    Row(
        modifier = modifier
    ) {
        VersionBox(
            text = "v${searchResult.version}",
            /*fontFamily = fontFamily.text,*/
            style = MaterialTheme.typography.bodySmall,
            fontWeight = if(selected) FontWeight.Bold else null,
            modifier = Modifier
        )

        Spacer(modifier = Modifier.weight(1f))

        RatingBar(
            value = searchResult.rating?.toFloat() ?: 0f,
            stepSize = StepSize.HALF,
            style = RatingBarStyle.Stroke(),
            size = 16.dp,
            spaceBetween = 2.dp,
            isIndicator = true,
            onValueChange = {},
            onRatingChanged = {},
            modifier = Modifier
        )

        val votes = searchResult.votes?.toInt().toString() ?: "?"
        val padLength = max(maxVotesPad-votes.length, 0)
        Text(
            text = "($votes)${" ".repeat(padLength)}",
            style = MaterialTheme.typography.bodySmall,
            fontFamily = fontFamily.text,
            modifier = Modifier.padding(start = 8.dp, bottom = 2.dp)
        )

    }
}