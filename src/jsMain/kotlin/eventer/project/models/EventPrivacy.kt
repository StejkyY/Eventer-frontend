package eventer.project.models

import kotlinx.serialization.Serializable

@Serializable
enum class EventPrivacy {
    Private, Public
}