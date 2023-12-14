package de.libf.kordbook.data.converter

fun interface ChordConverter {
    fun convertToChordPro(input: String): String
}