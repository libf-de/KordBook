package de.libf.kordbook.ui.viewmodel

import de.libf.kordbook.data.model.Chords
import de.libf.kordbook.data.model.SearchResult
import de.libf.kordbook.data.repository.ChordsRepository
import kotlinx.coroutines.launch
import moe.tlaster.precompose.viewmodel.ViewModel
import moe.tlaster.precompose.viewmodel.viewModelScope
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class DesktopScreenViewModel : ViewModel(), KoinComponent {
    private val repo: ChordsRepository by inject()

    val chordList = repo.chordList
    val searchSuggestions = repo.searchSuggestions
    val listLoading = repo.listLoading

    val chordsToDisplay = repo.chordsToDisplay
    val displayedChordsSaved = repo.currentChordsSaved

    init {
        viewModelScope.launch {
            repo.showLocalChordsList()
        }
    }

    fun onSearchResultSelected(searchResult: SearchResult, findBestVersion: Boolean) {
        viewModelScope.launch {
            if(findBestVersion) {
                repo.fetchBestVersionFromUrl(searchResult.url)
            } else {
                repo.fetchChordsFromUrl(searchResult.url)
            }
        }
    }

    fun updateSearchSuggestions(query: String) {
        viewModelScope.launch {
            repo.getSearchSuggestions(query)
        }
    }

    fun setSearchQuery(query: String) {
        viewModelScope.launch {
            repo.searchChordsList(query)
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