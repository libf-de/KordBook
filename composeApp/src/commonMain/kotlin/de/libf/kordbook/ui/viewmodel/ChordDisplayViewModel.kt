package de.libf.kordbook.ui.viewmodel

import de.libf.kordbook.data.model.Chords
import de.libf.kordbook.data.model.LocalChordOrigin
import de.libf.kordbook.data.sources.remote.UltimateGuitarApiFetcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import moe.tlaster.precompose.viewmodel.ViewModel
import moe.tlaster.precompose.viewmodel.viewModelScope
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class ChordDisplayViewModel : ViewModel(), KoinComponent {
    val localStore: LocalChordOrigin by inject()
    val ugStore: UltimateGuitarApiFetcher by inject()
    val allSources = listOf(localStore, ugStore)

    val chords = MutableStateFlow(Chords.EMPTY)

    fun getChordsFromUrl(url: String) {
        viewModelScope.launch {
            allSources.forEach {
                when(val data = it.fetchSongByUrl(url,)) {
                    is Chords -> {
                        chords.emit(data)
                        return@launch
                    }
                }
            }
        }
    }
}