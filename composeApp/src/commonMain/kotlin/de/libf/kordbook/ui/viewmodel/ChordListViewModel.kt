package de.libf.kordbook.ui.viewmodel

import de.libf.kordbook.data.model.ChordOrigin
import de.libf.kordbook.data.model.LocalChordOrigin
import de.libf.kordbook.data.model.SearchResult
import de.libf.kordbook.data.sources.remote.UltimateGuitarApiFetcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import moe.tlaster.precompose.viewmodel.ViewModel
import moe.tlaster.precompose.viewmodel.viewModelScope
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class ChordListViewModel : ViewModel(), KoinComponent {
    val localStore: LocalChordOrigin by inject()
    val ugStore: UltimateGuitarApiFetcher by inject()
    val allSources = listOf(localStore, ugStore)

    var searchQuery = MutableStateFlow("")

    fun setSearchQuery(query: String) {
        viewModelScope.launch {
            searchQuery.emit(query)
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun getChords(): Flow<Map<ChordOrigin, List<SearchResult>>> = combine(
        searchQuery,
        *allSources.map { source ->
            searchQuery.flatMapLatest { query -> source.searchSongsFlow(query) }
        }.toTypedArray()
    ) { results ->
        results
            .filterIsInstance<Map<ChordOrigin, List<SearchResult>>>()
            .flatMap { it.entries }
            .associate { it.key to it.value }
    }

    fun showAllChords() {
        viewModelScope.launch {
            localStore.getAllChordsFlow()
        }
    }

}