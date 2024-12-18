package eventer.project

import eventer.project.components.menu
import eventer.project.layout.EventAgendaPanel
import eventer.project.layout.EventBasicInfoPanel
import eventer.project.layout.EventDescriptionPanel
import eventer.project.layout.EventPanel
import eventer.project.layout.*
import eventer.project.web.ConduitManager
import eventer.project.web.RoutingManager
import eventer.project.web.View
import io.kvision.*
import io.kvision.panel.root
import io.kvision.panel.simplePanel
import io.kvision.routing.Routing
import io.kvision.state.bind
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

val AppScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
const val DEBUG = true

fun debug(s: String, color: LogType? = null) {
    if (DEBUG) {
        console.log("%c $s", when (color) {
            null -> ""
            LogType.EVENT -> "background: #FF0; color: #000000"
            LogType.ROUTING -> "background: #09F; color: #000000"
        })
    }
}

enum class LogType {
    EVENT,
    ROUTING
}

class App : Application() {

    override fun start(state: Map<String, Any>) {
        ConduitManager.initialize()

        root("eventer" ) {
            simplePanel().bind(ConduitManager.agendaStore) { state ->
                when(state.view) {
                    View.EVENTS -> {
                        menu(state)
                        add(EventsPanel(state))
                    }
                    View.LOGIN -> {
                        add(LoginPanel())
                    }
                    View.REGISTER -> {
                        add(RegisterPanel())
                    }
                    View.PROFILE -> {
                        add(MyProfilePanel(state))
                    }
                    View.NEW_EVENT -> {
                        add(NewEventPanel(state))
                    }
                    View.EVENT_PREVIEW -> {
                        add(EventPreviewPanel(state))
                    }
                    View.EVENT_BASIC_INFO -> {
                        menu(state)
                        val childPanel = EventBasicInfoPanel(state)
                        add(EventPanel(state, childPanel))
                    }
                    View.EVENT_DESCRIPTION -> {
                        menu(state)
                        val childPanel = EventDescriptionPanel(state)
                        add(EventPanel(state, childPanel))
                    }
                    View.EVENT_AGENDA -> {
                        menu(state)
                        val childPanel = EventAgendaPanel(state, CalendarMode.EDIT)
                        add(EventPanel(state, childPanel))
                    }
                }
                RoutingManager.updatePageLinks()
            }
        }
    }
}

fun main() {
    startApplication(
        ::App,
        module.hot,
        BootstrapModule,
        BootstrapCssModule,
        FontAwesomeModule,
        DatetimeModule,
        CoreModule
    )
}
