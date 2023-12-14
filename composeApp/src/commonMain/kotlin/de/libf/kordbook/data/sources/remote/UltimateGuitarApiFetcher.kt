package de.libf.kordbook.data.sources.remote

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

private fun <E> List<E>.append(function: () -> E): List<E> {
    return this + function()
}

class UltimateGuitarApiFetcher : ChordOrigin, KoinComponent {

    companion object {
        const val NAME = "Ultimate Guitar"
        const val REMOTE_SOURCE = false
    }

    override val NAME: String
        get() = Companion.NAME

    override val REMOTE_SOURCE: Boolean
        get() = Companion.REMOTE_SOURCE

    val md5Tool by inject<Md5>()

    val CLIENT_ID = (1..16).map {
        (('a'..'f') + ('0'..'9')).random()
    }.joinToString("")

    fun API_KEY(): String {
        val CURRENT_DATE = Clock.System.now().toLocalDateTime(TimeZone.UTC)
        return "${CLIENT_ID}${CURRENT_DATE.year}-" +
                "${CURRENT_DATE.monthNumber.toTwoDigitString()}-" +
                "${CURRENT_DATE.dayOfMonth.toTwoDigitString()}:" +
                "${CURRENT_DATE.hour.toTwoDigitString()}createLog()"
    }


