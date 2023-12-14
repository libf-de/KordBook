package de.libf.kordbook.ui.viewmodel

import de.libf.kordbook.data.model.Chords
import de.libf.kordbook.data.model.SearchResult
import de.libf.kordbook.data.repository.ChordsRepository
import kotlinx.coroutines.launch
import moe.tlaster.precompose.viewmodel.ViewModel
import moe.tlaster.precompose.viewmodel.viewModelScope
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class ChordDisplayViewModel : ViewModel(), KoinComponent {
    val repo: ChordsRepository by inject()

    val chordsToDisplay = repo.chordsToDisplay
    val displayedChordsSaved = repo.currentChordsSaved

    fun onSearchResultSelected(searchResult: SearchResult, findBestVersion: Boolean) {
        viewModelScope.launch {
            if(findBestVersion) {
                repo.fetchBestVersionFromUrl(searchResult.url)
            } else {
                repo.fetchChordsFromUrl(searchResult.url)
            }
        }
    }

    fun fetchChords(url: String, findBest: Boolean) {
        viewModelScope.launch {
            if(findBest) {
                repo.fetchBestVersionFromUrl(url)
            } else {
                repo.fetchChordsFromUrl(url)
            }
        }
    }

    fun saveChords(chords: Chords) {
        viewModelScope.launch {
            repo.saveChords(chords)
        }
    }

    fun deleteChords(chords: Chords) {
        viewModelScope.launch {
            repo.deleteChords(chords)
        }
    }
}