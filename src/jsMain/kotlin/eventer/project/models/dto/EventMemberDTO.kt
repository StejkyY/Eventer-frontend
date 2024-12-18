package eventer.project.models.dto

import eventer.project.models.EventMember
import kotlinx.serialization.Serializable

@Serializable
data class EventMemberDTO(val eventMember: EventMember)
