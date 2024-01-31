package de.libf.kordbook.ui.viewmodel

import de.libf.kordbook.data.model.SearchResult
import de.libf.kordbook.data.model.Song
import de.libf.kordbook.data.repository.SongsRepository
import kotlinx.coroutines.launch
import moe.tlaster.precompose.viewmodel.ViewModel
import moe.tlaster.precompose.viewmodel.viewModelScope
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class DesktopScreenViewModel : ViewModel(), KoinComponent {
    private val repo: SongsRepository by inject()

    val chordList = repo.songList
    val searchSuggestions = repo.searchSuggestions
    val listLoading = repo.listLoading

    val chordsToDisplay = repo.songToDisplay
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

    fun toggleSongFavorite(song: Song) {
        viewModelScope.launch {
            repo.toggleSongFavorite(song)
        }
    }

}