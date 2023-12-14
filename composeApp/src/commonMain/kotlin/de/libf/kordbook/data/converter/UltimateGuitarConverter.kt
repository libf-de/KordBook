package de.libf.kordbook.data.converter

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf

object UltimateGuitarConverter : ChordConverter {
    override fun convertToChordPro(input: String): String {
        val chordLineRegex = Regex("^\\s*((([A-G]|N\\.?C\\.?)([#b])?([^/\\s-:]*)(/([A-G]|N\\.?C\\.?)([#b])?)?)(\\s|\$)+)+(\\s|\$)+")
        val notAChord = listOf("Interlude")

        val lines = input.sanitizeInput().split("\n")
        val lineIt = lines.iterator()

        val sectionSuffix = mutableStateOf("")

        val output = StringBuilder()

        while (lineIt.hasNext()) {
            val line = lineIt.next()

            // Check if current line is empty -> just add to output
            if (line.isBlank()) {
                output.append("\n")
                continue
            }

            if(line.matches(Regex("^\\[.*]$"))) {
                parseSection(
                    line = line,
                    sectionSuffix = sectionSuffix,
                    output = output
                )
                continue
            }

            // Check if line matches CHORD_LINE_REGEX, there is a nextline and line contains no badwords
            if (chordLineRegex.matches(line) &&
                lineIt.hasNext() &&
                notAChord.none { line.contains(it) }
            ) {
                parseChordLine(
                    lineIt = lineIt,
                    line = line,
                    output = output
                )
            } else {
                output.append(line).append("\n")
                if (line.isBlank()) output.append("\n")
            }
        }

        return output.cleanupOutput()
    }

    private fun String.sanitizeInput(): String {
        return this
            .replace(Regex("(\\[[A-Z][A-Za-z0-9 ]+])\n\n", RegexOption.MULTILINE), "$1\n")
            .replace("\r", "")
            .replace(Regex("(\\[[A-Z][A-Za-z0-9 ]+])\n\n", RegexOption.MULTILINE), "$1\n")
            .replace("[tab]", "")
            .replace("[/tab]", "")
            .replace("[ch]", "")
            .replace("[/ch]", "")
    }

    private fun parseSection(sectionSuffix: MutableState<String>, line: String, output: StringBuilder) {
        // Verse regex
        val verseRegex = Regex("^\\[\\s*Verse(.*)?]$")
        val chorusRegex = Regex("^\\[\\s*Chorus(.*)?]$")
        val bridgeRegex = Regex("^\\[\\s*Bridge(.*)?]$")

        output.append(sectionSuffix.value)

        if(verseRegex.matches(line)) {
            val verseNumber = verseRegex.find(line)?.groups?.get(1)?.value?.trim()
            if(verseNumber?.isNotBlank() == true) {
                output.append("{start_of_verse: Verse ${verseNumber}}\n")
            } else {
                output.append("{start_of_verse}\n")
            }
            sectionSuffix.value = "{end_of_verse}\n"
        } else if(chorusRegex.matches(line)) {
            val chorusNumber = chorusRegex.find(line)?.groups?.get(1)?.value?.trim()
            if(chorusNumber?.isNotBlank() == true) {
                output.append("{start_of_chorus: Chorus ${chorusNumber}}\n")
            } else {
                output.append("{start_of_chorus}\n")
            }
            sectionSuffix.value = "{end_of_chorus}\n"
        } else if(bridgeRegex.matches(line)) {
            val bridgeNumber = bridgeRegex.find(line)?.groups?.get(1)?.value?.trim()
            if(bridgeNumber?.isNotBlank() == true) {
                output.append("{start_of_bridge: Bridge ${bridgeNumber}}\n")
            } else {
                output.append("{start_of_bridge}\n")
            }
            sectionSuffix.value = "{end_of_bridge}\n"
        } else {
            output.append("{highlight: ${line.trim().removePrefix("[").removeSuffix("]")}}\n")
            sectionSuffix.value = ""
        }
    }

    private fun parseChordLine(lineIt: Iterator<String>, line: String, output: StringBuilder) {
        var textLine = lineIt.next()

        var posOffset = 0
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
    }

    private fun StringBuilder.cleanupOutput(): String {
        return this.toString()
            .replace(Regex("^(.+)(\\{end_of_[a-z]+\\})$"), "\n\n") /* Ensure {end_of_*} is on its own line */
            .replace(Regex("\n(\\{end_of_[a-z]+\\})\n", RegexOption.MULTILINE), "$1\n") /* Ensure there is no newline before {end_of_*} */
            .replace(Regex("^(.+)\\n(\\{[^\\n}e].+\\})", RegexOption.MULTILINE), "$1\n\n$2") /* Ensure there is a newline before {start_of_*} */
    }
}