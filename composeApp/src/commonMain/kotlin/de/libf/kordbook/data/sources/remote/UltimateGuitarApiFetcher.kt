package de.libf.kordbook.data.sources.remote

import de.libf.kordbook.data.converter.UltimateGuitarConverter
import de.libf.kordbook.data.model.ChordOrigin
import de.libf.kordbook.data.model.Chords
import de.libf.kordbook.data.model.ResultType
import de.libf.kordbook.data.model.SearchResult
import de.libf.kordbook.data.tools.Md5
import io.github.aakira.napier.Napier
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.HttpRequestRetry
import io.ktor.client.plugins.UserAgent
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.http.encodeURLParameter
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * Fetches data from the Ultimate Guitar API.
 *
 * Implements the ChordOrigin interface and uses Koin for dependency injection.
 */
class UltimateGuitarApiFetcher : ChordOrigin, KoinComponent {
    /** ChordOrigin attributes **/
    companion object {
        const val NAME = "Ultimate Guitar"
        const val REMOTE_SOURCE = false
    }

    override val NAME: String
        get() = Companion.NAME

    override val REMOTE_SOURCE: Boolean
        get() = Companion.REMOTE_SOURCE

    /** Ktor-Client / API-"Authentication" related **/
    private val md5Tool by inject<Md5>()

    private val clientID = (1..16).map {
        (('a'..'f') + ('0'..'9')).random()
    }.joinToString("")

    /**
     * Generates the API key.
     *
     * The API key is a combination of the client ID and the current date.
     *
     * @return The generated API key.
     */
    private fun apiKey(): String {
        val currentDate = Clock.System.now().toLocalDateTime(TimeZone.UTC)
        return "${clientID}${currentDate.year}-" +
                "${currentDate.monthNumber.toTwoDigitString()}-" +
                "${currentDate.dayOfMonth.toTwoDigitString()}:" +
                "${currentDate.hour.toTwoDigitString()}createLog()"
    }

    /**
     * The HTTP client used to make requests to the Ultimate Guitar API.
     *
     * The client includes several plugins for user agent, retrying requests, and content negotiation.
     */
    private val client = HttpClient {
        defaultRequest {
            header("X-UG-CLIENT-ID", clientID)
            header("X-UG-API-KEY", md5Tool.fromString(apiKey()))
        }

        install(UserAgent) {
            agent = "UG_ANDROID/7.0.7 (Pixel; Android 11)"
        }

        install(HttpRequestRetry) {
            retryOnException(
                maxRetries = 3,
                retryOnTimeout = true
            )

            retryOnServerErrors(
                maxRetries = 3
            )
        }

        install(ContentNegotiation) {
            json(Json {
                prettyPrint = true
                isLenient = true
                ignoreUnknownKeys = true
            })
        }
    }

    /** JSON data classes **/
    @Serializable
    private data class UGSearch(
        val tabs: List<UGTab>
    )

    @Serializable
    private data class UGSuggestions(
        val suggestions: List<String>
    )

    @Serializable
    private data class UGTab(
        val id: Int,
        val song_id: Int? = null,
        val song_name: String,
        val artist_id: Int,
        val artist_name: String,
        val version: Int? = null,
        val votes: Int? = null,
        val rating: Double? = null,
        val tonality_name: String = "",
        val type: String? = null
    )

    @Serializable
    private data class UGTabData(
        val id: Int,
        val song_id: Int? = null,
        val song_name: String,
        val artist_id: Int,
        val artist_name: String,
        val type: String? = null,
        val version: Int? = null,
        val votes: Int? = null,
        val rating: Double? = null,
        val tonality_name: String = "",
        val versions: List<UGTab>,
        val capo: Int? = null,
        val recommended: List<UGTab>,
        val content: String
    ) {
        /**
         * Converts the tab data to a Chords object.
         *
         * @return The converted Chords object.
         */
        fun toChords(): Chords {
            val chordsContent = UltimateGuitarConverter.convertToChordPro(this.content)
            return Chords(
                id = this.id.toString(),
                songName = this.song_name,
                songId = this.song_id.toString(),
                artist = this.artist_name,
                artistId = this.artist_id.toString(),
                version = this.version.toString(),
                tonality = this.tonality_name,
                capo = this.capo.toString(),
                chords = chordsContent,
                rating = this.rating,
                votes = this.votes?.toDouble(),
                versions = this.versions.map {
                    Chords(
                        id = it.id.toString(),
                        songName = it.song_name,
                        songId = it.song_id.toString(),
                        artist = it.artist_name,
                        artistId = it.artist_id.toString(),
                        version = it.version.toString(),
                        tonality = it.tonality_name,
                        rating = it.rating,
                        votes = it.votes?.toDouble(),
                        capo = null,
                        chords = null,
                        related = listOf(),
                        url = "UG::${it.id}",
                        origin = NAME
                    )
                }.sortedBy { it.version?.toDoubleOrNull() },
                related = this.recommended.map {
                    Chords(
                        id = it.id.toString(),
                        songName = it.song_name,
                        songId = it.song_id.toString(),
                        artist = it.artist_name,
                        artistId = it.artist_id.toString(),
                        version = it.version.toString(),
                        tonality = it.tonality_name,
                        rating = it.rating,
                        votes = it.votes?.toDouble(),
                        capo = null,
                        chords = null,
                        related = listOf(),
                        url = "UG::${it.id}",
                        origin = NAME
                    )
                },
                url = "UG::${this.id}",
                origin = NAME
            )
        }
    }

