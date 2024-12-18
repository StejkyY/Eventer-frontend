//@file:UseContextualSerialization(LocalDate::class, LocalTime::class)


package eventer.project.models

import eventer.project.web.LocalDateSerializer
import eventer.project.web.LocalTimeSerializer
import io.kvision.types.LocalDate
import io.kvision.types.LocalTime
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseContextualSerialization


@Serializable
data class Session(
    val id: Int? = null,
    val name: String? = null,
    @Serializable(with = LocalDateSerializer::class)
    val date: LocalDate? = null,
    @Serializable(with = LocalTimeSerializer::class)
    val startTime: LocalTime? = null,
    val duration: Int? = null,
    val description: String? = null,
    val dayOrder: Int? = null,
    val type: Type? = null,
    val location: Location? = null,
    val eventId: Int? = null
)