package de.libf.kordbook.data.model

import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.FullText
import io.realm.kotlin.types.annotations.PrimaryKey
import org.mongodb.kbson.ObjectId

data class Chords(
    @PrimaryKey
    var _id: ObjectId = ObjectId(),
    val id: String,

    @FullText
    val title: String,

    @FullText
    val artist: String,
    val related: List<Chords>,
    val url: String,
    val origin: ChordOrigin,
    val rating: Double? = null,
    val votes: Double? = null,
    val version: String? = null,
    val tonality: String? = null,
    val capo: String? = null,
    val chords: String? = null,
) : RealmObject