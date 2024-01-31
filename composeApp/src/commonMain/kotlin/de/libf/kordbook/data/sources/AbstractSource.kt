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
    open fun setNextHandler(nextHandler: AbstractSource) {
        this.nextHandler = nextHandler
    }

    internal abstract suspend fun searchImpl(query: String, page: Int): List<SearchResult>

    suspend fun searchToFlow(query: String, page: Int = 1, resultFlow: MutableSharedFlow<List<SearchResult>>) {
        resultFlow.emit(searchImpl(query, page))
        nextHandler?.searchToFlow(query, page, resultFlow)
    }

    internal abstract suspend fun fetchSongByUrlImpl(url: String): Song?
    suspend fun fetchSongByUrl(url: String): Song? {
        return fetchSongByUrlImpl(url) ?: nextHandler?.fetchSongByUrl(url)
    }

    suspend fun fetchBestSongByUrl(url: String): Song? {

        return fetchSongByUrl(url) ?: nextHandler?.fetchBestSongByUrl(url)
    }

    internal abstract suspend fun getSearchSuggestionsImpl(query: String): List<SearchResult>
    fun getSearchSuggestionsFlow(query: String, jobList: MutableList<Job>, resultFlow: MutableSharedFlow<List<SearchResult>>) {
        jobList.add(
            CoroutineScope(Dispatchers.IO).launch {
                resultFlow.emit(getSearchSuggestionsImpl(query))
            }
        )
        nextHandler?.getSearchSuggestionsFlow(query, jobList, resultFlow)
    }
}