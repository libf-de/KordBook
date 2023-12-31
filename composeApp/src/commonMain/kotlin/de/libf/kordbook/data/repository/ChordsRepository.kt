package de.libf.kordbook.data.repository

import de.libf.kordbook.data.model.ChordOrigin
import de.libf.kordbook.data.model.Chords
import de.libf.kordbook.data.model.LocalChordOrigin
import de.libf.kordbook.data.model.SearchResult
import de.libf.kordbook.data.sources.remote.UltimateGuitarApiFetcher
import de.libf.kordbook.data.tools.levenshtein
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.IO
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flatMapMerge
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.datetime.Clock
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class ChordsRepository : KoinComponent {
    private val localSrc: LocalChordOrigin by inject()
    private val ugSrc: UltimateGuitarApiFetcher by inject()
    private val allSources = listOf(localSrc, ugSrc)

    val chordsToDisplay = MutableStateFlow(Chords.EMPTY)
    val chordList = MutableStateFlow<Map<ChordOrigin, List<SearchResult>>>(emptyMap())
    @OptIn(ExperimentalCoroutinesApi::class)
    val currentChordsSaved: Flow<Boolean> = chordsToDisplay.flatMapMerge {
        localSrc.haveChords(it)
    }
    val searchSuggestions = MutableStateFlow(emptyList<SearchResult>())
    val listLoading = MutableStateFlow(false)

    private var suggestionJobs: MutableList<Job> = mutableListOf()

    init {
        CoroutineScope(Dispatchers.IO).launch {
            currentChordsSaved.collect {
                println("Current chords saved: $it")
            }
        }
    }

    fun getSearchSuggestions(query: String) {
        suggestionJobs.forEach { it.cancel()  }

        var suggestionList = emptyList<SearchResult>()
        val listMutex = Mutex()
        var numDone = 0

        suggestionJobs.add(
            CoroutineScope(Dispatchers.IO).launch {
                listLoading.emit(true)
            }
        )

        allSources.forEach {
            suggestionJobs.add(
                CoroutineScope(Dispatchers.IO).launch {
                    val result = it.getSearchSuggestions(query)
                    listMutex.lock()
                    suggestionList = suggestionList + result
                    suggestionList = suggestionList.sortedBy {
                        val levSong = levenshtein(it.songName, query)
                        val levArtist = levenshtein(it.artist, query)
                        levSong + levArtist
                    }
                    searchSuggestions.emit(suggestionList)
                    numDone++
                    listMutex.unlock()
                }
            )
        }

        suggestionJobs.add(
            CoroutineScope(Dispatchers.IO).launch {
                while(numDone < allSources.size) {
                    delay(100)
                }
                listLoading.emit(false)
            }
        )
    }

    suspend fun searchChordsList(query: String) {
        if(query.isBlank()) {
            showLocalChordsList()
            return
        }

        var resultMap = emptyMap<ChordOrigin, List<SearchResult>>()
        val mapMutex = Mutex()
        allSources.forEach {
            CoroutineScope(Dispatchers.IO).launch {
                val start = Clock.System.now().toEpochMilliseconds()
                println("Started fetching from ${it.NAME} at $start")
                val result = it.searchSongs(query)
                println("${it.NAME} got result in ${Clock.System.now().toEpochMilliseconds() - start}ms")
                mapMutex.lock()
                resultMap = resultMap + (it to result)
                chordList.emit(resultMap)
                mapMutex.unlock()
                println("${it.NAME} done in ${Clock.System.now().toEpochMilliseconds() - start}ms")
            }
        }
    }

    suspend fun showLocalChordsList() {
        localSrc.getAllChordsFlow().collect {
            chordList.emit(mapOf(it))
        }
    }

    private suspend fun getChordsFromUrl(url: String): Chords? {
        allSources.forEach {
            when(val data = it.fetchSongByUrl(url)) {
                is Chords -> {
                    return data
                }
            }
        }
        return null
    }

    suspend fun fetchChordsFromUrl(url: String) {
        getChordsFromUrl(url)?.let {
            chordsToDisplay.emit(it)
        }
    }

    suspend fun fetchBestVersionFromUrl(url: String) {
        val firstVersion = getChordsFromUrl(url) ?: return
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
                getChordsFromUrl(targetChordsUrl.url)?.let {
                    chordsToDisplay.emit(it)
                    return
                }
            }
        }
        chordsToDisplay.emit(firstVersion)
    }

    suspend fun saveChords(chords: Chords) {
        localSrc.storeChords(chords)
    }

    suspend fun deleteChords(chords: Chords) {
        localSrc.deleteChords(chords)
    }

}