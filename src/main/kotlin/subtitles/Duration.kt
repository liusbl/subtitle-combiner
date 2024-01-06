package subtitles

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
data class Duration(
    @SerialName("text") val text: String,
    @SerialName("startTime") val startTime: String,
    @SerialName("endTime") val endTime: String
) {
    @Transient
    val startTimeMillis: Long
        get() {
            val (other, millis) = startTime.split(",")
            val (hours, minutes, seconds) = other.split(":").map { it.toLong() }
            return (hours * 3600 + minutes * 60 + seconds) * 1000 + millis.toLong()
        }

    @Transient
    val endTimeMillis: Long
        get() {
            val (other, millis) = endTime.split(",")
            val (hours, minutes, seconds) = other.split(":").map { it.toLong() }
            return (hours * 3600 + minutes * 60 + seconds) * 1000 + millis.toLong()
        }

}