package de.libf.kordbook.data.sources.remote

import com.fleeksoft.ksoup.Ksoup
import com.fleeksoft.ksoup.network.parseGetRequest
import com.fleeksoft.ksoup.nodes.Document
import de.libf.kordbook.data.model.ChordOrigin
import de.libf.kordbook.data.model.Chords
import de.libf.kordbook.data.model.SearchResult
import io.ktor.http.parsing.ParseException
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

class UltimateGuitarFetcher : ChordOrigin {



    @Serializable
    private data class UltimateGuitarSongData(
        val store: UGStore
    ) {
        @Serializable
        internal data class UGStore(
            val page: UGPage
        ) {
            @Serializable
            internal data class UGPage(
                val data: UGPageData
            ) {
                @Serializable
                internal data class UGPageData(
                    val tab_view: UGTabView,
                    val tab: UGTab
                ) {
                    @Serializable
                    internal data class UGTabView(
                        val wiki_tab: UGWikiTab,
                        val meta: UGTabMeta,
                        val versions: List<UGTab>
                    ) {
                        @Serializable
                        internal data class UGWikiTab(
                            val content: String
                        )

                        @Serializable
                        internal data class UGTabMeta(
                            val capo: Int,
                            val tonality: String,
                        )
                    }
                }
            }
        }
    }

    @Serializable
    private data class UltimateGuitarSearchData(
        val store: UGStore
    ) {
        @Serializable
        internal data class UGStore(
            val page: UGPage
        ) {
            @Serializable
            internal data class UGPage(
                val data: UGPageData
            ) {
                @Serializable
                internal data class UGPageData(
                    val results: List<UGTab>
                )
            }
        }
    }

    @Serializable
    private data class UGTab(
        val id: Int,
        val song_id: Int,
        val song_name: String,
        val artist_id: Int,
        val artist_name: String,
        val version: Int,
        val votes: Int,
        val rating: Double,
        val tonality_name: String,
        val tab_url: String,
        val type: String
    )

    private val jsonIgnoringUnknownKeys = Json { ignoreUnknownKeys = true }

    private fun convertUGToChordPro(input: String): String {
        println("converting from:")
        println(input)
        println("==========================================")


        val lines = input
            .replace("\r", "")
            .replace("[tab]", "")
            .replace("[/tab]", "")
            .replace("[ch]", "")
            .replace("[/ch]", "")
            .split("\n")

        //val outputLines = mutableListOf<String>()

        val output = StringBuilder()

        var isChordsLine = true
        var contentStarted = false
        var nextChords = mutableListOf<Pair<Int, String>>()
        var sectionSuffix = ""

        lines.forEach { line ->
            // empty lines
            if(line.isBlank() && nextChords.isEmpty()) {
                output.append("\n")
                return@forEach
            }


            // "Pre-comments"
            if(line.startsWith("[")) contentStarted = true
            if(!contentStarted) {
                output.append("{comment: ${line.trim()}}\n")
                return@forEach
            }

            // Heading
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
                isChordsLine = true
                return@forEach
            }

            // Chords
            if(isChordsLine) {
                var posOffset: Int = 0
                // Match everything that's not a whitespace
                Regex("\\S+").findAll(line).forEach {
                    val textToInsert = "[${it.value}]"
                    nextChords.add((it.range.first + posOffset) to textToInsert)
                    posOffset += textToInsert.length
                }

                isChordsLine = false
                return@forEach
            }

            // Text
            if(nextChords.isNotEmpty()) {
                var outLine: String = line
                nextChords.forEach {
                    // insert text at given position, or extend with spaces if out of bounds
                    val pos = it.first
                    val text = it.second
                    outLine = if(pos < outLine.length) {
                        outLine.substring(0, pos) + text + outLine.substring(pos)
                    } else {
                        outLine + " ".repeat(pos - outLine.length) + text
                    }
                }
                output.append(outLine).append("\n")

                if(line.isBlank()) output.append("\n")

                nextChords.clear()
                isChordsLine = true
            }
        }

        // cleanup output
        val cleanOutput = output.toString()
            .replace("\n{end_of_verse}\n{", "{end_of_verse}\n\n{")
            .replace("\n{end_of_chorus}\n{", "{end_of_chorus}\n\n{")
            .replace("\n{end_of_bridge}\n{", "{end_of_bridge}\n\n{")

        return cleanOutput
    }

    private fun getUGJsonFromUrl(url: String): String {
        val doc: Document = Ksoup.parseGetRequest(url = url)
        return doc
            .selectFirst(".js-store")
            ?.attr("data-content")
            ?.primitiveHtmlDecode() ?: throw ParseException("Song data json is null")
    }


    private fun String.primitiveHtmlDecode(): String {
        return this
            .replace("&amp;", "&")
            .replace("&lt;", "<")
            .replace("&gt;", ">")
            .replace("&quot;", "\"")
            .replace("&#039;", "'")
    }

    override val NAME: String
        get() = "Ultimate Guitar"

    override val REMOTE_SOURCE: Boolean
        get() = true

    override suspend fun fetchSongByUrl(url: String): Chords {
        val songDataJson = getUGJsonFromUrl(url)

        val songData = jsonIgnoringUnknownKeys
            .decodeFromString<UltimateGuitarSongData>(songDataJson)
            .store
            .page
            .data

        return Chords(
            id = songData.tab.id.toString(),
            title = songData.tab.song_name,
            artist = songData.tab.artist_name,
            version = songData.tab.version.toString(),
            tonality = songData.tab_view.meta.tonality,
            capo = songData.tab_view.meta.capo.toString(),
            chords = convertUGToChordPro(songData.tab_view.wiki_tab.content),
            related = songData.tab_view.versions.map {
                Chords(
                    id = it.id.toString(),
                    title = it.song_name,
                    artist = it.artist_name,
                    version = it.version.toString(),
                    tonality = it.tonality_name,
                    capo = null,
                    chords = "",
                    related = emptyList(),
                    url = it.tab_url,
                    origin = this
                )
            },
            url = url,
            origin = this
        )
    }

    override suspend fun searchSongs(query: String): List<SearchResult> {
        val searchDataJson = getUGJsonFromUrl("https://www.ultimate-guitar.com/search.php?search_type=title&value=$query")

        val searchData = jsonIgnoringUnknownKeys
            .decodeFromString<UltimateGuitarSearchData>(searchDataJson)
            .store
            .page
            .data

        return searchData.results.map {
            SearchResult(
                title = it.song_name,
                artist = it.artist_name,
                version = it.version,
                rating = it.rating,
                votes = it.votes.toDouble(),
                id = it.id.toString(),
                url = it.tab_url
            )
        }
    }
}