package subtitles

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.io.File

@Serializable
data class Subtitle(
    @SerialName("number") val number: String,
    @SerialName("duration") val duration: Duration,
    @SerialName("subtitleTextList") val textList: List<SubtitleText>
)

@Serializable
data class SubtitleText(
    @SerialName("origin") val origin: String,
    @SerialName("text") val text: String
)

private val subtitlesPath = "src${File.separatorChar}main${File.separatorChar}kotlin${File.separatorChar}subtitles${File.separatorChar}"

fun fileExists(fileName: String): Boolean = File("$subtitlesPath$fileName").exists()

fun List<Subtitle>.printToFile(fileName: String) {
    File("$subtitlesPath$fileName").writeText(toPrintableText())
}

fun List<Subtitle>.toPrintableText(): String =
    joinToString(separator = "\n\n", transform = Subtitle::toDisplayString)

fun Subtitle.toDisplayString(): String {
    val text = textList.joinToString(separator = "\n") { subtitle -> "[${subtitle.origin}] ${subtitle.text}" }
    return "${number}\n" +
            "${duration.text}\n" +
            text
}

fun parseSubtitles(fileName: String, origin: String): List<Subtitle> {
    val lines = File("$subtitlesPath$fileName").readLines()
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
            number = "${index + 1}",
            duration = Duration(
                text = durationText,
                startTime = durationText.split("-->")[0].trim(),
                endTime = durationText.split("-->")[1].trim()
            ),
            textList = listOf(
                SubtitleText(
                    origin = origin,
                    text = chunk.drop(1).joinToString("\n")
                )
            )
        )
    }
}

fun List<List<String>>.addEmpty(): List<List<String>> = this + listOf(emptyList())

fun List<List<String>>.addValue(value: String): List<List<String>> {
    val lastList = this.last()
    val appendedLastList = lastList + listOf(value)
    return this.dropLast(1) + listOf(appendedLastList)
}
