import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import de.libf.kordbook.data.sources.remote.UltimateGuitarFetcher
import de.libf.kordbook.res.MR
import de.libf.kordbook.ui.components.ChordProViewer
import de.libf.kordbook.ui.components.ChordsFontFamily
import dev.icerock.moko.resources.compose.fontFamilyResource
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.painterResource

@OptIn(ExperimentalResourceApi::class)
@Composable
fun App() {
    MaterialTheme {
        var greetingText by remember { mutableStateOf("Hello World!") }
        var showImage by remember { mutableStateOf(false) }
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

        val chords = UltimateGuitarFetcher().fetchSongFromUrl("https://tabs.ultimate-guitar.com/tab/jeff-buckley/hallelujah-chords-198052")

        ChordProViewer(
            chordProText = chords,
            transposeBy = 0,
            scrollSpeed = 1f,
            isAutoScrollEnabled = false,
            fontFamily = ChordsFontFamily(
                metaName = fontFamilyResource(MR.fonts.MartianMono.bold),
                metaValue = fontFamilyResource(MR.fonts.MartianMono.medium),
                comment = fontFamilyResource(MR.fonts.MartianMono.light),
                section = fontFamilyResource(MR.fonts.MartianMono.medium),
                chord = fontFamilyResource(MR.fonts.MartianMono.bold),
                text = fontFamilyResource(MR.fonts.MartianMono.regular)
            )
        )




    }
}