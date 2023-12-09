package de.libf.kordbook.data.converter

class UltimateGuitarParser {
    var it: Iterator<String> = "".split("\n").iterator()
    var song: List<Pair<String, String>> = listOf()

    fun parse(input: String) {
        it = input.split("\n").iterator()

        while(it.hasNext()) {
            val line = it.next()
            parseLine(line)
        }
    }

    fun parseLine(line: String) {

    }
}