package de.libf.kordbook.data.model

data class SearchResult(
    val title: String,
    val artist: String,
    val version: Int?,
    val rating: Double?,
    val votes: Double?,
    val id: String,
    val url: String
)