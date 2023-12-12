package de.libf.kordbook.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import de.libf.kordbook.data.model.SearchResult
import de.libf.kordbook.res.MR
import dev.icerock.moko.resources.compose.painterResource

@Composable
fun SuggestionItem(
    searchResult: SearchResult,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(12.dp)
        ) {
            Text(
                text = searchResult.songName,
                softWrap = false,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.weight(1f)
            )

            Icon(
                painterResource(MR.images.suggestion),
                contentDescription = null
            )
        }

        Divider()
    }
}