    protected val client = HttpClient {
        defaultRequest {
            header("X-UG-CLIENT-ID", CLIENT_ID)
            header("X-UG-API-KEY", md5Tool.fromString(API_KEY()))
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
        fun toChords(): Chords {
            val chordsContent = convertUGToChordPro(this.content)
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

        private fun convertUGToChordPro(input: String): String {
            println("converting from:")
            println(input)
            println("==========================================")

            val CHORD_LINE_REGEX = Regex("^\\s*((([A-G]|N\\.?C\\.?)([#b])?([^/\\s-:]*)(/([A-G]|N\\.?C\\.?)([#b])?)?)(\\s|\$)+)+(\\s|\$)+")

            val saneInput = input
                .replace(Regex("(\\[[A-Z][A-Za-z0-9 ]+\\])\n\n", RegexOption.MULTILINE), "$1\n")

            println("==========================================")
            println("sane input:")
            println(saneInput)

            val lines = saneInput
                .replace("\r", "")
                .replace(Regex("(\\[[A-Z][A-Za-z0-9 ]+\\])\n\n", RegexOption.MULTILINE), "$1\n")
                .replace("[tab]", "")
                .replace("[/tab]", "")
                .replace("[ch]", "")
                .replace("[/ch]", "")
                .split("\n")

            //val outputLines = mutableListOf<String>()

            val output = StringBuilder()

            var isChordsLine = true
            var contentStarted = true
            //var nextChords = mutableListOf<Pair<Int, String>>()
            var sectionSuffix = ""

            val lineIt = lines.iterator()

            while (lineIt.hasNext()) {
                val line = lineIt.next()

                // Check if current line is empty -> just add to output
                if (line.isBlank()) {
                    output.append("\n")
                    continue
                }

                if(line.matches(Regex("^\\[.*\\]$"))) {
                    // Verse regex
                    val verseRegex = Regex("^\\[\\s*Verse(.*)?\\]$")
                    val chorusRegex = Regex("^\\[\\s*Chorus(.*)?\\]$")
                    val bridgeRegex = Regex("^\\[\\s*Bridge(.*)?\\]$")

                    output.append(sectionSuffix)

                    if(verseRegex.matches(line)) {
                        val verseNumber = verseRegex.find(line)?.groups?.get(1)?.value?.trim()
                        if(verseNumber?.isNotBlank() == true) {
                            output.append("{start_of_verse: Verse ${verseNumber}}\n")
                        } else {
                            output.append("{start_of_verse}\n")
                        }
                        sectionSuffix = "{end_of_verse}\n"
                    } else if(chorusRegex.matches(line)) {
                        val chorusNumber = chorusRegex.find(line)?.groups?.get(1)?.value?.trim()
                        if(chorusNumber?.isNotBlank() == true) {
                            output.append("{start_of_chorus: Chorus ${chorusNumber}}\n")
                        } else {
                            output.append("{start_of_chorus}\n")
                        }
                        sectionSuffix = "{end_of_chorus}\n"
                    } else if(bridgeRegex.matches(line)) {
                        val bridgeNumber = bridgeRegex.find(line)?.groups?.get(1)?.value?.trim()
                        if(bridgeNumber?.isNotBlank() == true) {
                            output.append("{start_of_bridge: Bridge ${bridgeNumber}}\n")
                        } else {
                            output.append("{start_of_bridge}\n")
                        }
                        sectionSuffix = "{end_of_bridge}\n"
                    } else {
                        output.append("{highlight: ${line.trim().removePrefix("[").removeSuffix("]")}}\n")
                        sectionSuffix = ""
                    }
                    continue
                }

                val notAChord = listOf("Interlude")

                // Check if line matches CHORD_LINE_REGEX, there is a nextline and line contains no badwords
                if (CHORD_LINE_REGEX.matches(line) &&
                    lineIt.hasNext() &&
                    notAChord.none { line.contains(it) }
                    ) {
                    var textLine = lineIt.next()

                    var posOffset: Int = 0
                    // Match everything that's not a whitespace
                    Regex("\\S+").findAll(line).forEach {
                        val chordToInsert = "[${it.value}]"

                        val pos = it.range.first + posOffset

                        textLine = if (pos < textLine.length) {
                            textLine.substring(0, pos) + chordToInsert + textLine.substring(pos)
                        } else {
                            textLine + " ".repeat(pos - textLine.length) + chordToInsert
                        }

                        posOffset += chordToInsert.length
                    }

                    output.append(textLine).append("\n")

                    if (textLine.isBlank()) output.append("\n")
                } else {
                    output.append(line).append("\n")
                    if (line.isBlank()) output.append("\n")
                }
            }

            // cleanup output
            val cleanOutput = output.toString()
                .replace(Regex("^(.+)(\\{end_of_[a-z]+\\})$"), "\n\n") /* Ensure {end_of_*} is on its own line */
                .replace(Regex("\n(\\{end_of_[a-z]+\\})\n", RegexOption.MULTILINE), "$1\n") /* Ensure there is no newline before {end_of_*} */
                .replace(Regex("^(.+)\\n(\\{[^\\n\\}e].+\\})", RegexOption.MULTILINE), "$1\n\n$2") /* Ensure there is a newline before {start_of_*} */

            println("==========================================")
            println("to:")
            println(cleanOutput)

            return cleanOutput
        }
    }

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

    override suspend fun fetchSongBySearchResult(searchResult: SearchResult): Chords? {
        return fetchSongByUrl(searchResult.url)
    }

    override suspend fun searchSongs(query: String, page: Int): List<SearchResult> {
        var start = Clock.System.now().toEpochMilliseconds()
        println("start running from ${NAME} at ${start}")
        val response = client.get(
            "https://api.ultimate-guitar.com/api/v1/tab/search" +
                    "?title=${query.encodeURLParameter()}" +
                    "&display_songs=1" +
                    "&filter=1" +
                    "&type[]=300" +
                    "&page=${page}")

        println("client id = ${CLIENT_ID}, api key = ${API_KEY()}")

        println("got response from ${NAME} in ${Clock.System.now().toEpochMilliseconds() - start}ms")

        if (response.status.value !in 200..299) {
            if(response.status.value == 404) return emptyList()

            Napier.e { "Unexpected code: $response" }
            throw Exception("Unexpected code: $response")
        }

        val searchData: UGSearch = response.body()

        println("parsed response from ${NAME} in ${Clock.System.now().toEpochMilliseconds() - start}ms")

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

    override fun searchSongsFlow(query: String): Flow<Pair<ChordOrigin, List<SearchResult>>> = flow {
        if(query.isBlank()) return@flow

        emit(this@UltimateGuitarApiFetcher to searchSongs(query))
    }

    suspend fun getSuggestions(query: String): List<SearchResult> {
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

    override suspend fun getSearchSuggestions(query: String): List<SearchResult> {
        val result = searchSongs(query).take(3) + getSuggestions(query).take(5)

        println("Got suggestions vom UG: " + result.map { it.songName }.joinToString(", "))

        return result
    }
}

fun Int.toTwoDigitString(): String {
    return if(this < 10) "0${this}" else this.toString()
}