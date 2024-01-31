import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import com.example.compose.AppTheme
import de.libf.kordbook.data.ChordsDatabase
import de.libf.kordbook.data.DbSong
import de.libf.kordbook.data.extensions.ChordFormatAdapter
import de.libf.kordbook.data.extensions.InstrumentTypeAdapter
import de.libf.kordbook.data.extensions.ListOfStringsAdapter
import de.libf.kordbook.data.repository.SongsRepository
import de.libf.kordbook.data.sources.AbstractSource
import de.libf.kordbook.data.sources.remote.UltimateGuitarSource
import de.libf.kordbook.data.stores.LocalStoreInterface
import de.libf.kordbook.data.stores.SqldelightStore
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
    single<UltimateGuitarSource> { UltimateGuitarSource() }
    single<SqldelightStore> { SqldelightStore(get<UltimateGuitarSource>()) }

    single<LocalStoreInterface> { get<SqldelightStore>() }
    single<AbstractSource> { get<SqldelightStore>() }

    singleOf(::SongsRepository)
    //singleOf(::UltimateGuitarApiFetcher)
    singleOf(::ChordDisplayViewModel)

    single {
        ChordsDatabase(
            driver = get(),
            DbSongAdapter = DbSong.Adapter(
                relatedAdapter = ListOfStringsAdapter,
                versionsAdapter = ListOfStringsAdapter,
                formatAdapter = ChordFormatAdapter,
                instrumentAdapter = InstrumentTypeAdapter
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


