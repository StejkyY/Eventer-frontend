package eventer.project.models

import io.kvision.i18n.I18n
import io.kvision.i18n.tr
import kotlinx.serialization.Serializable

@Serializable
enum class EventType(val displayName: String) {
    InPerson(tr("In-person")), Hybrid(tr("Hybrid")), Virtual(tr("Virtual"));

    override fun toString() : String {
        return displayName
    }
}