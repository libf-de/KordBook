import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import com.example.compose.AppTheme
import de.libf.kordbook.data.ChordsDatabase
import de.libf.kordbook.data.DbChords
import de.libf.kordbook.data.model.LocalChordOrigin
import de.libf.kordbook.data.repository.ChordsRepository
import de.libf.kordbook.data.sources.local.SqlDataStore
import de.libf.kordbook.data.sources.remote.UltimateGuitarApiFetcher
import de.libf.kordbook.res.MR
import de.libf.kordbook.ui.components.ChordsFontFamily
import de.libf.kordbook.ui.screens.ChordListScreen
import de.libf.kordbook.ui.screens.ChordsScreen
import de.libf.kordbook.ui.viewmodel.ChordDisplayViewModel
import de.libf.kordbook.ui.viewmodel.ChordListViewModel
import dev.icerock.moko.resources.compose.fontFamilyResource
import io.github.aakira.napier.DebugAntilog
import io.github.aakira.napier.Napier
import io.ktor.util.decodeBase64String
import moe.tlaster.precompose.PreComposeApp
import moe.tlaster.precompose.navigation.NavHost
import moe.tlaster.precompose.navigation.path
import moe.tlaster.precompose.navigation.rememberNavigator
import moe.tlaster.precompose.navigation.transition.NavTransition
import org.koin.compose.KoinContext
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val commonModule = module {
    singleOf(::ChordListViewModel)
    single<LocalChordOrigin> { SqlDataStore() }
    singleOf(::ChordsRepository)
    singleOf(::UltimateGuitarApiFetcher)
    singleOf(::ChordDisplayViewModel)

    single {
        ChordsDatabase(
            driver = get(),
            DbChordsAdapter = DbChords.Adapter(
                relatedAdapter = SqlDataStore.ListOfStringsAdapter,
                versionsAdapter = SqlDataStore.ListOfStringsAdapter,
                formatAdapter = SqlDataStore.ChordFormatAdapter
            )
        )
    }
}

data object ROUTES {
    const val LIST = "/list"
    const val CHORD = "/chords/{best}/{url}"
}

@Composable
fun BaseApp(mainComposable: @Composable () -> Unit) {
    Napier.base(DebugAntilog())

    KoinContext {
        PreComposeApp {
            AppTheme {
                mainComposable()
            }
        }
    }
}

@Composable
fun DefaultComposable() {
    val navigator = rememberNavigator()

    val fontFamily = ChordsFontFamily(
        metaName = fontFamilyResource(MR.fonts.MartianMono.bold),
        metaValue = fontFamilyResource(MR.fonts.MartianMono.medium),
        comment = fontFamilyResource(MR.fonts.MartianMono.light),
        section = fontFamilyResource(MR.fonts.MartianMono.medium),
        chord = fontFamilyResource(MR.fonts.MartianMono.bold),
        text = fontFamilyResource(MR.fonts.MartianMono.regular),
        title = fontFamilyResource(MR.fonts.MartianMono.bold),
        subtitle = fontFamilyResource(MR.fonts.MartianMono.medium),
    )

    Scaffold { _ ->
        NavHost(
            navigator = navigator,
            navTransition = NavTransition(),
            initialRoute = ROUTES.LIST,
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
                    navigator = navigator,
                    chordFontFamily = fontFamily
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
                val best: Boolean = backStackEntry.path<Boolean>("best") ?: true
                ChordsScreen(
                    url = url ?: "",
                    findBest = best,
                    chordFontFamily = fontFamily,
                    navigator = navigator
                )
            }

        }
    }
}

@Composable
fun App() {
    BaseApp {
        DefaultComposable()
    }
}


