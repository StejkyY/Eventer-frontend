package eventer.project.models

import kotlinx.serialization.Serializable

@Serializable
enum class EventState(val displayName: String) {
    Upcoming("Upcoming"), InProgress("In progress"), Elapsed("Elapsed");

    override fun toString() : String {
        return displayName
    }
}