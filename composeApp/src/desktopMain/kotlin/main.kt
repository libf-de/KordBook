import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import ca.gosyer.appdirs.AppDirs
import de.libf.kordbook.data.ChordsDatabase
import de.libf.kordbook.data.tools.JvmMd5
import de.libf.kordbook.data.tools.KeyEventDispatcher
import de.libf.kordbook.data.tools.Md5
import de.libf.kordbook.ui.screens.DesktopChordScreen
import de.libf.kordbook.ui.viewmodel.DesktopScreenViewModel
import io.github.aakira.napier.Napier
import org.koin.compose.koinInject
import org.koin.core.context.startKoin
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module
import java.io.File
import java.security.Key
import kotlin.io.path.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.exists
import kotlin.io.path.isRegularFile
import dev.icerock.moko.resources.compose.painterResource
import de.libf.kordbook.res.MR

val desktopModule = module {
    single<Md5> { JvmMd5() }
    single<SqlDriver> {
        val appDirs = AppDirs("KordBook")

        val dbDir = Path(appDirs.getUserDataDir())
        if(!dbDir.exists())
            dbDir.createDirectories()


        val dbPath = dbDir.resolve("chords.db")

        Napier.d("db located at: $dbPath")

        val driver = JdbcSqliteDriver("jdbc:sqlite:$dbPath")
        if(!dbPath.exists())
            ChordsDatabase.Schema.create(driver)
        driver
    }

    singleOf(::DesktopScreenViewModel)

}

fun main() = application {
    startKoin {
        modules(commonModule + desktopModule)
    }

    val eventHandler = remember { KeyEventDispatcher() }

    Window(onCloseRequest = ::exitApplication,
           title = "Kordbook",
           icon = painterResource(MR.images.icon),
        /*onKeyEvent = {
            eventHandler.handleKeyEvent(it)
            false
        }*/) {
        BaseApp {
            DesktopChordScreen(eventHandler)
        }
    }
}

@Preview
@Composable
fun AppDesktopPreview() {
    App()
}
