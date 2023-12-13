package de.libf.kordbook.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import de.libf.kordbook.data.model.LocalChordOrigin
import de.libf.kordbook.data.model.SearchResult

@Composable
fun ChordItem(
    searchResult: SearchResult,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(start = 12.dp, end = 12.dp, top = 12.dp)
        ) {
            Text(
                text = searchResult.songName,
                softWrap = false,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.weight(1f)
            )

            if(searchResult.origin == LocalChordOrigin.NAME) {
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

                Text(
                    text = "(${searchResult.votes?.toInt()})",
                    style = MaterialTheme.typography.bodySmall,
                    softWrap = false,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(start = 8.dp, bottom = 2.dp)
                )
            }


        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(start = 12.dp, end = 12.dp, top = 2.dp, bottom = 12.dp)
        ) {
            Text(
                text = searchResult.artist,
                style = MaterialTheme.typography.titleMedium,
                softWrap = false,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )

            if(searchResult.origin == LocalChordOrigin.NAME) {
                VersionBox(
                    text = "v${searchResult.version}",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier
                )
            }
        }

        Divider()
    }
}

@Composable
fun VersionBox(
    text: String,
    fontFamily: FontFamily? = null,
    fontWeight: FontWeight? = null,
    style: TextStyle = MaterialTheme.typography.bodySmall,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(MaterialTheme.colorScheme.secondaryContainer, RoundedCornerShape(6.dp))
            .padding(horizontal = 3.dp, vertical = 2.dp)
    ) {
        Text(
            text = text,
            fontWeight = fontWeight,
            fontFamily = fontFamily,
            style = style,
            color = MaterialTheme.colorScheme.onSecondaryContainer,
        )
    }
}