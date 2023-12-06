package de.libf.kordbook.ui.previews

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.runtime.Composable
import de.libf.kordbook.ui.components.ChordProViewer

@Composable
@Preview
fun ChordProViewerPreview() {
    val hallelujah = "{comment: Intro}\n" +
            "[C][Am][C][Am]\n" +
            "\n" +
            "{start_of_verse}\n" +
            "I [C]heard there was a [Am]secret chord\n" +
            "That [C]David played and it [Am]pleased the Lord\n" +
            "But [F]you don't really [G]care for music, [C]do you?[G]\n" +
            "Well it [C]goes like this the [F]fourth, the [G]fifth\n" +
            "The [Am]minor fall and the [F]major lift\n" +
            "The [G]baffled king [E7]composing halle[Am]lujah\n" +
            "{end_of_verse}\n"

    ChordProViewer(
        chordProText = hallelujah,
        transposeBy = 0,
        scrollSpeed = 1f,
        isAutoScrollEnabled = false
    )
}