import java.io.File

fun main() {
    combineMatching(
        firstFilePath = "src/main/kotlin/s01e02-romaji.srt",
        firstFileOrigin = "RMJ",
        secondFilePath = "src/main/kotlin/s01e02-bad-en.srt",
        secondFileOrigin = "BAD-EN",
        resultFilePath = "src/main/kotlin/s01e02-bad-en-and-romaji.srt"
    )

//    combineNonMatching(
//        firstFilePath = "src/main/kotlin/s01e02-bad-en-and-romaji.srt",
//        firstFileOrigin = "",
//        secondFilePath = "src/main/kotlin/s01e02-og-en.srt",
//        secondFileOrigin = "EN",
//        resultFilePath = "src/main/kotlin/s01e02-bad-en-and-romaji-and-og-en.srt"
//    )
}

private fun combineMatching(
    firstFilePath: String,
    firstFileOrigin: String,
    secondFilePath: String,
    secondFileOrigin: String,
    resultFilePath: String
) {
    val linesFirst = File(firstFilePath).readLines()
    val linesSecond = File(secondFilePath).readLines()

    val subtitlesFirst = parseSubtitles(linesFirst, firstFileOrigin)
    val subtitlesSecond = parseSubtitles(linesSecond, secondFileOrigin)

    val newSubtitleText = subtitlesFirst.combineMatching(subtitlesSecond).toPrintableText()

    println(newSubtitleText)

    File(resultFilePath).writeText(newSubtitleText)
}

private fun combineNonMatching(
    firstFilePath: String,
    firstFileOrigin: String,
    secondFilePath: String,
    secondFileOrigin: String,
    resultFilePath: String
) {
    val linesFirst = File(firstFilePath).readLines()
    val linesSecond = File(secondFilePath).readLines()

    val subtitlesFirst = parseSubtitles(linesFirst, firstFileOrigin)
    val subtitlesSecond = parseSubtitles(linesSecond, secondFileOrigin)

    val newSubtitleText = subtitlesFirst.combineNonMatching(subtitlesSecond).toPrintableText()

    println(newSubtitleText)

    File(resultFilePath).writeText(newSubtitleText)
}

fun List<Subtitle>.combineMatching(list: List<Subtitle>): List<Subtitle> {
    val firstList = this
    val secondList = list
    return firstList.map { subtitle ->
        val secondSubtitle = secondList.find { it.duration == subtitle.duration }
            ?: error("No matching subtitle found. Investigate. Subtitle: $subtitle")
        subtitle.copy(text = "${subtitle.text}\n${secondSubtitle.originText}${secondSubtitle.text}")
    }
}

fun List<Subtitle>.combineNonMatching(list: List<Subtitle>): List<Subtitle> {
    val firstList = this
    val firstReversed = firstList.reversed()
    val secondList = list.toMutableList()
    val workaroundForFirstDuration =
        if (secondList[0].duration.startTimeMillis < firstList[0].duration.startTimeMillis) {
            val subtitle = secondList[0]
            secondList.remove(subtitle)
            listOf(subtitle)
        } else {
            emptyList()
        }
    return workaroundForFirstDuration + firstList.flatMap { firstSubtitle ->
        val secondSubtitle = secondList.find { secondSubtitle ->
            secondSubtitle.duration.startTimeMillis >= firstSubtitle.duration.startTimeMillis &&
                    // Covers case where the next subtitle is not closest in time
                    firstReversed.find { secondSubtitle.duration.startTimeMillis >= it.duration.startTimeMillis } == firstSubtitle
        }

        if (secondSubtitle == null && secondList.isNotEmpty()) {
            listOf(firstSubtitle)
        } else {
            secondSubtitle?.let(secondList::remove)
            buildList {
                add(firstSubtitle)
                secondSubtitle?.let { add(it) }
            }
        }
    } + secondList // Add what's left in second after first list runs out
}

fun parseSubtitles(lines: List<String>, origin: String): List<Subtitle> {
    val chunks = lines.drop(1)
        .foldIndexed(listOf(listOf<String>())) { index, acc, next ->
            if (next.contains("-->")) {
                if (index == 1) {
                    acc.addEmpty()
                        .addValue(next)
                } else {
                    (acc.dropLast(1) + listOf(acc.last().dropLast(2)))
                        .addEmpty()
                        .addValue(next)
                }
            } else {
                acc.addValue(next)
            }
        }.drop(1)
    return chunks.mapIndexed { index, chunk ->
        val durationText = chunk.first()
        Subtitle(
            origin = origin,
            number = "${index + 1}",
            duration = Duration(
                text = durationText,
                startTime = durationText.split("-->")[0].trim(),
                endTime = durationText.split("-->")[1].trim()
            ),
            text = chunk.drop(1).joinToString("\n")
        )
    }
}

fun List<List<String>>.addEmpty(): List<List<String>> = this + listOf(emptyList())
fun List<List<String>>.addValue(value: String): List<List<String>> {
    val lastList = this.last()
    val appendedLastList = lastList + listOf(value)
    return this.dropLast(1) + listOf(appendedLastList)
}

data class Subtitle(
    val origin: String,
    val number: String,
    val duration: Duration,
    val text: String
) {
    val originText: String = if (origin.isNotBlank()) "[${origin}] " else ""
}

fun List<Subtitle>.toPrintableText(): String =
    joinToString(separator = "\n\n", transform = Subtitle::toDisplayString)

fun Subtitle.toDisplayString(): String =
    "${number}\n" +
            "${duration.text}\n" +
            "$originText$text"

data class Duration(
    val text: String,
    val startTime: String,
    val endTime: String
) {
    val startTimeMillis: Long
        get() {
            val (other, millis) = startTime.split(",")
            val (hours, minutes, seconds) = other.split(":").map { it.toLong() }
            return (hours * 3600 + minutes * 60 + seconds) * 1000 + millis.toLong()
        }
    val endTimeMillis: Long
        get() {
            val (other, millis) = endTime.split(",")
            val (hours, minutes, seconds) = other.split(":").map { it.toLong() }
            return (hours * 3600 + minutes * 60 + seconds) * 1000 + millis.toLong()
        }

}