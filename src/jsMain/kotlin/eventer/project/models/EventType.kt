package eventer.project.models

import kotlinx.serialization.Serializable

@Serializable
enum class EventType(val displayName: String) {
    InPerson("In-person"), Hybrid("Hybrid"), Virtual("Virtual");

    override fun toString() : String {
        return displayName
    }
}