package eventer.project.models.dto

import eventer.project.models.EventRole
import kotlinx.serialization.Serializable

@Serializable
data class EventRoleDTO(val eventRole: EventRole)

@Serializable
data class EventRolesDTO(val eventRoles: List<EventRole>)
