import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.compose.ExperimentalComposeLibrary

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.jetbrainsCompose)
    alias(libs.plugins.realm)
    id(libs.plugins.mokoResources.get().pluginId)

    alias(libs.plugins.sqldelight)

    alias(libs.plugins.kotlinx.serialization)
}

kotlin {
    androidTarget {
        compilations.all {
            kotlinOptions {
                jvmTarget = "11"
            }
        }
    }
    
    jvm("desktop")
    
    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries {
            framework {
                baseName = "ComposeApp"
                isStatic = true

                // Used to provide (localized) resources on iOS
                export(libs.moko.resources)
                export(libs.moko.graphics) // toUIColor here
            }

            //extraSpecAttributes["resources"] = "['src/commonMain/resources/**']"
        }
    }
    
    sourceSets {
        //val desktopMain by getting

        /////Sqldelight iOS database driver
        //            implementation(libs.sqldelight.driver.native)


        val androidMain by getting {
            dependsOn(commonMain.get())

            dependencies {
                implementation(libs.compose.ui)
                implementation(libs.compose.ui.tooling.preview)
                implementation(libs.androidx.activity.compose)
                compileOnly(libs.realm.base)
                compileOnly(libs.realm.sync)
                implementation(libs.koin.android)
                // Android Database Driver
                implementation(libs.sqldelight.driver.android)
            }
        }

        val desktopMain by getting {
            dependsOn(commonMain.get())

            dependencies {
                implementation(compose.desktop.currentOs)

                // Sqldeight Desktop Driver
                implementation(libs.sqldelight.driver.sqlite)
            }
        }

        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            @OptIn(ExperimentalComposeLibrary::class)
            implementation(compose.components.resources)


            implementation(libs.realm.base)
            implementation(libs.realm.sync)

            // Sqldelight coroutines extension
            implementation(libs.sqldelight.coroutines)


            implementation(libs.kotlinx.coroutines)
            api(libs.koin)
            implementation(libs.koin.compose)

            // Multiplatform Resources (moko resources)
            api(libs.moko.resources)
            api(libs.moko.resources.compose)

            implementation(libs.ksoup)
            implementation(libs.ksoup.network)

            implementation(libs.kotlinx.serialization)

            // Precompose -> Multiplatform Navigation
            api(libs.precompose)
            api(libs.precompose.viewModel)
            api(libs.precompose.koin)

            // Logging
            api(libs.logging)

            // Datetime
            api(libs.kotlinx.datetime)

            // Ktor
            implementation(libs.ktor.client)
            implementation(libs.ktor.client.cio)
            implementation(libs.ktor.client.serialization.json)
            implementation(libs.ktor.client.content.negotiation)

        }
    }
}

android {
    namespace = "de.libf.kordbook"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
    sourceSets["main"].res.srcDirs("src/androidMain/res")
    sourceSets["main"].resources.srcDirs("src/commonMain/resources")

    defaultConfig {
        applicationId = "de.libf.kordbook"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 1
        versionName = "1.0"
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = libs.versions.compose.compiler.get()
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    dependencies {
        debugImplementation(libs.compose.ui.tooling)
    }
}

compose.desktop {
    application {
        mainClass = "MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "de.libf.kordbook"
            packageVersion = "1.0.0"
        }
    }
}

multiplatformResources {
    multiplatformResourcesPackage = "de.libf.kordbook.res"
    disableStaticFrameworkWarning = true
}

sqldelight {
    databases {
        create("ChordsDatabase") {
            packageName = "de.libf.kordbook.data"
        }
    }
}