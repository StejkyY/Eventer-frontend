package eventer.project

import eventer.project.layout.menu
import eventer.project.layout.EventAgendaPanel
import eventer.project.layout.EventBasicInfoPanel
import eventer.project.layout.EventDescriptionPanel
import eventer.project.layout.EventPanel
import eventer.project.layout.*
import eventer.project.web.ConduitManager
import eventer.project.web.RoutingManager
import eventer.project.web.View
import io.kvision.*
import io.kvision.i18n.DefaultI18nManager
import io.kvision.i18n.I18n
import io.kvision.pace.Pace
import io.kvision.pace.PaceOptions
import io.kvision.panel.root
import io.kvision.panel.simplePanel
import io.kvision.state.bind
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

val AppScope = CoroutineScope(Dispatchers.Default + SupervisorJob())

class App : Application() {
    init {
        require("css/styles.css")
        Pace.init(require("pace-progressbar/themes/blue/pace-theme-minimal.css"))
        Pace.setOptions(PaceOptions(manual = true))
        ConduitManager.initialize()
        if (I18n.language !in listOf("en", "cz")) {
            I18n.language = "en"
        }
    }

    override fun start(state: Map<String, Any>) {
        I18n.manager =
            DefaultI18nManager(
                mapOf(
                    "cz" to require("i18n/messages-cz.json"),
                    "en" to require("i18n/messages-en.json")
                )
            )

        root("eventer" ) {
            simplePanel().bind(ConduitManager.agendaStore) { state ->
                if(!state.appLoading) {
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
                            menu(state)
                            add(MyProfilePanel(state))
                        }
                        View.NEW_EVENT -> {
                            menu(state)
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
