package subtitles

import kotlinx.serialization.json.Json

val json = Json { prettyPrint = true }

fun main() {
    val episode = "s03e01"

    // Merged all japanese texts into one line so that it's better recognized in Google Translate
    val subtitlesJp = parseSubtitles(fileName = "$episode-jp.srt", origin = "JP")
        .map { subtitle ->
            val subtitleText = subtitle.textList[0]
            subtitle.copy(textList = listOf(subtitleText.copy(text = subtitleText.text.replace("\n", ""))))
        }
    "$episode-jp-single-lines.srt".let { fileName ->
        if (!fileExists(fileName)) {
            subtitlesJp.printToFile(fileName)
            println("Japanese subtitles merged into single lines.")
            println("Go translate and come back with -romaji and -bad-en files.")
            return
        }
    }

    val subtitlesRomaji = parseSubtitles(fileName = "$episode-romaji.srt", origin = "RMJ")
    val subtitlesBadEn = parseSubtitles(fileName = "$episode-bad-en.srt", origin = "BAD-EN")
    val subtitlesRomajiBadEn = subtitlesRomaji.combineMatching(subtitlesBadEn)
    subtitlesRomajiBadEn.printToFile("$episode-romaji-bad-en.srt")

    val subtitlesRomajiBadEnJp = subtitlesRomajiBadEn.combineMatching(subtitlesJp)
    subtitlesRomajiBadEnJp.printToFile("$episode-romaji-bad-en-jp.srt")

    val subtitlesOgEn = parseSubtitles(fileName = "$episode-og-en.srt", origin = "OG-EN")

    val subtitlesRomajiOgEn = subtitlesRomaji.combineNonMatching(subtitlesOgEn)
    subtitlesRomajiOgEn.printToFile("$episode-romaji-og-en.srt")

    val subtitlesRomajiBadEnOgEn = subtitlesRomajiBadEn.combineNonMatching(subtitlesOgEn)
    subtitlesRomajiBadEnOgEn.printToFile("$episode-romaji-bad-en-og-en.srt")

    val subtitlesRomajiBadEnJpOgEn = subtitlesRomajiBadEnJp.combineNonMatching(subtitlesOgEn)
    subtitlesRomajiBadEnJpOgEn.printToFile("$episode-romaji-bad-en-jp-og-en.srt")
}

fun List<Subtitle>.combineMatching(list: List<Subtitle>): List<Subtitle> {
    val firstList = this
    val secondList = list
    return firstList.map { subtitle ->
        val secondSubtitle = secondList.find { it.duration == subtitle.duration }
            ?: error("No matching subtitle found. Investigate. Subtitle: $subtitle")
        subtitle.copy(textList = subtitle.textList + secondSubtitle.textList)
    }
}

fun List<Subtitle>.combineNonMatching(list: List<Subtitle>): List<Subtitle> =
    (this + list).sortedBy { it.duration.startTimeMillis }
