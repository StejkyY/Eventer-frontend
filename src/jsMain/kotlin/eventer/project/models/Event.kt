//@file:UseContextualSerialization(LocalDate::class, LocalTime::class)


package eventer.project.models

import eventer.project.web.LocalDateSerializer
import eventer.project.web.LocalTimeSerializer
import io.kvision.types.LocalDate
import io.kvision.types.LocalTime
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseContextualSerialization

@Serializable
data class Event(
    val id: Int? = null,
    val name: String? = null,
    val location: String? = null,
    @Serializable(with = LocalDateSerializer::class)
    val startDate: LocalDate? = null,
    @Serializable(with = LocalDateSerializer::class)
    val endDate: LocalDate? = null,
    @Serializable(with = LocalTimeSerializer::class)
    val startTime: LocalTime? = null,
    @Serializable(with = LocalTimeSerializer::class)
    val endTime: LocalTime? = null,
    val description: String? = null,
    val type: EventType? = null,
    val state: EventState? = null,
    var userEventRole: EventRole? = null,
    val privacy: EventPrivacy? = null
)