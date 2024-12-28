package eventer.project.layout

import eventer.project.state.AgendaAppState
import io.kvision.core.*
import io.kvision.html.Icon
import io.kvision.html.Label
import io.kvision.html.span
import io.kvision.i18n.I18n
import io.kvision.i18n.gettext
import io.kvision.i18n.tr
import io.kvision.panel.SimplePanel
import io.kvision.panel.hPanel
import io.kvision.panel.simplePanel
import io.kvision.panel.vPanel
import io.kvision.utils.perc
import io.kvision.utils.px
import io.kvision.utils.vh
import io.kvision.utils.vw

class EventPreviewPanel(state: AgendaAppState) : SimplePanel() {

    init {
        vPanel(spacing = 20, className = "event-preview-header") {
            add(Label(state.selectedEvent?.name.toString(), className = "event-preview-main-label"))
            hPanel(spacing = 5, className = "event-preview-header-text-aligned") {
                add(Icon("fas fa-location-dot"))
                add(Label(state.selectedEvent?.location.toString(), className = "event-preview-location-label"))
            }
            val typeString = gettext(state.selectedEvent?.type.toString())
            add(Label(typeString, className = "event-preview-type-label"))
            hPanel(spacing = 20, className = "event-preview-header-text-aligned") {
                vPanel() {
                    alignItems = AlignItems.CENTER
                    add(Label(state.selectedEvent?.startDate?.toDateString()))
                    add(Label(state.selectedEvent?.startTime?.toLocaleTimeString()?.dropLast(3)))
                }
                add(Label(" – "))
                vPanel() {
                    alignItems = AlignItems.CENTER
                    add(Label(state.selectedEvent?.endDate?.toDateString()))
                    add(Label(state.selectedEvent?.endTime?.toLocaleTimeString()?.dropLast(3)))
                }
            }
        }
        vPanel(className = "event-preview-description") {
            vPanel {
                width = 70.perc
                add(Label(state.selectedEvent?.description, rich = true))
            }
        }

        vPanel {
            paddingTop = 3.perc
            alignItems = AlignItems.CENTER
            add(Label(tr("Agenda"), className = "event-preview-agenda-label"))
            simplePanel(className = "event-preview-agenda") {
                add(EventAgendaPanel(state, CalendarMode.PREVIEW))
            }
        }
    }
}