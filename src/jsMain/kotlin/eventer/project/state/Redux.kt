package eventer.project.state

import eventer.project.web.View
import eventer.project.models.*
import io.kvision.redux.RAction

data class AgendaAppState(
    val view: View = View.EVENTS,
    val previousView: View = View.EVENTS,
    val profile: User? = null,
    val eventRoles: List<EventRole>? = null,
    val events: List<Event>? = null,
    val sessionTypes: List<Type>? = null,
    val selectedEvent: Event? = null,
    val selectedEventSessions: List<Session>? = null,
    val selectedEventLocations: List<Location>? = null,
//    val editingEventState: EventFormattedState? = null,
    val formattedEventSessions: Map<Double, Map<Location, List<Session>>>? = null,
    val googleAccountSynced: Boolean = false,
    val microsoftAccountSynced: Boolean = false,
)

sealed class AgendaAppAction: RAction {
    object loginPage: AgendaAppAction()
    object registerPage: AgendaAppAction()
    object eventsPage: AgendaAppAction()
    object profilePage: AgendaAppAction()
    object newEventPage: AgendaAppAction()
    object eventPreviewPage: AgendaAppAction()
    object eventBasicInfoPage: AgendaAppAction()
    object eventDescriptionPage: AgendaAppAction()
    object eventAgendaPage: AgendaAppAction()
    object previousPage: AgendaAppAction()
    object logout: AgendaAppAction()
    object googleAccountUnlinked: AgendaAppAction()
    object microsoftAccountUnlinked: AgendaAppAction()
    data class eventsLoaded(val events: List<Event>): AgendaAppAction()
    data class eventRolesLoaded(val roles: List<EventRole>): AgendaAppAction()
    data class eventLoaded(val event: Event): AgendaAppAction()
    data class eventUpdated(val event: Event): AgendaAppAction()
    data class sessionTypesLoaded(val types: List<Type>): AgendaAppAction()
    data class eventSessionsLoaded(val sessions: List<Session>): AgendaAppAction()
    data class eventLocationsLoaded(val locations: List<Location>): AgendaAppAction()
    data class profileLoaded(val profile: User): AgendaAppAction()
    data class formattedEventSessionsLoaded(val formattedEventSessions: Map<Double, Map<Location, List<Session>>>): AgendaAppAction()
    object googleAccountLinked: AgendaAppAction()
    object microsoftAccountLinked: AgendaAppAction()
}

fun agendaAppReducer(state: AgendaAppState, action: AgendaAppAction): AgendaAppState = when (action) {
    is AgendaAppAction.loginPage -> {
        state.copy(view = View.LOGIN)
    }
    is AgendaAppAction.registerPage -> {
        state.copy(view = View.REGISTER)
    }
    is AgendaAppAction.eventsPage -> {
        state.copy(view = View.EVENTS, previousView = if (state.view == View.LOGIN) state.previousView else state.view)
    }
    is AgendaAppAction.profilePage -> {
        state.copy(view = View.PROFILE, previousView = if (state.view == View.LOGIN) state.previousView else state.view)
    }
    is AgendaAppAction.newEventPage -> {
        state.copy(view = View.NEW_EVENT, previousView = if (state.view == View.LOGIN) state.previousView else state.view)
    }
    is AgendaAppAction.eventPreviewPage -> {
        state.copy(view = View.EVENT_PREVIEW, previousView = if (state.view == View.LOGIN) state.previousView else state.view)
    }
    is AgendaAppAction.eventBasicInfoPage -> {
        state.copy(view = View.EVENT_BASIC_INFO, previousView = if (state.view == View.LOGIN) state.previousView else state.view)
    }
    is AgendaAppAction.eventDescriptionPage -> {
        state.copy(view = View.EVENT_DESCRIPTION, previousView = if (state.view == View.LOGIN) state.previousView else state.view)
    }
    is AgendaAppAction.eventAgendaPage -> {
        state.copy(view = View.EVENT_AGENDA, previousView = if (state.view == View.LOGIN) state.previousView else state.view)
    }
    is AgendaAppAction.previousPage -> {
        state.copy(view = state.previousView, previousView = if (state.view == View.LOGIN) state.previousView else state.view)
    }
    is AgendaAppAction.eventLoaded -> {
        state.copy(selectedEvent = action.event)
    }
    is AgendaAppAction.eventUpdated -> {
        state.copy(selectedEvent = action.event)
    }
    is AgendaAppAction.eventLocationsLoaded -> {
        state.copy(selectedEventLocations = action.locations)
    }
    is AgendaAppAction.eventSessionsLoaded -> {
        state.copy(selectedEventSessions = action.sessions)
    }
    is AgendaAppAction.eventsLoaded -> {
        state.copy(events = action.events)
    }
    is AgendaAppAction.eventRolesLoaded -> {
        state.copy(eventRoles = action.roles)
    }
    is AgendaAppAction.profileLoaded -> {
        state.copy(profile = action.profile)
    }
    is AgendaAppAction.sessionTypesLoaded -> {
        state.copy(sessionTypes = action.types)
    }
    is AgendaAppAction.googleAccountLinked -> {
        state.copy(googleAccountSynced = true)
    }
    is AgendaAppAction.microsoftAccountLinked -> {
        state.copy(microsoftAccountSynced = true)
    }
    is AgendaAppAction.logout -> {
        AgendaAppState()
    }
    is AgendaAppAction.formattedEventSessionsLoaded -> {
        state.copy(formattedEventSessions = action.formattedEventSessions)
    }
    is AgendaAppAction.googleAccountUnlinked -> {
        state.copy(googleAccountSynced = false)
    }
    is AgendaAppAction.microsoftAccountUnlinked -> {
        state.copy(microsoftAccountSynced = false)
    }
}