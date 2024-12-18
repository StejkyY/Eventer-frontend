package eventer.project.models

import kotlinx.serialization.Serializable

@Serializable
enum class EventState {
    Upcoming, InProgress, Elapsed
}