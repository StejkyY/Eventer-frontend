package eventer.project.models.dto

import eventer.project.models.Event
import kotlinx.serialization.Serializable

@Serializable
data class EventDTO(val event: Event)

@Serializable
data class EventsDTO(val events: List<Event>)
