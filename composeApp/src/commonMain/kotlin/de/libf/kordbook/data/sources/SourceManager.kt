package de.libf.kordbook.data.sources

import de.libf.kordbook.data.model.ChordOrigin
import de.libf.kordbook.data.model.LocalChordOrigin
import de.libf.kordbook.data.sources.local.RealmDataStore
import de.libf.kordbook.data.sources.remote.UltimateGuitarApiFetcher
import de.libf.kordbook.data.sources.remote.UltimateGuitarFetcher
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class SourceManager : KoinComponent {
    fun <T: ChordOrigin> getSourceFromName(name: String): T {
        return when(name) {
            LocalChordOrigin.NAME -> inject<LocalChordOrigin>().value as T
            UltimateGuitarApiFetcher.NAME -> inject<UltimateGuitarApiFetcher>().value as T
            UltimateGuitarFetcher.NAME -> inject<UltimateGuitarFetcher>().value as T

            else -> throw ClassCastException("name not found")
        }
    }
}