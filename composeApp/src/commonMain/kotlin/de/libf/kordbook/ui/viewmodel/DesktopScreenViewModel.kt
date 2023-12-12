package de.libf.kordbook.ui.viewmodel

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import de.libf.kordbook.data.model.ChordOrigin
import de.libf.kordbook.data.model.Chords
import de.libf.kordbook.data.model.LocalChordOrigin
import de.libf.kordbook.data.model.ResultType
import de.libf.kordbook.data.model.SearchResult
import de.libf.kordbook.data.repository.ChordsRepository
import de.libf.kordbook.data.sources.remote.UltimateGuitarApiFetcher
import de.libf.kordbook.data.tools.levenshtein
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import moe.tlaster.precompose.viewmodel.ViewModel
import moe.tlaster.precompose.viewmodel.viewModelScope
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class DesktopScreenViewModel : ViewModel(), KoinComponent {
    /*val localStore: LocalChordOrigin by inject()
    val ugStore: UltimateGuitarApiFetcher by inject()
    val allSources = listOf(localStore, ugStore)*/
    val repo: ChordsRepository by inject()

    val chordList = repo.chordList
    val chordsToDisplay = repo.chordsToDisplay
    val displayedChordsSaved = repo.currentChordsSaved
    var chordsLoaded = MutableStateFlow(false)
    val searchSuggestions = repo.searchSuggestions
    val listLoading = repo.listLoading

    init {
        viewModelScope.launch {
            chordsToDisplay.collect {
                if(it.chords != null) chordsLoaded.emit(true)
            }
        }

        viewModelScope.launch {
            repo.showLocalChordsList()
        }
    }

    fun onSearchResultSelected(searchResult: SearchResult, findBestVersion: Boolean) {
        viewModelScope.launch {
            chordsLoaded.emit(false)
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