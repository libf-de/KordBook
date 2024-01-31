package de.libf.kordbook.data.repository

import de.libf.kordbook.data.model.SearchResult
import de.libf.kordbook.data.model.Song
import de.libf.kordbook.data.sources.AbstractSource
import de.libf.kordbook.data.stores.LocalStoreInterface
import de.libf.kordbook.data.tools.levenshtein
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.IO
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapMerge
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.datetime.Clock
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class SongsRepository : KoinComponent {
    private val localSrc: LocalStoreInterface by inject()
    private val sources: AbstractSource by inject()

    //private val allSources = listOf(localSrc, ugSrc)

    val songToDisplay = MutableStateFlow(Song.EMPTY)
    val songList = MutableSharedFlow<List<SearchResult>>()
    @OptIn(ExperimentalCoroutinesApi::class)
    val currentChordsSaved: Flow<Boolean> = songToDisplay.flatMapMerge {
        localSrc.isSongFavorite(it)
    }
    val searchSuggestions = MutableSharedFlow<List<SearchResult>>()
    val listLoading = MutableStateFlow(false)

    private var suggestionJobs: MutableList<Job> = mutableListOf()

    init {
        CoroutineScope(Dispatchers.IO).launch {
            currentChordsSaved.collect {
                println("Current song saved: $it")
            }
        }
    }

    fun getSearchSuggestions(query: String) {
        suggestionJobs.forEach { it.cancel() }
        sources.getSearchSuggestionsFlow(query, suggestionJobs, searchSuggestions)
    }

    suspend fun searchChordsList(query: String) {
        if(query.isBlank()) {
            showLocalChordsList()
            return
        }

        sources.searchToFlow(query, 1, songList)
    }

    suspend fun showLocalChordsList() {
        localSrc.getAllSongsFlow().collect {
            songList.emit(it)
        }
    }

    suspend fun fetchChordsFromUrl(url: String) {
        localSrc.getSongByUrl(url).collect {
            songToDisplay.emit(it)
        }
    }

    suspend fun fetchBestVersionFromUrl(url: String) {
        val firstVersion = localSrc.getSongByUrl(url).first()
        if(firstVersion.versions.size > 1) {
            println(firstVersion.versions.sortedBy { it.ratingVotesRatio() }.reversed().map {
                "${it.version} -> ${it.rating} / ${it.votes} / ${it.ratingVotesRatio()}"
            })

            val targetChordsUrl = firstVersion
                .versions
                .sortedBy { it.ratingVotesRatio() }
                .reversed()
                .first()

            if(targetChordsUrl.ratingVotesRatio() > firstVersion.ratingVotesRatio()) {
                fetchChordsFromUrl(targetChordsUrl.url)
                return
            }
        }
        songToDisplay.emit(firstVersion)
    }

    suspend fun toggleSongFavorite(song: Song) {
        localSrc.toggleSongFavorite(song)
    }
}