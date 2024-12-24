package eventer.project.web

import io.kvision.types.LocalDate
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlin.js.Date

object LocalDateSerializer : KSerializer<LocalDate> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("LocalDate", PrimitiveKind.STRING)

    /**
     * Serializes a `LocalDate` value into a formatted string (YYYY-MM-DD).
     */
    override fun serialize(encoder: Encoder, value: LocalDate) {
        val year = value.getFullYear()
        val month = (value.getMonth() + 1).toString().padStart(2, '0')
        val day = value.getDate().toString().padStart(2, '0')
        val dateString = "$year-$month-$day"
        encoder.encodeString(dateString)
    }

    /**
     * Deserializes a formatted string (YYYY-MM-DD) into a `LocalDate` value.
     */
    override fun deserialize(decoder: Decoder): LocalDate {
        val dateString = decoder.decodeString()
        val parts = dateString.take(10).split("-").map { it.toInt() }
        return Date(parts[0], parts[1] - 1, parts[2], 0, 0, 0)
    }
}