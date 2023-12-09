package de.libf.kordbook.data.model
enum class ResultType {
    SUGGESTION,
    RESULT
}

data class SearchResult(
    val songName: String,
    val songId: String,
    val artist: String,
    val artistId: String,
    val version: String?,
    val rating: Double?,
    val votes: Double?,
    val id: String,
    val url: String,
    val origin: String,
    val type: ResultType = ResultType.RESULT,
) {
    fun ratingVotesRatio(): Double? {
        return (this.votes?.let { it1 ->
            this.rating?.times(
                it1
            )
        })
    }
}