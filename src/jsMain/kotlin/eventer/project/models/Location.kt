package eventer.project.models

import kotlinx.serialization.Serializable

@Serializable
data class Location(
    val id: Int? = null,
    val name: String? = null,
    val eventId: Int? = null
)