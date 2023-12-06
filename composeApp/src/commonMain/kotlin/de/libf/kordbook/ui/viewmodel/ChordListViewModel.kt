package de.libf.kordbook.ui.viewmodel

import de.libf.kordbook.data.model.ChordOrigin
import de.libf.kordbook.data.model.Chords
import de.libf.kordbook.data.model.SearchResult
import de.libf.kordbook.data.sources.local.RealmDataStore
import de.libf.kordbook.data.sources.remote.UltimateGuitarFetcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import moe.tlaster.precompose.viewmodel.ViewModel
import moe.tlaster.precompose.viewmodel.viewModelScope
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class ChordListViewModel : ViewModel(), KoinComponent {
    val localStore: RealmDataStore by inject()
    val ugStore: UltimateGuitarFetcher by inject()

    val allSources = listOf(localStore, ugStore)

    fun getAllChordsFlow(): Flow<List<Chords>> {
        return localStore.getAllChordsFlow()
    }

    fun searchChords(query: String): Flow<Map<ChordOrigin, List<SearchResult>>> = flow {
        val results = mutableMapOf<ChordOrigin, List<SearchResult>>()
        allSources.forEach {
            viewModelScope.launch {
                results[it] = it.searchSongs(query)
                emit(results)
            }
        }
    }

    fun showAllChords() {
        viewModelScope.launch {
            localStore.getAllChordsFlow()
        }
    }

}