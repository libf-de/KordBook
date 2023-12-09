import platform.UIKit.UIDevice
import org.koin.dsl.module
class IOSPlatform: Platform {
    override val name: String = UIDevice.currentDevice.systemName() + " " + UIDevice.currentDevice.systemVersion
}

actual fun getPlatform(): Platform = IOSPlatform()

//actual val platformModule = module { }