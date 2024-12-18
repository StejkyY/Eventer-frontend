package eventer.project.models

import kotlinx.serialization.Serializable

@Serializable
data class EventMember(
    val id: Int? = null,
    val eventId: Int? = null,
    val userId: Int? = null,
    val roleId: Int? = null
)
