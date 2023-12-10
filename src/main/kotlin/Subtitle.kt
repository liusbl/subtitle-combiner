import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Subtitle(
    @SerialName("number") val number: String,
    @SerialName("duration") val duration: Duration,
    @SerialName("origin") val origin: String,
    @SerialName("text") val text: String
) {
    @Transient
    val originText: String = if (origin.isNotBlank()) "[${origin}] " else ""
}

fun List<Subtitle>.toPrintableText(): String =
    joinToString(separator = "\n\n", transform = Subtitle::toDisplayString)

fun Subtitle.toDisplayString(): String =
    "${number}\n" +
            "${duration.text}\n" +
            "$originText$text"

