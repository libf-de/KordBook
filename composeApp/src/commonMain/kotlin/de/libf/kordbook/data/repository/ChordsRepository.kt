package de.libf.kordbook.data.repository

import de.libf.kordbook.data.model.ChordOrigin
import de.libf.kordbook.data.model.Chords
import de.libf.kordbook.data.model.LocalChordOrigin
import de.libf.kordbook.data.model.SearchResult
import de.libf.kordbook.data.sources.remote.UltimateGuitarApiFetcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flatMapMerge
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.datetime.Clock
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class ChordsRepository : KoinComponent {
    val localSrc: LocalChordOrigin by inject()
    val ugSrc: UltimateGuitarApiFetcher by inject()
    val allSources = listOf(localSrc, ugSrc)

    val chordsToDisplay = MutableStateFlow(Chords.EMPTY)
    val chordList = MutableStateFlow<Map<ChordOrigin, List<SearchResult>>>(emptyMap())
    @OptIn(ExperimentalCoroutinesApi::class)
    val currentChordsSaved: Flow<Boolean> = chordsToDisplay.flatMapMerge {
        localSrc.haveChords(it)
    }

    init {
        CoroutineScope(Dispatchers.IO).launch {
            currentChordsSaved.collect {
                println("Current chords saved: $it")
            }
        }
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
                println("Started fetching from ${it.NAME} at ${start}")
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
            val targetChordsUrl = firstVersion
                .versions
                .sortedBy { it.ratingVotesRatio() }
                .reversed()
                .first()
                .url

            getChordsFromUrl(targetChordsUrl)?.let {
                chordsToDisplay.emit(it)
                return
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