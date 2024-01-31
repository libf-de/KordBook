package de.libf.kordbook.ui.viewmodel

import de.libf.kordbook.data.model.SearchResult
import de.libf.kordbook.data.model.Song
import de.libf.kordbook.data.repository.SongsRepository
import kotlinx.coroutines.launch
import moe.tlaster.precompose.viewmodel.ViewModel
import moe.tlaster.precompose.viewmodel.viewModelScope
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class ChordDisplayViewModel : ViewModel(), KoinComponent {
    val repo: SongsRepository by inject()

    val chordsToDisplay = repo.songToDisplay
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

    fun toggleSongFavorite(song: Song) {
        viewModelScope.launch {
            repo.toggleSongFavorite(song)
        }
    }
}