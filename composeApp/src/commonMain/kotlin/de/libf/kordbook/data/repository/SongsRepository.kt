package de.libf.kordbook.data.repository

import androidx.compose.runtime.collectAsState
import de.libf.kordbook.data.model.SearchResult
import de.libf.kordbook.data.model.Song
import de.libf.kordbook.data.sources.AbstractSource
import de.libf.kordbook.data.stores.LocalStoreInterface
import de.libf.kordbook.data.tools.levenshtein
import io.github.aakira.napier.Napier
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.IO
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
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
    //val songList = MutableSharedFlow<List<SearchResult>>()
    val chordList = MutableStateFlow<Map<String, List<SearchResult>>>(emptyMap())
    @OptIn(ExperimentalCoroutinesApi::class)
    val currentChordsSaved: Flow<Boolean> = songToDisplay.flatMapMerge {
        localSrc.isSongFavorite(it)
    }
    val searchSuggestions = MutableStateFlow(emptyList<SearchResult>())
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
        var suggestions = emptyList<SearchResult>()
        val suggestionMutex = Mutex()

        sources.getSearchSuggestions(query) { (sourceName, sourceSuggestions) ->
            suggestionMutex.lock()
            suggestions = suggestions + sourceSuggestions
            searchSuggestions.emit(suggestions)
            suggestionMutex.unlock()
        }
    }

    fun searchChordsList(query: String) {
        /*if(query.isBlank()) {
            showLocalChordsList()
            return
        }*/

        var resultMap = emptyMap<String, List<SearchResult>>()
        val mapMutex = Mutex()

        sources.search(query, 1) {
            mapMutex.lock()
            resultMap = resultMap + it
            chordList.emit(resultMap)
            mapMutex.unlock()
        }
    }

    suspend fun showLocalChordsList() {
        /*localSrc.getAllSongsFlow().collect {
            chordList.emit(mapOf("Favoriten" to it))
        }*/
    }

    suspend fun fetchChordsFromUrl(url: String) {
        localSrc.getAndCacheSong(url)?.let { songToDisplay.emit(it) }
        /*localSrc.getSongByUrl(url).collectLatest {
            songToDisplay.emit(it)
            Napier.d { "Emitting song ${it.songName} for url ${url}" }
        }*/
        /*localSrc.getSongByUrl(url).collect {

        }*/
    }

    suspend fun fetchBestVersionFromUrl(url: String) {
        val firstVersion = localSrc.getAndCacheSong(url) ?: return
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