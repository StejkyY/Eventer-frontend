package eventer.project.models

import io.kvision.i18n.I18n
import io.kvision.i18n.tr
import kotlinx.serialization.Serializable

@Serializable
enum class EventState(val displayName: String) {
    Upcoming(tr("Upcoming")), InProgress(tr("In progress")), Elapsed(tr("Elapsed"));

    override fun toString() : String {
        return displayName
    }
}