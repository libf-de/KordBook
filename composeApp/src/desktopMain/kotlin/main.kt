import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.runtime.Composable
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import de.libf.kordbook.data.ChordsDatabase
import de.libf.kordbook.data.tools.JvmMd5
import de.libf.kordbook.data.tools.Md5
import moe.tlaster.precompose.PreComposeWindow
import org.koin.core.context.startKoin
import org.koin.dsl.module
import java.io.File

val desktopModule = module {
    single<Md5> { JvmMd5() }
    single<SqlDriver> {
        val dbPath = "chords.db"

        val driver = JdbcSqliteDriver("jdbc:sqlite:${dbPath}")
        if(!File(dbPath).exists())
            ChordsDatabase.Schema.create(driver)
        driver
    }

}

fun main() = application {
    startKoin {
        modules(commonModule + desktopModule)
    }
    PreComposeWindow(onCloseRequest = ::exitApplication) {
        App()
    }
}

@Preview
@Composable
fun AppDesktopPreview() {
    App()
}