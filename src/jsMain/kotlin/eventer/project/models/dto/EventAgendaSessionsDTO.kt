package eventer.project.models.dto

import eventer.project.models.Session
import kotlinx.serialization.Serializable

@Serializable
data class EventAgendaSessionsDTO(val addedSessions: List<Session>,
                                  val updatedSessions: List<Session>,
                                  val deletedSessions: List<Session>)
