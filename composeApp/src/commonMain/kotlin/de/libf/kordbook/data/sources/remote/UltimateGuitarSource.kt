package de.libf.kordbook.data.sources.remote

import de.libf.kordbook.data.model.InstrumentType
import de.libf.kordbook.data.model.SongFormat
import de.libf.kordbook.data.model.ResultType
import de.libf.kordbook.data.model.SearchResult
import de.libf.kordbook.data.model.Song
import de.libf.kordbook.data.sources.AbstractSource
import de.libf.kordbook.data.tools.Md5
import io.github.aakira.napier.Napier
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.HttpRequestRetry
import io.ktor.client.plugins.UserAgent
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.statement.HttpResponse
import io.ktor.http.HttpStatusCode
import io.ktor.http.encodeURLParameter
import io.ktor.serialization.kotlinx.json.json
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class UltimateGuitarSource : AbstractSource(), KoinComponent {

    /**
     * The HTTP client used to make requests to the Ultimate Guitar API.
     *
     * The client includes several plugins for user agent, retrying requests, and content negotiation.
     */
    private class UGHttpClient : KoinComponent {
        private var client: HttpClient? = null
        private var failedRequests = 0

        /** Ktor-Client / API-"Authentication" related **/
        private val md5Tool by inject<Md5>()

        /** Client ID for the Ultimate Guitar API. **/
        var clientID: String = ""
            private set

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
                    "${currentDate.hour}createLog()"
        }

        constructor() {
            newClient()
        }

        suspend fun get(urlString: String, block: HttpRequestBuilder.() -> Unit = {}): HttpResponse {
            if(client == null) newClient()
            val response = client!!.get(urlString, block)

            if(failedRequests < 4 && response.status.value !in 200..299 && response.status.value != 404) {
                Napier.d { "Got invalid status code, trying to generate a new client..." }
                failedRequests++
                newClient()
                return get(urlString, block)
            } else if(response.status == HttpStatusCode.OK) {
                failedRequests = 0
            }

            return response
        }

        fun newClient() {
            clientID = (1..16).map {
                (('a'..'f') + ('0'..'9')).random()
            }.joinToString("")

            client = HttpClient {
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
        }
    }

    private val clientMgr = UGHttpClient()

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
         * Converts the tab data to a Song object.
         *
         * @return The converted Song object.
         */
        fun toSong(): Song {
            //val chordsContent = UltimateGuitarConverter.convertToChordPro(this.content)
            return Song(
                songName = this.song_name,
                songId = this.song_id.toString(),
                artist = this.artist_name,
                artistId = this.artist_id.toString(),
                version = this.version.toString(),
                tonality = this.tonality_name,
                capo = this.capo.toString(),
                content = this.content,
                format = SongFormat.UG,
                rating = this.rating,
                votes = this.votes?.toDouble(),
                versions = this.versions.map {
                    Song(
                        songName = it.song_name,
                        songId = it.song_id.toString(),
                        artist = it.artist_name,
                        artistId = it.artist_id.toString(),
                        version = it.version.toString(),
                        tonality = it.tonality_name,
                        rating = it.rating,
                        votes = it.votes?.toDouble(),
                        capo = null,
                        content = null,
                        related = listOf(),
                        url = "UG::${it.id}",
                        format = SongFormat.NULL,
                        instrument = InstrumentType.CHORDS
                    )
                }.sortedBy { it.version?.toDoubleOrNull() },
                related = this.recommended.map {
                    Song(
                        songName = it.song_name,
                        songId = it.song_id.toString(),
                        artist = it.artist_name,
                        artistId = it.artist_id.toString(),
                        version = it.version.toString(),
                        tonality = it.tonality_name,
                        rating = it.rating,
                        votes = it.votes?.toDouble(),
                        capo = null,
                        content = null,
                        related = listOf(),
                        url = "UG::${it.id}",
                        format = SongFormat.NULL,
                        instrument = InstrumentType.CHORDS
                    )
                },
                url = "UG::${this.id}",
                instrument = InstrumentType.CHORDS
            )
        }
    }

    override val NAME: String = "Ultimate Guitar"

    override suspend fun searchImpl(query: String, page: Int): List<SearchResult> {
        val start = Clock.System.now().toEpochMilliseconds()
        val response = clientMgr.get(
            "https://api.ultimate-guitar.com/api/v1/tab/search" +
                    "?title=${query.encodeURLParameter()}" +
                    "&display_songs=1" +
                    "&filter=1" +
                    "&type[]=300" +
                    "&page=${page}")

        Napier.d("client id = ${clientMgr.clientID}")

        Napier.d("got response from UG in ${Clock.System.now().toEpochMilliseconds() - start}ms")

        if (response.status.value !in 200..299) {
            if(response.status.value == 404) return emptyList()

            Napier.e { "Unexpected code: $response" }
            throw Exception("Unexpected code: $response")
        }

        val searchData: UGSearch = response.body()

        Napier.d("parsed response from UG in ${Clock.System.now().toEpochMilliseconds() - start}ms")

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
                url = "UG::${it.id}"
            )
        }
    }

    override suspend fun fetchSongByUrlImpl(url: String): Song? {
        if(!url.startsWith("UG::")) return null
        val id = url.removePrefix("UG::")
        val response = clientMgr.get("https://api.ultimate-guitar.com/api/v1/tab/info?tab_id=${id}&tab_access_type=private")
        if (response.status.value !in 200..299) {
            Napier.e { "Unexpected code: $response" }
            throw Exception("Unexpected code: $response")
        }

        val name =  response.body<UGTabData>().song_name

        Napier.d { "UGSource returns $name" }

        return response.body<UGTabData>().toSong()
    }

    /**
     * Fetches search suggestions for the given query.
     *
     * @param query The search query.
     * @return A list of suggestions as SearchResult.
     */
    private suspend fun getSuggestions(query: String): List<SearchResult> {
        val searchSuggestions = clientMgr.get("https://api.ultimate-guitar.com/api/v1/tab/suggestion?q=${query.encodeURLParameter()}")



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
                url = "",
                type = ResultType.SUGGESTION
            )

        }
    }

    override suspend fun getSearchSuggestionsImpl(query: String): List<SearchResult> {
        return searchImpl(query, 1).take(3) + getSuggestions(query).take(5)
    }

}