package words

import java.io.File
import java.util.*

fun main() {
    adjustWordCountList()
}

fun adjustWordCountList() {
    val wordList = File("src/main/kotlin/words/s01-all-words.txt").readLines()
        .map {
            val (word, countString) = it.split(" ")
            Word(word, countString.drop(1).dropLast(1).toInt())
        }

    val rootLength = 4

    val groups = wordList.groupBy {
        if (it.text.length < rootLength) it.text else it.text.take(rootLength)
    }.toList()
        .sortedByDescending { (_, words) -> words.sumOf { it.count } }

    val lines = groups.map { (root, words) -> "$root: ${words.joinToString(", \n${" ".repeat(rootLength + 2)}")}" }

    File("src/main/kotlin/words/s01-all-words-grouped.txt").writeText(lines.joinToString("\n"))
}

data class Word(
    val text: String,
        val count: Int
) {
    override fun toString(): String {
        return "${text} [${count}]"
    }
}

fun createWordCountList() {
    val lines = File("src/main/kotlin/words/s01-combined-romaji.srt").readLines()
        .filter {
            it.isNotBlank() && !it.contains("-->") && !it.all { it.isDigit() || it.isWhitespace() }
                    && !it.contains(".srt:")
        }
        .map {
            val word = it.replace("…", "")
                .replace("(", "")
                .replace(")", "")
                .replace("!", "")
                .replace("?", "")
                .replace("`", "")
                .replace("'", "")
                .replace("”", "")
                .replace("“", "")
                .replace("\"", "")
                .toLowerCase(Locale.ENGLISH)
            word
        }
    val line = lines.joinToString(" ")
    val words = line.split(" ")
        .flatMap { word ->
            word.let {
                if (it.endsWith("desu")) {
                    listOf(it.replace("desu", ""))
                } else if (it.endsWith("desuka")) {
                    listOf(it.replace("desuka", ""), "desu", "ka")
                } else if (it.endsWith("desuga")) {
                    listOf(it.replace("desuga", ""), "desu", "ga")
                } else if (it.endsWith("desukara")) {
                    listOf(it.replace("desukara", ""), "desu", "kara")
                } else {
                    listOf(it)
                }
            }
        }
        .filter { !it.all { it.isDigit() || it.isWhitespace() } }

    val sorted = words.map { word -> word to words.count { it == word } }
        .distinct()
        .filter { it.first.isNotBlank() }
        .sortedWith(
            compareByDescending<Pair<String, Int>> { it.second }
                .thenBy { it.first }
        )
    File("src/main/kotlin/words/s01-all-words.txt")
        .writeText(sorted.joinToString("\n") { (word, count) -> "$word [$count]" })
}
