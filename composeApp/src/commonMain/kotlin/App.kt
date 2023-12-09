import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import de.libf.kordbook.data.ChordsDatabase
import de.libf.kordbook.data.DbChords
import de.libf.kordbook.data.model.LocalChordOrigin
import de.libf.kordbook.data.repository.ChordsRepository
import de.libf.kordbook.data.sources.local.RealmDataStore
import de.libf.kordbook.data.sources.local.SqlDataStore
import de.libf.kordbook.data.sources.remote.UltimateGuitarApiFetcher
import de.libf.kordbook.res.MR
import de.libf.kordbook.ui.components.ChordProViewer
import de.libf.kordbook.ui.components.ChordsFontFamily
import de.libf.kordbook.ui.screens.ChordListScreen
import de.libf.kordbook.ui.screens.ChordsScreen
import de.libf.kordbook.ui.screens.DesktopChordScreen
import de.libf.kordbook.ui.viewmodel.ChordDisplayViewModel
import de.libf.kordbook.ui.viewmodel.ChordListViewModel
import de.libf.kordbook.ui.viewmodel.DesktopScreenViewModel
import dev.icerock.moko.resources.compose.fontFamilyResource
import io.github.aakira.napier.DebugAntilog
import io.github.aakira.napier.Napier
import io.ktor.util.decodeBase64String
import moe.tlaster.precompose.navigation.NavHost
import moe.tlaster.precompose.navigation.path
import moe.tlaster.precompose.navigation.rememberNavigator
import moe.tlaster.precompose.navigation.transition.NavTransition
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.KoinContext
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

// Common App Definitions
//expect val platformModule: Module
val platformModule = module {

}

val commonModule = module {
    singleOf(::ChordListViewModel)
    //singleOf(::RealmDataStore)
    single<LocalChordOrigin> { SqlDataStore() }
    singleOf(::ChordsRepository)
    singleOf(::UltimateGuitarApiFetcher)
    singleOf(::ChordDisplayViewModel)
    singleOf(::DesktopScreenViewModel)

    single<ChordsDatabase> {
        ChordsDatabase(
            driver = get(),
            DbChordsAdapter = DbChords.Adapter(
                relatedAdapter = SqlDataStore.ListOfStringsAdapter,
                versionsAdapter = SqlDataStore.ListOfStringsAdapter
            )
        )
    }
}

data object ROUTES {
    const val DESKTOP = "/desktop"
    const val LIST = "/list"
    const val CHORD = "/chords/{url}"
}

@OptIn(ExperimentalResourceApi::class)
@Composable
fun App() {
    Napier.base(DebugAntilog())

    KoinContext {
        MaterialTheme {
            val navigator = rememberNavigator()

            Scaffold { paddingValues ->
                val bottomPadding = remember { paddingValues.calculateBottomPadding() }

                NavHost(
                    navigator = navigator,
                    navTransition = NavTransition(),
                    initialRoute = ROUTES.DESKTOP, //TODO: Revert back to SUBSTITUTIONS
                ) {
                    scene(
                        route = ROUTES.LIST,
                        navTransition = NavTransition(
                            createTransition = fadeIn(),
                            resumeTransition = fadeIn(),
                            destroyTransition = fadeOut(),
                            pauseTransition = fadeOut(),
                        ),
                    ) {
                        ChordListScreen(
                            navigator
                        )
                    }

                    scene(
                        route = ROUTES.DESKTOP,
                        navTransition = NavTransition(
                            createTransition = fadeIn(),
                            resumeTransition = fadeIn(),
                            destroyTransition = fadeOut(),
                            pauseTransition = fadeOut(),
                        ),
                    ) {
                        DesktopChordScreen(
                            navigator
                        )
                    }

                    scene(
                        route = ROUTES.CHORD,
                        navTransition = NavTransition(
                            createTransition = fadeIn(),
                            resumeTransition = fadeIn(),
                            destroyTransition = fadeOut(),
                            pauseTransition = fadeOut(),
                        )
                    ) { backStackEntry ->
                        val url: String? = backStackEntry.path<String>("url")?.decodeBase64String()
                        ChordsScreen(
                            url = url ?: "",
                            navigator = navigator
                        )
                    }

                }
            }
        }
    }


}