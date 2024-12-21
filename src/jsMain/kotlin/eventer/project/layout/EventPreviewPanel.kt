package eventer.project.layout

import eventer.project.state.AgendaAppState
import io.kvision.core.*
import io.kvision.html.Icon
import io.kvision.html.Label
import io.kvision.i18n.I18n
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
        vPanel(spacing = 20) {
            width = 100.vw
            height = 35.vh
            alignItems = AlignItems.CENTER
            background = Background(Color.name(Col.LIGHTGRAY))
            borderBottom = Border(1.px, BorderStyle.SOLID, Color.name(Col.BLACK))

            add(Label(state.selectedEvent?.name.toString()) {
                marginTop = 3.perc
                fontSize = 40.px
            })
            hPanel(spacing = 5) {
                verticalAlign = VerticalAlign.BASELINE
                alignItems = AlignItems.CENTER
                add(Icon("fas fa-location-dot"))
                add(Label(state.selectedEvent?.location.toString()) {
                    fontSize = 20.px
                })
            }
            add(Label(state.selectedEvent?.type.toString() + " " + tr("event")) {
                fontSize = 18.px
            })
            hPanel(spacing = 20) {
                verticalAlign = VerticalAlign.BASELINE
                alignItems = AlignItems.CENTER
                vPanel(spacing = 3) {
                    alignItems = AlignItems.CENTER
                    add(Label(state.selectedEvent?.startDate?.toDateString()))
                    add(Label(state.selectedEvent?.startTime?.toLocaleTimeString()?.dropLast(3)))
                }
                add(Label(" – "))
                vPanel(spacing = 3) {
                    alignItems = AlignItems.CENTER
                    add(Label(state.selectedEvent?.endDate?.toDateString()))
                    add(Label(state.selectedEvent?.endTime?.toLocaleTimeString()?.dropLast(3)))
                }
            }
        }
        vPanel {
            marginTop = 3.perc
            width = 100.vw
            alignItems = AlignItems.CENTER

            vPanel {
                width = 70.perc
                add(Label(state.selectedEvent?.description, rich = true))
            }
        }

        vPanel {
            alignItems = AlignItems.CENTER
            add(Label(tr("Agenda")) {
                fontSize = 30.px
            })
            simplePanel {
                marginTop = 2.perc
                width = 80.vw
                add(EventAgendaPanel(state, CalendarMode.PREVIEW))
            }
        }
    }
}