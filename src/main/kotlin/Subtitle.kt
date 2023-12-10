import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

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

fun List<Subtitle>.toPrintableText(): String =
    joinToString(separator = "\n\n", transform = Subtitle::toDisplayString)

fun Subtitle.toDisplayString(): String {
    val text = textList.joinToString(separator = "\n") { subtitle -> "[${subtitle.origin}] ${subtitle.text}" }
    return "${number}\n" +
            "${duration.text}\n" +
            text
}

