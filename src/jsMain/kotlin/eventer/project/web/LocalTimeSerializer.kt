package eventer.project.web

import io.kvision.types.LocalDate
import io.kvision.types.LocalTime
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlin.js.Date

object LocalTimeSerializer : KSerializer<LocalTime> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("LocalTime", PrimitiveKind.STRING)

    /**
     * Serializes a `LocalTime` value into a string.
     */
    override fun serialize(encoder: Encoder, value: LocalTime) {
        val timeString = value.toTimeString().take(8)
        encoder.encodeString(timeString)
    }

    /**
     * Deserializes a string into a `LocalTime` value.
     */
    override fun deserialize(decoder: Decoder): LocalTime {
        val timeString = decoder.decodeString()
        if(timeString.length > 8){
            return Date(timeString)
        } else {
            return Date("1970-01-01T$timeString")
        }
    }
}