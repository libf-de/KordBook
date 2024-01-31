package de.libf.kordbook.data.sources

import de.libf.kordbook.data.model.SearchResult
import de.libf.kordbook.data.model.Song
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch

abstract class AbstractSource(var nextHandler: AbstractSource? = null) {

    var searchJob: Job? = null
    var suggestionJob: Job? = null
    abstract val NAME: String

    internal abstract suspend fun searchImpl(query: String, page: Int): List<SearchResult>

    fun search(query: String, page: Int = 1, callback: suspend (Pair<String, List<SearchResult>>) -> Unit) {
        if(searchJob?.isActive == true) {
            searchJob?.cancel()
        }

        searchJob = CoroutineScope(Dispatchers.IO).launch {
            callback(NAME to searchImpl(query, page))
        }

        nextHandler?.search(query, page, callback)
    }


    internal abstract suspend fun fetchSongByUrlImpl(url: String): Song?
    suspend fun fetchSongByUrl(url: String): Song? {
        return fetchSongByUrlImpl(url) ?: nextHandler?.fetchSongByUrl(url)
    }

    suspend fun fetchBestSongByUrl(url: String): Song? {

        return fetchSongByUrl(url) ?: nextHandler?.fetchBestSongByUrl(url)
    }

    internal abstract suspend fun getSearchSuggestionsImpl(query: String): List<SearchResult>

    fun getSearchSuggestions(query: String, callback: suspend (Pair<String, List<SearchResult>>) -> Unit) {
        if(suggestionJob?.isActive == true) {
            suggestionJob?.cancel()
        }

        suggestionJob = CoroutineScope(Dispatchers.IO).launch {
            callback(NAME to getSearchSuggestionsImpl(query))
        }

        nextHandler?.getSearchSuggestions(query, callback)
    }

    fun getSearchSuggestionsFlow(query: String, jobList: MutableList<Job>, resultFlow: MutableSharedFlow<List<SearchResult>>) {
        jobList.add(
            CoroutineScope(Dispatchers.IO).launch {
                resultFlow.emit(getSearchSuggestionsImpl(query))
            }
        )
        nextHandler?.getSearchSuggestionsFlow(query, jobList, resultFlow)
    }
}