    /** Implementation **/

    /**
     * Fetches a song by its URL.
     *
     * @param url The URL of the song.
     * @return The fetched Chords object, or null if the URL does not start with "UG::".
     */
    override suspend fun fetchSongByUrl(url: String): Chords? {
        if(!url.startsWith("UG::")) return null
        val id = url.removePrefix("UG::")
        val response = client.get("https://api.ultimate-guitar.com/api/v1/tab/info?tab_id=${id}&tab_access_type=private")
        if (response.status.value !in 200..299) {
            Napier.e { "Unexpected code: $response" }
            throw Exception("Unexpected code: $response")
        }

        return response.body<UGTabData>().toChords()
    }

    /**
     * Fetches a song by a search result.
     *
     * @param searchResult The search result.
     * @return The fetched Chords object.
     */
    override suspend fun fetchSongBySearchResult(searchResult: SearchResult): Chords? {
        return fetchSongByUrl(searchResult.url)
    }

    /**
     * Searches for songs.
     *
     * @param query The search query.
     * @param page The page number.
     * @return A list of search results.
     */
    override suspend fun searchSongs(query: String, page: Int): List<SearchResult> {
        val start = Clock.System.now().toEpochMilliseconds()
        Napier.d("start running from $NAME at $start")
        val response = client.get(
            "https://api.ultimate-guitar.com/api/v1/tab/search" +
                    "?title=${query.encodeURLParameter()}" +
                    "&display_songs=1" +
                    "&filter=1" +
                    "&type[]=300" +
                    "&page=${page}")

        Napier.d("client id = ${clientID}, api key = ${apiKey()}")

        Napier.d("got response from $NAME in ${Clock.System.now().toEpochMilliseconds() - start}ms")

        if (response.status.value !in 200..299) {
            if(response.status.value == 404) return emptyList()

            Napier.e { "Unexpected code: $response" }
            throw Exception("Unexpected code: $response")
        }

        val searchData: UGSearch = response.body()

        Napier.d("parsed response from $NAME in ${Clock.System.now().toEpochMilliseconds() - start}ms")

        return searchData.tabs.filter {
            it.type == "Chords"
        }.map {
            SearchResult(
                songName = it.song_name,
                songId = it.song_id.toString(),
                artist = it.artist_name,
                artistId = it.artist_id.toString(),
                version = it.version.toString(),
                rating = it.rating,
                votes = it.votes?.toDouble(),
                id = it.id.toString(),
                url = "UG::${it.id}",
                origin = this.NAME
            )
        }
    }

    /**
     * Searches for songs, but returns a flow
     *
     * @param query The search query.
     * @return A flow of search results pairs.
     */
    override fun searchSongsFlow(query: String): Flow<Pair<ChordOrigin, List<SearchResult>>> = flow {
        if(query.isBlank()) return@flow

        emit(this@UltimateGuitarApiFetcher to searchSongs(query))
    }

    /**
     * Fetches search suggestions for the given query.
     *
     * @param query The search query.
     * @return A list of suggestions as SearchResult.
     */
    private suspend fun getSuggestions(query: String): List<SearchResult> {
        val searchSuggestions = client.get("https://api.ultimate-guitar.com/api/v1/tab/suggestion?q=${query.encodeURLParameter()}")
        if (searchSuggestions.status.value !in 200..299) {
            if(searchSuggestions.status.value == 404) return emptyList()

            Napier.e { "Unexpected code: $searchSuggestions" }
            throw Exception("Unexpected code: $searchSuggestions")
        }

        return searchSuggestions.body<UGSuggestions>().suggestions.map {
            SearchResult(
                songName = it,
                songId = it,
                artist = "",
                artistId = "",
                version = null,
                rating = null,
                votes = null,
                id = "",
                url = "",
                origin = this.NAME,
                type = ResultType.SUGGESTION
            )

        }
    }

    /**
     * Fetches search suggestions and some results for the given query.
     *
     * @param query The search query.
     * @return A list of suggestions as SearchResult.
     */
    override suspend fun getSearchSuggestions(query: String): List<SearchResult> {
        return searchSongs(query).take(3) + getSuggestions(query).take(5)
    }
}

fun Int.toTwoDigitString(): String {
    return if(this < 10) "0${this}" else this.toString()
}