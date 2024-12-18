package eventer.project.models.dto

import eventer.project.models.Session
import kotlinx.serialization.Serializable

@Serializable
data class SessionDTO(val session: Session)

@Serializable
data class SessionsDTO(val sessions: List<Session>)
