package eventer.project.models

import kotlinx.serialization.Serializable

@Serializable
enum class EventType {
    InPerson, Hybrid, Virtual
}