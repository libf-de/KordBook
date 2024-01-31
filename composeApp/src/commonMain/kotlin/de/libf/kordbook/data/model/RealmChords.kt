package de.libf.kordbook.data.model

import io.realm.kotlin.ext.realmListOf
import io.realm.kotlin.types.RealmList
import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.FullText
import io.realm.kotlin.types.annotations.PrimaryKey
import org.mongodb.kbson.ObjectId

class RealmChords(
    var id: String,

    var title: String,

    var artist: String,
    var versions: RealmList<RealmChords> = realmListOf(),
    var related: RealmList<RealmChords>,
    var url: String,

    @PrimaryKey
    var _id: ObjectId = ObjectId(),
    var rating: Double? = null,
    var votes: Double? = null,
    var version: String? = null,
    var tonality: String? = null,
    var capo: String? = null,
    var chords: String? = null,
) : RealmObject {
    constructor() : this("", "", "", realmListOf(), realmListOf(), "")
}