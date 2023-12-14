package de.libf.kordbook.ui.viewmodel

import de.libf.kordbook.data.model.Chords
import de.libf.kordbook.data.model.LocalChordOrigin
import de.libf.kordbook.data.repository.ChordsRepository
import de.libf.kordbook.data.sources.remote.UltimateGuitarApiFetcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import moe.tlaster.precompose.viewmodel.ViewModel
import moe.tlaster.precompose.viewmodel.viewModelScope
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class ChordDisplayViewModel : ViewModel(), KoinComponent {
    val repo: ChordsRepository by inject()

    val chordsToDisplay = repo.chordsToDisplay
    val displayedChordsSaved = repo.currentChordsSaved

    fun fetchChords(url: String, findBest: Boolean) {
        viewModelScope.launch {
            if(findBest) {
                repo.fetchBestVersionFromUrl(url)
            } else {
                repo.fetchChordsFromUrl(url)
            }
        }

    }
}